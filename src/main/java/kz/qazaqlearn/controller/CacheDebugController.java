package kz.qazaqlearn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@RestController
@Profile("dev")
@RequestMapping("/api/debug/cache")
public class CacheDebugController {

    private static final Logger logger = LoggerFactory.getLogger(CacheDebugController.class);

    private final CacheManager cacheManager;

    public CacheDebugController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> pingCache() {
        logger.info("Testing Redis cache connectivity");

        Map<String, Object> result = new HashMap<>();
        result.put("cacheManagerType", cacheManager.getClass().getSimpleName());

        try {
            // Test writing to cache
            Cache testCache = cacheManager.getCache("test");
            if (testCache != null) {
                testCache.put("testKey", "testValue");
                Object retrievedValue = testCache.get("testKey");
                result.put("writeTest", "SUCCESS");
                result.put("readTest", retrievedValue != null ? "SUCCESS" : "FAILED");
                result.put("retrievedValue", retrievedValue);
                logger.info("Cache test successful: wrote and read test value");
            } else {
                result.put("cacheTest", "FAILED - Cache 'test' is null");
                logger.warn("Cache 'test' is null");
            }

            // Check publishedCourses cache
            Cache publishedCache = cacheManager.getCache("publishedCourses");
            if (publishedCache != null) {
                result.put("publishedCoursesCache", "EXISTS");
                logger.info("publishedCourses cache exists");
            } else {
                result.put("publishedCoursesCache", "NOT FOUND");
                logger.warn("publishedCourses cache not found");
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
            logger.error("Cache test failed", e);
        }

        return ResponseEntity.ok(result);
    }
}