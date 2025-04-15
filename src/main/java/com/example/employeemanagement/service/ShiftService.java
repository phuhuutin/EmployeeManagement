package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.FindShift;
import com.example.employeemanagement.dto.ShiftDTO;
import com.example.employeemanagement.dto.SingleUserShiftData;
import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.redis.*;
import com.example.employeemanagement.redis.service.EmployerCacheService;
import com.example.employeemanagement.redis.service.FindShiftCacheService;
import com.example.employeemanagement.redis.service.UserCacheService;
import com.example.employeemanagement.redis.service.UserShiftsCacheService;
import com.example.employeemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EmployerRepository employerRepository;
    private final ClockInOutRecordRepository clockInOutRecordRepository;
    private final PayrollRepository payrollRepository;
    private final EmployerCacheService employerCacheService;
    private final UserShiftsCacheService userShiftsCacheService;
    private static final Logger logger = LoggerFactory.getLogger(ShiftService.class);
    private final FindShiftCacheService findShiftCacheService;
    private final UserCacheService userCacheService;
    private final EmployerService employerService;
     private final StringRedisTemplate stringRedisTemplate;
     private final RedisTemplate<String, Object> redisTemplate;
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public List<SingleUserShiftData> getAllShiftsByUserDatabase() throws NoResourceFoundException {
        List<SingleUserShiftData> re = new ArrayList<>();
        // Get the currently authenticated user from the SecurityContext

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Retrieves the username of the authenticated user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET,"Could not find/access the current user"));

        for(Shift shift : user.getPickedShifts()){
            SingleUserShiftData data = new SingleUserShiftData();
            data.setId(shift.getId());
            data.setUsername(user.getUsername());
            data.setDate(shift.getDate());
            data.setStartTime(shift.getStartTime());
            data.setEndTime(shift.getEndTime());
            data.setEmployer(user.getEmployer().toEmployerCache());
            for(ClockInOutRecord clock : shift.getClockInOutRecords()) {
                if (clock.getUser().equals(user)) {
                    data.setClock(clock.toClockInOutRecordCache());
                    break;
                }
            }

            re.add(data);
        }

        return re;

    }
    public List<SingleUserShiftData> getAllShiftsByUser() throws NoResourceFoundException {
    logger.info("🔍 Fetching shifts for the current user...");

    // Get the currently authenticated user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET, "Could not find/access the current user"));

    Long userId = user.getId();
    logger.info("👤 User found: {} (ID: {})", username, userId);

    // 1️⃣ Check if data exists in Redis cache
    try {
        Optional<UserShiftsCache> cachedShifts = userShiftsCacheService.findById(userId);
        if (cachedShifts.isPresent()) {
            logger.info("✅ Returning cached shifts from Redis for user ID: {}", userId);
            EmployerCache employerCache = employerCacheService.getEmployerFromCache(user.getEmployer().getId()).orElse(null);
            return cachedShifts.get().getShifts().stream().map( shift -> shift.mapToSingleUserShiftData(employerCache)).toList();
        }
    } catch (Exception e) {
        logger.error("❌ Error retrieving cached shifts for user ID: {}", userId, e);
    }

    // 2️⃣ If cache is empty, fetch shifts from database
    List<SingleUserShiftData> shifts = fetchShiftsFromDatabase(user);
    logger.info("📦 Fetched {} shifts from the database for user ID: {}", shifts.size(), userId);

    shifts.forEach(shift -> {
        if (shift.getEmployer() != null) {
            Long employerId = Long.valueOf(shift.getEmployer().getId());
            // ✅ Check if employer already exists in Redis cache
            if (employerCacheService.getEmployerFromCache(employerId).isEmpty()) {

                // ✅ Compare with user.getEmployer() before fetching from DB
                if (user.getEmployer() != null && user.getEmployer().getId().equals(employerId)) {
                    employerCacheService.saveEmployerToCache(user.getEmployer());
                } else {
                    employerRepository.findById(employerId).ifPresent(employerCacheService::saveEmployerToCache);
                }

                System.out.println("✅ Employer cached for ID: " + employerId);
            } else {
                System.out.println("🔄 Employer already cached, skipping: " + employerId);
            }
        }
    });



    // 3️⃣ Store data in Redis for future requests
    try {

        List<SingleUserShiftDataCache> cache = shifts.stream().map(SingleUserShiftData::mapToSingleUserShiftDataCache).toList();
        UserShiftsCache userShiftsCache = new UserShiftsCache(userId, cache, LocalDate.now());
        userShiftsCacheService.save(userShiftsCache);
        logger.info("📌 Cached shifts for user ID: {}", userId);
    } catch (Exception e) {
        logger.error("❌ Failed to cache shifts for user ID: {}", userId, e);
    }

    return shifts;
}

    private List<SingleUserShiftData> fetchShiftsFromDatabase(User user) {
        List<SingleUserShiftData> re = new ArrayList<>();
        for (Shift shift : user.getPickedShifts()) {
            SingleUserShiftData data = new SingleUserShiftData();
            data.setId(shift.getId());
            data.setUsername(user.getUsername());
            data.setDate(shift.getDate());
            data.setStartTime(shift.getStartTime());
            data.setEndTime(shift.getEndTime());
            if (user.getEmployer() != null) {
                data.setEmployer(shift.getEmployer().toEmployerCache());
            }

            shift.getClockInOutRecords().stream()
                    .filter(clock -> clock.getUser().equals(user))
                    .findFirst()
                    .ifPresent(clock -> data.setClock(new ClockInOutRecordCache(
                            clock.getId(),
                            clock.getClockInTime(),
                            clock.getClockOutTime(),
                            clock.getMinuteWorked(),
                            shift.getId(),
                            user.getId()
                    )));

            re.add(data);
        }
        return re;
    }

    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
    }

    public Shift saveShift(ShiftDTO shiftDTO) {
        Employer employer = employerRepository.findById(shiftDTO.getEmployerId())
                .orElseThrow(() -> new IllegalArgumentException("Can not find the given employer"));

        return shiftRepository.save(mapToShift(shiftDTO, employer));
    }
    public Shift saveShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    public boolean deleteShiftbyId(Long shiftId){
        try{
            shiftRepository.deleteById(shiftId);
            return true;
        }catch (Exception e){
            return false;
        }

    }

    // The mapping method belongs to the service
    private Shift mapToShift(ShiftDTO shiftDTO, Employer employer) {
        Shift shift = new Shift();
        shift.setDate(shiftDTO.getDate());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setWorkerLimit(shiftDTO.getWorkerLimit());

        User manager = userRepository.findById(shiftDTO.getPostedById())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + shiftDTO.getPostedById() + " not found"));
        shift.setPostedBy(manager);
        shift.setEmployer(employer);
        return shift;
    }


    public List<ClockInOutRecord> payEvaluate( Shift shift){
        List<ClockInOutRecord> clockWithCalculatedPayRoll = new ArrayList<>();

            Set<User> pickedShiftUser = shift.getEmployees();
            for(ClockInOutRecord record : shift.getClockInOutRecords()){

                if(record.getClockInTime() == null && record.getClockOutTime() == null){
                    User user = record.getUser();
                    Payroll payroll = new Payroll();
                    payroll.setShift_id(shift.getId());
                    payroll.setUser(user);
                    payroll.setPayRate(user.getPayRate());
                    payroll.addtotalPay(0);
                    record.setPayroll(payroll);
                    clockWithCalculatedPayRoll.add(record);
                    break;
                }else{
                    User user = record.getUser();
                    Payroll payroll = new Payroll();
                    payroll.setShift_id(shift.getId());
                    payroll.setUser(user);
                    payroll.setPayRate(user.getPayRate());
                    BigDecimal bd = BigDecimal.valueOf(record.getMinuteWorked() / 60).setScale(2, RoundingMode.HALF_UP);
                    payroll.addtotalHoursWorked(bd.doubleValue());
                    if(pickedShiftUser.contains(record.getUser()) ){
                        bd = BigDecimal.valueOf(user.getPayRate()/60 * record.getMinuteWorked()).setScale(2, RoundingMode.HALF_UP);
                    }else{
                        bd = BigDecimal.valueOf(0);
                    }
                    payroll.addtotalPay(bd.doubleValue());
                    record.setPayroll(payroll);
                    clockWithCalculatedPayRoll.add(record);
                }






            }

        return clockWithCalculatedPayRoll;
    }


    public Payroll singlePayEvaluate(ClockInOutRecord clock){
            Payroll payroll = clock.getPayroll();
            assert payroll != null;
            BigDecimal bd = BigDecimal.valueOf(clock.getMinuteWorked() / 60).setScale(2, RoundingMode.HALF_UP);
            payroll.setTotalHoursWorked(bd.doubleValue());
            bd = BigDecimal.valueOf(payroll.getPayRate()/60 * clock.getMinuteWorked()).setScale(2, RoundingMode.HALF_UP);
            payroll.setTotalPay(bd.doubleValue());
            return payroll;
    }
    public Shift findShiftById(Long shiftId){
        // Fetch the shift by ID
        return shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with ID: " + shiftId));
    }
    public List<ClockInOutRecord> singleShiftPayEvaluate(Long shiftId){
        Optional<Shift> shiftOptional = shiftRepository.findById(shiftId);
        Shift shift;
        if(shiftOptional.isPresent()){
            shift = shiftOptional.get();
            return this.payEvaluate(shift);
        }else{
            throw new ResourceAccessException("Error finding the shift with " + shiftId);
        }
    }





    public List<AttendanceRecord> attendanceEvaluateViaClock(ClockInOutRecord clock){
        List<AttendanceRecord> result = new ArrayList<>();
        Shift shift = clock.getShift();

        if(clock.getClockInTime() == null &&  clock.getClockOutTime() == null){
            AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.ABSENT);
            record.setAttendancePoints(clock.getUser().getAttendancePoints());
            record.setClockInOutRecord(clock);
            result.add(record);
            return result;
        }

                long startShiftOffset = Duration.between(shift.getStartTime(), clock.getClockInTime()).toMinutes();
                //if the user was late for more than 10 minutes
                if(startShiftOffset > 10){
                    //handle late
                    AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LATE);
                    record.setAttendancePoints(clock.getUser().getAttendancePoints());
                    // clock.getUser().getAttendancePoints().addNewRecord(record);
                    record.setClockInOutRecord(clock);
                    result.add(record);
                }

                if(clock.getClockOutTime() != null){
                    long endShiftOffset = Duration.between(clock.getClockOutTime(), shift.getEndTime()).toMinutes();
                    //if the user clocks out more than 10 mins early
                    if(endShiftOffset >= 10){
                        //handle clocks out early
                        AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LEAVEEARLY);
                        record.setAttendancePoints(clock.getUser().getAttendancePoints());
                        // clock.getUser().getAttendancePoints().addNewRecord(record);
                        record.setClockInOutRecord(clock);
                        result.add(record);
                    }
                }else{
                    AttendanceRecord record =  new AttendanceRecord(shift, AttendanceReason.LEAVEEARLY);
                    record.setAttendancePoints(clock.getUser().getAttendancePoints());
                    // clock.getUser().getAttendancePoints().addNewRecord(record);
                    result.add(record);
                }

        return result;
    }



    public List<ClockInOutRecord> singleShiftAttendanceEvaluate(Long shiftId ){
        Optional<Shift> shiftOptional = shiftRepository.findById(shiftId);
        Shift shift;
        if(shiftOptional.isPresent()){
            shift = shiftOptional.get();
            List<ClockInOutRecord> clocks = shift.getClockInOutRecords();
            for(ClockInOutRecord clock: clocks){
                List<AttendanceRecord> records = attendanceEvaluateViaClock(clock);
                clock.setAttendanceRecords(records);
            }
            return clocks;

        }else{
            throw new ResourceAccessException("Error finding the shift with " + shiftId);
        }
    }


    public void performTransaction() {
        List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForSet().add("key", "value1");

                return operations.exec(); // Executes the transaction
            }
        });

        System.out.println("Number of items added to set: " + txResults.get(0));
    }


    public void addEmployeeToShift(Long shiftId) throws NoResourceFoundException {
        logger.info("➡️ Attempting to add employee to shift ID: {}", shiftId);

        
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            logger.info("🔍 Authenticated user: {}", username);

            // Fetch user and shift from the database
            User employee = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("❌ User not found with username: {}", username);
                        return new IllegalArgumentException("User not found with username: " + username);
                    });

            Shift shift = shiftRepository.findById(shiftId)
                    .orElseThrow(() -> {
                        logger.error("❌ Shift not found with ID: {}", shiftId);
                        return new IllegalArgumentException("Shift not found with ID: " + shiftId);
                    });

            logger.info("✅ Successfully retrieved user: {} and shift: {}", employee.getId(), shift.getId());

            // Validate shift eligibility
            if (!employee.getEmployer().equals(shift.getEmployer())) {
                logger.error("❌ User {} does not work for employer {}", employee.getId(), shift.getEmployer().getId());
                throw new IllegalStateException("You do not work for this employer.");
            }
            if (shift.getEmployees().contains(employee)) {
                logger.error("❌ User {} has already picked this shift {}", employee.getId(), shift.getId());
                throw new IllegalStateException("User already picked this shift.");
            }
            if (LocalDateTime.now().isAfter(shift.getStartTime())) {
                logger.error("❌ Shift {} has already started. Cannot add user {}.", shift.getId(), employee.getId());
                throw new IllegalStateException("You cannot add a started shift.");
            }

            // Key for the shift workers
            String shiftKey = "shift:" + shiftId + ":workers";
            logger.info("🔑 Using Redis key: {}", shiftKey);

            // ✅ Ensure Redis and Database Worker Count are Synced
            Integer dbWorkerCount = shift.getCurrentWorkers();
            String redisValue = (String) stringRedisTemplate.opsForValue().get(shiftKey);

            if (redisValue == null || !redisValue.matches("\\d+")) {
                logger.warn("⚠️ Invalid Redis value for key {}. Resetting to DB value: {}", shiftKey, dbWorkerCount);
                stringRedisTemplate.opsForValue().set(shiftKey, String.valueOf(dbWorkerCount));
                redisValue = String.valueOf(dbWorkerCount);
            }
            final String finalRedisValue = redisValue;

            for (int attempt = 0; attempt < 3; attempt++) {
                logger.info("🔄 Attempt {} to add user {} to shift {}", attempt + 1, employee.getId(), shift.getId());

                // Using SessionCallback to execute a Redis transaction
                List<Object> txResults = stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
                    @Override
                    public List<Object> execute(RedisOperations operations) throws DataAccessException {
                        operations.watch(shiftKey); // Watch the shift worker key
                        logger.debug("👀 Watching key: {}", shiftKey);

                        // Retrieve the current worker count from Redis
                        int currentWorkers = Optional.ofNullable(finalRedisValue)
                                .map(value -> {
                                    try {
                                        return Integer.parseInt(value);
                                    } catch (NumberFormatException e) {
                                        logger.error("❌ Failed to parse Redis worker count. Using default: {}", shift.getCurrentWorkers());
                                        return shift.getCurrentWorkers(); // Fallback to DB value
                                    }
                                }).orElse(shift.getCurrentWorkers());

                        logger.info("👥 Current workers in shift {}: {} (Limit: {})", shift.getId(), currentWorkers, shift.getWorkerLimit());

                        // Check if the worker count is already at the limit
                        if (currentWorkers >= shift.getWorkerLimit()) {
                            logger.warn("🚫 Shift {} is full, cannot add user {}.", shift.getId(), employee.getId());
                            operations.unwatch(); // Release watch if the shift is full
                            return Collections.singletonList(false);
                        }

                        // Start the transaction
                        operations.multi();
                        logger.debug("🔄 Starting Redis transaction for key: {}", shiftKey);

                        // Increment the worker count
                        operations.opsForValue().increment(shiftKey);
                        logger.info("✅ Incrementing worker count in Redis for shift {}", shift.getId());

                        // Execute the transaction
                        return operations.exec(); // Return the result of the transaction
                    }
                });

                // If the transaction was successful, proceed with saving the employee and shift
                if (txResults != null && !txResults.isEmpty()) {
                    // Add the user to the shift, create clock-in record, etc.
                    ClockInOutRecord clock = new ClockInOutRecord();
                    clock.setShift(shift);
                    clock.setUser(employee);
                    clock = clockInOutRecordRepository.save(clock);
                    logger.info("🕒 Created clock-in record for user {} on shift {}", employee.getId(), shift.getId());

                    shift.addEmployee(employee);
                    employee.getPickedShifts().add(shift);
                    shift.addClockInRecord(clock);

                    shiftRepository.save(shift);
                    userRepository.save(employee);
                    logger.info("💾 Successfully saved user {} and shift {}", employee.getId(), shift.getId());

                    // Update the cache
                    updateUserShiftsCache(employee.getId());
                    findShiftCacheService.cacheTheWholeFindShift(shift);
                    logger.info("📌 Cache updated for shift {} and user {}", shift.getId(), employee.getId());

                    return; // Exit the method on success
                }

                logger.warn("⚠️ Retrying transaction for shift {} (Attempt {}/{})", shift.getId(), attempt + 1, 3);
            }

            throw new IllegalStateException("❌ Failed to add employee to shift after multiple attempts.");

    }




    public List<FindShift> findShiftAfterNow(Long employerId) {
        // Fetch cached shifts
        logger.debug("Fetching all future shifts from cache...");
        List<FindShiftCache> cachedShifts = findShiftCacheService.getAllShiftsCache();

        if (!cachedShifts.isEmpty()) {
            logger.debug("Returning {} future shifts from cache", cachedShifts.size());

            return cachedShifts.stream().map(shiftCache -> {
                logger.debug("Processing cached shift ID: {}", shiftCache.getId());

                // Fetch employer details from cache or DB
                EmployerCache employer = employerCacheService.getEmployerFromCache(shiftCache.getEmployerId()).orElse(null);
                if (employer == null) {
                    logger.debug("Employer ID {} not found in cache, fetching from DB", shiftCache.getEmployerId());
                    employer = employerService.getEmployerById(shiftCache.getEmployerId()).toEmployerCache();
                    employerCacheService.save(employer);
                }

                // Fetch employees from cache or DB
                Set<Long> employeeIds = shiftCache.getEmployeesId();
                FindShift findShift = shiftCache.toFindShift();
                findShift.setEmployer(employer);

                for (Long id : employeeIds) {
                    logger.debug("Fetching employee ID: {}", id);
                    UserCache userCache = userCacheService.findById(id).orElse(null);
                    if (userCache == null) {
                        logger.debug("User ID {} not found in cache, fetching from DB", id);
                        userCache = userService.getUserById(id).toUserCache();
                        userCacheService.save(userCache, UserCache.CACHE_TTL);
                    }
                    findShift.addEmployee(userCache);
                }

                // Fetch postedBy user details
                UserCache postedBy = userCacheService.findById(shiftCache.getPostedByUserId()).orElse(null);
                if (postedBy == null) {
                    logger.debug("PostedBy User ID {} not found in cache, fetching from DB", shiftCache.getPostedByUserId());
                    postedBy = userService.getUserById(shiftCache.getPostedByUserId()).toUserCache();
                    userCacheService.save(postedBy, UserCache.CACHE_TTL);
                }
                findShift.setPostedBy(postedBy);

                return findShift;
            }).collect(Collectors.toList());
        }

        // Fetch from DB if cache is empty
        logger.debug("No cached shifts found, fetching from DB...");
        List<Shift> shifts = shiftRepository.findAllShiftsAfterNow(LocalDateTime.now(), employerId);
        List<FindShift> result = new ArrayList<>();

        for (Shift shift : shifts) {
            logger.debug("Caching new shift ID: {}", shift.getId());
//
//            FindShiftCache cache = new FindShiftCache();
//            cache.setId(shift.getId());
//            cache.setEmployerId(shift.getEmployer().getId());
//            cache.setStartTime(shift.getStartTime());
//            cache.setEndTime(shift.getEndTime());
//            cache.setWorkerLimit(shift.getWorkerLimit());
//            cache.setCurrentWorkers(shift.getCurrentWorkers());
//            cache.setPostedByUserId(shift.getPostedBy().getId());
//
//
//
//            FindShift findShift = cache.toFindShift();
//            findShift.setEmployer(shift.getEmployer().toEmployerCache());
//
//
//            findShift.setPostedBy(shift.getPostedBy().toUserCache());
//
//            for (User user : shift.getEmployees()) {
//                cache.addEmployee(user.getId());
//                findShift.addEmployee(user.toUserCache());
//                userCacheService.save(user.toUserCache());
//            }
//            findShiftCacheService.save(cache);
//            result.add(findShift);

            FindShift findShift = findShiftCacheService.cacheTheWholeFindShift(shift);
            result.add(findShift);
        }

        logger.debug("Returning {} shifts from DB", result.size());
        return result;
    }
    @Transactional
    public void dropShift(Long shiftId) throws NoResourceFoundException {
        // Get the currently authenticated user from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();  // Retrieves the username of the authenticated user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoResourceFoundException(HttpMethod.GET,"Could not find/access the current user"));

        // Fetch the shift by ID
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found with ID: " + shiftId));

        if(LocalDateTime.now().plusDays(1L).isAfter(shift.getStartTime()))
            throw new IllegalArgumentException("It's too late to drop this shift.");
        if(!shift.getEmployees().contains(user))
            throw new IllegalArgumentException("The user is not scheduled for this shift.");
        user.getPickedShifts().remove(shift);
        shift.getEmployees().remove(user);

        // Filter the records to be removed
        ClockInOutRecord recordToRemove = shift.getClockInOutRecords()
                .stream()
                .filter(record -> record.getUser().equals(user))
                .findFirst()
                .orElse(null);
        // Remove from the list
        if (recordToRemove != null) {
            shift.getClockInOutRecords().remove(recordToRemove);
            clockInOutRecordRepository.delete(recordToRemove);
        }
        shift.setCurrentWorkers(shift.getCurrentWorkers() - 1);
        if(shift.getCurrentWorkers() < 0)
            throw new IllegalArgumentException("Something went wrong.");
        // Save the updated shift and employee
        shiftRepository.save(shift);
        userRepository.save(user);
        this.updateUserShiftsCache(user.getId());
        findShiftCacheService.cacheTheWholeFindShift(shift);

    }

    public void updateUserShiftsCache(Long userId) throws NoResourceFoundException {
        logger.info("🔄 Updating user shifts cache for user ID: {}", userId);
        // Fetch the latest shifts from DB
        List<SingleUserShiftData> updatedShifts = this.getAllShiftsByUserDatabase();
        List<SingleUserShiftDataCache> updatedShiftsCache = updatedShifts.stream()
                .map(SingleUserShiftData::mapToSingleUserShiftDataCache)
                .toList();
        logger.info("📦 Fetched {} shifts from the database for user ID: {}", updatedShifts.size(), userId);
        logger.info("📦 Cached {} shifts for user ID: {}", updatedShiftsCache.size(), userId);

        // Update Redis Cache
        UserShiftsCache userShiftsCache = new UserShiftsCache(userId, updatedShiftsCache, LocalDate.now());
        logger.info("📦 userShiftsCache: \n{}", userShiftsCache);
        logger.info("📦 Create userShiftsCache ");
        userShiftsCacheService.save(userShiftsCache);

        logger.info("✅ Cache updated for user ID: {}", userId);
    }

}
