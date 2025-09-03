package org.example.jsonCorrelate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BeanSerPropertyModifyConfig implements InitializingBean {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper.setSerializerFactory(
                objectMapper.getSerializerFactory()
                        .withSerializerModifier(new BeanPropertyModifySimpleDemo()));
    }
}
