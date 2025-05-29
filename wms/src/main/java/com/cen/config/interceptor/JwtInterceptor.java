package com.cen.config.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.cen.common.Constants;
import com.cen.entity.User;
import com.cen.exception.ServiceException;
import com.cen.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        String uri = request.getRequestURI();
//        if(uri.equals("/error")){
//            throw new ServiceException(Constants.CODE_400, "参数错误");
//        }
        //如果不是映射到方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        // 执行认证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException(Constants.CODE_401, "登录失效，请重新登录");
        }
        // 获取 token 中的userId
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (Exception e) {
            String errMsg = "验证失败，请重新登录";
            throw new ServiceException(Constants.CODE_401, errMsg);
        }
        //根据token中的userid查询数据库
        User byId = userService.getById(userId);
        if (byId == null) {
            throw new ServiceException(Constants.CODE_401,"用户不存在，请重新登录");
        }
        //验证token
        try {
            // 用户密码加签验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(byId.getPassword())).build();
            jwtVerifier.verify(token); // 验证token
        } catch (JWTVerificationException e) {
            throw new ServiceException(Constants.CODE_401, "验证失败，请重新登录");
        }
        return true;
    }
}
