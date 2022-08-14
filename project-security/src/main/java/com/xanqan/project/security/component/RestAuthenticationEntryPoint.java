package com.xanqan.project.security.component;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.common.ResultUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义返回结果：暂未登录或token已经过期
 *
 * @author xanqan
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control","no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(JSONUtil.parse(ResultUtils.error(ResultCode.NOT_LOGIN, authException.getMessage())));
        response.getWriter().flush();

    }
}
