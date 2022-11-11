package com.xanqan.project.model.vo;

import lombok.Data;

/**
 * 分片信息类
 *
 * @author xanqan
 */
@Data
public class FileChunk {
    /**
     * 分片编号
     */
    private Integer id;

    private String md5;
}
