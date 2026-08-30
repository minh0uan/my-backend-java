package com.demo.my_backend_java.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
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
        long currentTime = Instant.now().getEpochSecond();
        String newValue = currentLat + "," + currentLong + "," + currentTime;

        List<Object> results = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                //cast to String-based operations for the transaction block
                RedisOperations<String, String> stringOps = (RedisOperations<String, String>) operations;

                stringOps.watch(userKey);

                String lastLocation = stringOps.opsForValue().get(userKey);

                if (lastLocation != null) {
                    String[] part = lastLocation.split(",");
                    if (part.length == 3) {
                        try {
                            double lastLat = Double.parseDouble(part[0]);
                            double lastLong = Double.parseDouble(part[1]);
                            long lastTime = Long.parseLong(part[2]);
                        
                            double distance = calculateDistance(lastLat, lastLong, currentLat, currentLong);
                            long timeInBetween = currentTime - lastTime;
                        
                            //block instant teleportation
                            if (timeInBetween == 0 && distance > 10) {
                                throw new SecurityException("Impossible velocity: Teleportation Detected");
                            } 
                        
                            if (timeInBetween > 0) {
                                //speed is in m/s
                                double speed = distance / timeInBetween;
                            
                                //30km/h = 8.3 m/s
                                //8 is test speed TODO
                                if (speed > 30) {
                                    throw new SecurityException("Impossible velocity: " + speed + " m/s");
                                }
                            }
                        } catch (NumberFormatException e){
                            //ignore if catch a numeric error
                        }
                    }
                }

                //start transaction
                stringOps.multi();
                stringOps.opsForValue().set(userKey, newValue, Duration.ofHours(1));

                //execute transaction
                return stringOps.exec();
            }
        });

        //uf another thread modified the key after our watch, results will be empty
        if (results == null || results.isEmpty()) {
            throw new SecurityException("Concurrent Location Spam");
        }
    }

    // haversine - đường chim bay 
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

    public boolean isWithinInteractionRadius(Double userLat, Double userLng, Double markLat, Double markLng) {
        final int EARTH_RADIUS_METERS = 6371000;

        double latDistance = Math.toRadians(markLat - userLat);
        double lonDistance = Math.toRadians(markLng - userLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(markLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (EARTH_RADIUS_METERS * c) <= 50.0;
    }
}