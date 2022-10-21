package com.xanqan.project.model.vo;

import com.xanqan.project.model.domain.User;
import lombok.Data;

/**
 * user 脱敏
 *
 * @author xanqan
 */
@Data
public class UserInfo {
    /**
     * 自增id
     */
    private Integer id;

    /**
     * 登录用户名
     */
    private String name;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 储存空间大小（GB） 0-不限制
     */
    private Long sizeMax;

    /**
     * 已使用大小（byte）
     */
    private Long sizeUse;

    public UserInfo(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.nickName = user.getNickName();
        this.avatar = user.getNickName();
        this.sizeMax = user.getSizeMax();
        this.sizeUse = user.getSizeUse();
    }
}
