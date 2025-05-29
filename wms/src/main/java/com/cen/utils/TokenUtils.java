package com.cen.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cen.entity.User;
import org.springframework.stereotype.Component;

import java.util.Date;

public class TokenUtils {

    /*
     * 生成token
     * */
    public static String getToken(User user){
        return JWT.create().withAudience(user.getId()+"") // 将 user id 保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(), 5)) // 5小时后token过期
                .sign(Algorithm.HMAC256(user.getPassword())); // 以 password 作为 token 的密钥
    }
}