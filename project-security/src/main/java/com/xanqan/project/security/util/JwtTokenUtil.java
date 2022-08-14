package com.xanqan.project.security.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xanqan.project.common.ResultCode;
import com.xanqan.project.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtToken生成的工具类
 * JWT token的格式：header.payload.signature
 * header的格式（算法、token的类型）：
 * {"alg": "HS512","typ": "JWT"}
 * payload的格式（用户id、创建时间、生成时间）：
 * {"sub":"id","created":1489079981393,"exp":1489684781}
 * signature的生成算法：
 ** HASH512(base64UrlEncode(header) + "." +base64UrlEncode(payload),secret)
 *
 * @author xanqan
 */
@Component
public class JwtTokenUtil {

    /** 用户id_key */
    private static final String CLAIM_KEY_USERNAME = "sub";
    /** 创建时间_key */
    private static final String CLAIM_KEY_CREATED = "created";
    /** token负载开头 */
    private static final String TOKEN_HEAD = "Bearer";
    /** token盐值 */
    private static final String SECRET = "asdfgh";
    /** token的超期限时间 */
    private static final int EXPIRATION = 60 * 60 * 24;
    /** token的不刷新时间 */
    private static final int JUST_TIME = 30 * 60;

    /**
     * 根据用户信息生成token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * 根据负责生成JWT的token
     */
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 生成token的过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + EXPIRATION * 1000);
    }

    /**
     * 从token中获取登录用户账号
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "JWT格式验证失败:" + token);
        }
        return username;
    }

    /**
     * 从token中获取JWT中的负载
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAMS_ERROR, "JWT格式验证失败:" + token);
        }
        return claims;
    }

    /**
     * 验证token是否还有效
     *
     * @param token       客户端传入的token
     * @param userDetails 从数据库中查询出来的用户信息
     * @return true = 失效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * 判断token是否已经失效
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 当原来的token没过期时是可以刷新的
     *
     * @param oldToken 带tokenHead的token
     * @return token
     */
    public String refreshHeadToken(String oldToken) {
        if(StrUtil.isEmpty(oldToken)){
            throw new BusinessException(ResultCode.PARAMS_ERROR, "token为空");
        }
        String token = oldToken.substring(TOKEN_HEAD.length());
        if(StrUtil.isEmpty(token)){
            throw new BusinessException(ResultCode.PARAMS_ERROR, "token只有开头");
        }
        Claims claims = getClaimsFromToken(token);
        //如果token已经过期，不支持刷新
        if(isTokenExpired(token)){
            throw new BusinessException(ResultCode.NOT_LOGIN, "过期");
        }
        //如果token在30分钟之内刚刷新过，返回原token
        if(tokenRefreshJustBefore(claims)){
            return token;
        }else{
            claims.put(CLAIM_KEY_CREATED, new Date());
            return generateToken(claims);
        }
    }

    /**
     * 判断token在指定时间内是否刚刚刷新过
     * @param claims token负载
     * @return ture = 刚刷新过
     */
    private boolean tokenRefreshJustBefore(Claims claims) {
        Date created = claims.get(CLAIM_KEY_CREATED, Date.class);
        Date refreshDate = new Date();
        // 刷新时间在创建时间的指定时间内
        return refreshDate.after(created) && refreshDate.before(DateUtil.offsetSecond(created, JUST_TIME));
    }
}
