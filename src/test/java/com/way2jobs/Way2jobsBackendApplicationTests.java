package com.way2jobs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.sql.init.mode=never",
                "spring.jpa.hibernate.ddl-auto=none"
        }
)
class Way2jobsBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}