package com.xanqan.project.model.vo;

import com.xanqan.project.model.dto.File;
import com.xanqan.project.model.dto.Share;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 首页数据
 *
 * @author xanqan
 */
@Data
@AllArgsConstructor
public class Home {

    /**
     * 用户信息
     */
    private UserInfo user;

    /**
     * 根目录文件（首页）
     */
    private List<File> files;

    /**
     * 分类链接
     */
    private List<Share> shares;

}
