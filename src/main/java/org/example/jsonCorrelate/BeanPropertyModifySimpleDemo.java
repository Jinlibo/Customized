package org.example.jsonCorrelate;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;

public class BeanPropertyModifySimpleDemo extends BeanSerializerModifier {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            // 如果序列化器为 TranslationHandler 的话 将 Null 值也交给他处理
            if (writer.getSerializer() instanceof CustomBeanSerializer serializer) {
                writer.assignNullSerializer(serializer);
            }
        }
        return beanProperties;
    }
}
