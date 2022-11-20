package com.xanqan.project.model.vo;

import lombok.Data;

/**
 * 分享链接信息接受类
 *
 * @author xanqan
 */
@Data
public class ShareInfo {

    private String password;

    private Integer expire;

    private String name;

    private String path;
}
