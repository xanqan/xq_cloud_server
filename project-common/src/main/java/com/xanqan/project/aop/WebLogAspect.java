package com.xanqan.project.aop;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.model.WebLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 统一接口处理切面
 *
 * @author xanqan
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class WebLogAspect {

    @Pointcut("execution(public * com.xanqan.project.controller.*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();

        //记录请求信息
        WebLog webLog = new WebLog();
        webLog.setUsername(request.getRemoteUser());
        webLog.setIp(request.getRemoteAddr());
        webLog.setMethod(request.getMethod());
        webLog.setUri(request.getRequestURI());
        log.info("{}", JSONUtil.parse(webLog));

        //前面是前置通知，后面是后置通知
        Object result = joinPoint.proceed();

        WebLog webLog2 = new WebLog();
        long endTime = System.currentTimeMillis();
        webLog2.setSpendTime((int) (endTime - startTime));
        webLog2.setStartTime(startTime);
        log.info("{}", JSONUtil.parse(webLog2));
        return result;
    }
}
