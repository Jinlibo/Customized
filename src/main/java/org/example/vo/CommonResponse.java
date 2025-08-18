package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonResponse<T> implements Serializable {
    private Integer code; //响应码
    private String msg; //响应消息
    private T data; //响应对象

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder().code(200).msg("success").data(data).build();
    }
}