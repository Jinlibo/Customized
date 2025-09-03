package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.example.entity.SysUser;
import org.example.jsonCorrelate.BeanPropertyModifySimpleDemo;
import org.example.jsonCorrelate.CustomJsonSerDeSer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class JsonTest {
    @Test
    public void testTwiceSerializeJson() throws JsonProcessingException {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1002);
        sysUser.setUsername("zhangsan");
        sysUser.setPassword("abcd");
        sysUser.setSex("男");
        sysUser.setAddress("chinese");
        sysUser.setEnabled(1);

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(sysUser);
        System.out.println(value);
        System.out.println(objectMapper.writeValueAsString(value));
        String abcd = objectMapper.writeValueAsString("abcd");
        System.out.println(abcd);
        System.out.println(objectMapper.writeValueAsString(abcd));
    }

    @Test
    public void testCustomSeAndDesJson() throws IOException {
        InnerUser innerUser = new InnerUser();
        innerUser.setName("zhangsan");
        innerUser.setSex("男");
        innerUser.setAge(12);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializerFactory(
                objectMapper.getSerializerFactory()
                        .withSerializerModifier(new BeanPropertyModifySimpleDemo()));
        String jsonString = objectMapper.writeValueAsString(innerUser);
        System.out.println(jsonString);
        InnerUser deserializedUser = objectMapper.readValue(jsonString, InnerUser.class);
        System.out.println(deserializedUser);
    }

    @Data
    static class InnerUser {
        @CustomJsonSerDeSer
        private String name;
        private String sex;
        private Integer age;
    }
}
