package org.wishfoundation.userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.userservice.exception.WishFoundationException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/log")
public class LogsConfig {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/push-logs-last-hour")
    public void ingestLogsInDb() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        calendar.add(Calendar.HOUR_OF_DAY, -1);

        Date oneHourBefore = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");
        String formattedDate = dateFormat.format(oneHourBefore);
        ListOperations<String, Object> listOps = redisTemplate.opsForList();

        //    long totalSize = listOps.size(formattedDate);

        List<Object> logRequestList = listOps.range(formattedDate, 0, -1);
        redisTemplate.delete(formattedDate);
    }

    @GetMapping("/date-time/{date-time}")
    public List<Object> viewLogsManualy(@PathVariable("date-time") String dateTime) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        List<Object> logs = listOps.range(dateTime, 0, -1);
        if (logs == null || logs.isEmpty()) {
            throw new WishFoundationException("Logs not found for dateTime: " + dateTime);
        }
        return logs;
    }


    @GetMapping("/remove/{date-time}")
    public void removeKeyLogs(@PathVariable("date-time") String dateTime) {
        redisTemplate.delete(dateTime);
    }
}
