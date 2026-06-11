package dk.tinker.designer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
@ActiveProfiles("test")
@Import(DesignerApplicationTests.MongoContainerConfig.class)
class DesignerApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class MongoContainerConfig {

        @Bean
        @ServiceConnection
        MongoDBContainer mongoContainer() {
            return new MongoDBContainer("mongo:8");
        }
    }
}
