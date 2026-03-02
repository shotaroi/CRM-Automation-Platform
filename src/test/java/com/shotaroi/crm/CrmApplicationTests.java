package com.shotaroi.crm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CrmApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context loads, Flyway runs, and DB connection works
    }
}
