package com.example.employeemanagement.redis;
import com.example.employeemanagement.dto.SingleUserShiftData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
@Data
@AllArgsConstructor
@RedisHash( value =  "usershifts", timeToLive = 1800)
public class UserShiftsCache implements Serializable {

    @Id
    private Long userId;
    private List<SingleUserShiftDataCache> shifts;
    private LocalDate cacheDate;

    @Override
    public String toString() {
        return "UserShiftsCache{" +
                "userId=" + userId +
                ", shifts=" + shifts +
                ", cacheDate=" + cacheDate +
                '}';
    }

}