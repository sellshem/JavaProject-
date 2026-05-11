package kz.qazaqlearn;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public abstract class IntegrationTestBase {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("qazaq_learn")
            .withUsername("qazaq_user")
            .withPassword("qazaq_password");

    @Container
    public static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Container
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("jwt.secret", () -> "change-me-in-production-change-me");
        registry.add("jwt.expiration-ms", () -> "86400000");
        registry.add("encryption.key", () -> "test-encryption-key-32-char");
    }

    @BeforeEach
    void cleanRedis() {
        // Clean any rate limiting keys before each test
        Set<String> keys = redisTemplate.keys("rate_limit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // Clean failed login attempts
        Set<String> failedKeys = redisTemplate.keys("failed_login:*");
        if (failedKeys != null && !failedKeys.isEmpty()) {
            redisTemplate.delete(failedKeys);
        }
    }
}
