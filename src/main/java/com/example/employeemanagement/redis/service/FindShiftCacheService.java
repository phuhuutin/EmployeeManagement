package com.example.employeemanagement.redis.service;

import com.example.employeemanagement.dto.FindShift;
import com.example.employeemanagement.entity.Shift;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.redis.UserCache;
import com.example.employeemanagement.redis.repository.EmployerCacheRepository;
import com.example.employeemanagement.redis.FindShiftCache;
import com.example.employeemanagement.redis.repository.FindShiftCacheRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
public class FindShiftCacheService {

    private FindShiftCacheRepository findShiftCacheRepository;
    private EmployerCacheRepository employerCacheRepository;
    private UserCacheService userCacheService;
    private RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(FindShiftCacheService.class);
    public void save(FindShiftCache findShiftCache) {
        findShiftCacheRepository.save(findShiftCache);
    }

    /**
     * ✅ Cache a shift in Redis (stores employer separately).
     */

    public FindShift cacheTheWholeFindShift(Shift shift) {
        // Fetch employer details from cache or DB
        FindShiftCache findShiftCache = shift.toFindShiftCache();
        //Client Request Response
        FindShift findShift = findShiftCache.toFindShift();
        if (employerCacheRepository.findById(shift.getEmployer().getId()).isEmpty()) {
            employerCacheRepository.save(shift.getEmployer().toEmployerCache());
        }

        findShift.setEmployer(shift.getEmployer().toEmployerCache());

        for (User user : shift.getEmployees()) {

            Long id = user.getId();
            logger.debug("Fetching employee ID: {}", id);
            UserCache userCache = userCacheService.findById(id).orElse(null);
            if (userCache == null) {
                logger.debug("User ID {} not found in cache, now caching", id);
                 userCacheService.save(user, UserCache.CACHE_TTL);
            }
            findShift.addEmployee(user.toUserCache());
        }

        UserCache postedBy = userCacheService.findById(shift.getPostedBy().getId()).orElse(null);
        if (postedBy == null) {
            userCacheService.save(shift.getPostedBy(), UserCache.CACHE_TTL);
        }
        findShift.setPostedBy(shift.getPostedBy().toUserCache());

        // ✅ Caching FindShiftCache
//        String key = FindShiftCache.CACHE_NAME + ":" + findShiftCache.getId();
//        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
//        hashOps.put(key, "data", findShiftCache);
        findShiftCacheRepository.save(findShiftCache);

        logger.info("✅ Cached shift with ID: {}", shift.getId());

        return findShift;
    }
    public void cacheFindShift(Shift shift) {
        // Fetch employer details from cache or DB
        if (employerCacheRepository.findById(shift.getEmployer().getId()).isEmpty()) {
            employerCacheRepository.save(shift.getEmployer().toEmployerCache());
        }

        // ✅ Convert and cache FindShift
        FindShiftCache findShiftCache = shift.toFindShiftCache();
        findShiftCacheRepository.save(findShiftCache);

        logger.info("✅ Cached shift with ID: {}", shift.getId());
    }

    public List<FindShiftCache> getAllShiftsCache(){
        List<FindShiftCache> cachedShifts = (List<FindShiftCache>) findShiftCacheRepository.findAll();
        System.out.println("📦 Retrieved " + cachedShifts.size() + " shifts from cache");
        return cachedShifts;
    }

    /**
     * ✅ Retrieve a shift from cache.
     */
    public Optional<FindShiftCache> getCachedShift(Long shiftId) {
        return findShiftCacheRepository.findById(shiftId);
    }

    /**
     * ✅ Remove a shift from cache.
     */
    public void removeShiftFromCache(Long shiftId) {
        findShiftCacheRepository.deleteById(shiftId);
        System.out.println("🗑 Removed shift from cache: " + shiftId);
    }
}
