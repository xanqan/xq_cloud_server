package com.xanqan.project.security.component;

import cn.hutool.json.JSONUtil;
import com.xanqan.project.security.model.Jwt;
import com.xanqan.project.security.model.UserSecurity;
import com.xanqan.project.security.util.JwtTokenUtil;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT登录授权过滤器
 *
 * @author xanqan
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private Jwt jwt;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ParameterRequestWrapper requestWrapper = new ParameterRequestWrapper(request);
        String authHeader = request.getHeader(jwt.getTokenHeader());
        if (authHeader != null && authHeader.startsWith(jwt.getTokenHead())) {
            String authToken = authHeader.substring(jwt.getTokenHead().length());
            String userName = jwtTokenUtil.getUserNameFromToken(authToken);

            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserSecurity userSecurity = (UserSecurity) userDetailsService.loadUserByUsername(userName);
                if (jwtTokenUtil.validateToken(authToken, userSecurity)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userSecurity, null, userSecurity.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 将用户信息放入自定义request.parameter
                    requestWrapper.addParameter("user", JSONUtil.toJsonStr(userSecurity.getUser()));
                }
            }
        }
        filterChain.doFilter(requestWrapper, response);
    }
}
