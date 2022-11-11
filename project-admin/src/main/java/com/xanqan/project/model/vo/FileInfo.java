package com.xanqan.project.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 文件信息接收类
 *
 * @author xanqan
 */
@Data
public class FileInfo {

    private String path;

    private String newPath;

    private String name;

    private String newName;

    private List<FileChunk> fileChunks;
}
