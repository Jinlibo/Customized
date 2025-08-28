package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

@SpringBootTest
public class CustomizedApplicationTest {
    @Resource
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordEncoder() {
        System.out.println(passwordEncoder.encode("123456"));
    }

}
