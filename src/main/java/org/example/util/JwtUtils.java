package org.example.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtils {
    @Value("${my.secretKey:abcd}")
    private String secret; //密钥

    public String createJwt(String userInfo) {
        Date issDate = new Date(); //签发时间时间
        //头部
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("alg", "HS256");
        headerClaims.put("typ", "JWT");
        return JWT.create().withHeader(headerClaims)
                .withIssuer("thomas") //设置签发人
                .withIssuedAt(issDate) //签发时间
                .withClaim("user_info", userInfo) //自定义声明 放入登录用户信息
                .sign(Algorithm.HMAC256(secret)); //使用HS256进行签名，使用secret作为密钥
    }

    public boolean verifyToken(String jwtToken) {
        //创建校验器
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
            //校验token
            DecodedJWT decodedJwt = jwtVerifier.verify(jwtToken);
            log.info("token验证正确");
            return true;
        } catch (Exception e) {
            log.error("token验证不正确！！！");
            return false;
        }
    }

    /**
     * 从jwt的payload里获取声明，获取的用户的信息
     *
     * @param jwt
     * @return
     */
    public String getUserInfoFromToken(String jwt) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(jwt);
            return decodedJWT.getClaim("user_info").asString();
        } catch (IllegalArgumentException e) {
            return "";
        } catch (JWTVerificationException e) {
            return "";
        }
    }

}
