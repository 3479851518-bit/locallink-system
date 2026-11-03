package com.locallink;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 集成测试基类
 * 所有需要启动Spring容器的测试都继承此类
 */
@SpringBootTest(
    properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {
    RabbitAutoConfiguration.class
})
public abstract class BaseIntegrationTest {
}
