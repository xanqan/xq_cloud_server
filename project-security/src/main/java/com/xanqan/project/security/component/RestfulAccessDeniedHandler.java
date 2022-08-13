package com.xanqan.project.security.component;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义返回结果：没有相关权限
 *
 * @author xanqan
 */
@Component
@Slf4j
public class RestfulAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control","no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(JSONUtil.parse(ResultUtils.error(ResultCode.NO_AUTH, e.getMessage())));
        response.getWriter().flush();
        log.error("RestfulAccessDeniedHandler", e);
    }
}
