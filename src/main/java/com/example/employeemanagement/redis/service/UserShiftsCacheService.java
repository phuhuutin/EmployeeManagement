package com.example.employeemanagement.redis.service;

import com.example.employeemanagement.redis.UserShiftsCache;
import com.example.employeemanagement.redis.repository.UserShiftsCacheRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Data
@AllArgsConstructor
@Service
public class UserShiftsCacheService {
    private final UserShiftsCacheRepository userShiftsCacheRepository;
    private RedisTemplate<String, Object> redisTemplate;


    public void save(UserShiftsCache userShiftsCache) {
        userShiftsCacheRepository.save(userShiftsCache);
    }

    public Optional<UserShiftsCache> findById(Long userId) {
        return Optional.of(userShiftsCacheRepository.findById(userId).orElseThrow());
    }

//    public void updateUserShiftsCache(Long userId) throws NoResourceFoundException {
//        // Fetch the latest shifts from DB
//        List<SingleUserShiftData> updatedShifts = shiftService.getAllShiftsByUser();
//        List<SingleUserShiftDataCache> updatedShiftsCache = updatedShifts.stream()
//                .map(SingleUserShiftData::mapToSingleUserShiftDataCache)
//                .toList();
//
//
//
//        // Update Redis Cache
//        UserShiftsCache userShiftsCache = new UserShiftsCache(userId, updatedShiftsCache, LocalDate.now());
//        userShiftsCacheRepository.save(userShiftsCache);
//
//        System.out.println("✅ Cache updated for user ID: " + userId);
//    }

    public void delete(Long userId) {
        userShiftsCacheRepository.deleteById(userId);
    }
}
