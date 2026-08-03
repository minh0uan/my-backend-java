package com.demo.my_backend_java.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GpsValidationService {
    
    private final StringRedisTemplate redisTemplate;

    public GpsValidationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void validateAndSaveLocation(String username, double currentLat, double currentLong) {
        String userKey = "user_loca:" + username;
        String lastLoca = redisTemplate.opsForValue().get(userKey);

        long currentTime = Instant.now().getEpochSecond();

        if (lastLoca != null) {
            String[] part = lastLoca.split(",");
            if (part.length == 3) {
                try {
                    double lastLat = Double.parseDouble(part[0]);
                    double lastLong = Double.parseDouble(part[1]);
                    long lastTime = Long.parseLong(part[2]);

                    double distance = calculateDistance(lastLat, lastLong, currentLat, currentLong);
                    long timeInBetween = currentTime - lastTime;

                    if (timeInBetween > 0) {
                        //this speed is in m/s
                        double speed = distance / timeInBetween;
                    
                        if (speed > 30) {
                            throw new SecurityException("Impossible velocity: " + speed + " m/s");
                        }
                    }
                } catch (NumberFormatException e){
                    //ignore if catch a numeric error 
                }
            }
        }

        String newValue = currentLat + "," + currentLong + "," + currentTime;
        redisTemplate.opsForValue().set(userKey, newValue, Duration.ofHours(1));

    }

    //haversine - đường chim bay 
    //use the find calculate if the 
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
