package org.example.util;

import com.alibaba.fastjson2.JSON;
import org.example.vo.CommonResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseUtils {
    public static void toJsonResponse(HttpServletResponse response, CommonResponse<?> commonResponse) throws IOException {
        String strResponse = JSON.toJSONString(commonResponse);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println(strResponse);
        writer.flush();
    }
}
