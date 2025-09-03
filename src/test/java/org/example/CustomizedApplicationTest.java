package org.example;

import org.example.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

@SpringBootTest
public class CustomizedApplicationTest {
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testPasswordEncoder() {
        System.out.println(passwordEncoder.encode("123456"));
    }

    @Test
    public void testRedis() {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(123);
        sysUser.setUsername("张三");
        sysUser.setPassword("asbcd");
        sysUser.setSex("female");
        sysUser.setAddress("陕西");
        sysUser.setEnabled(2);
        redisTemplate.opsForValue().set("test", sysUser);
        SysUser test = (SysUser) redisTemplate.opsForValue().get("test");
        System.out.println(test);
    }

}
