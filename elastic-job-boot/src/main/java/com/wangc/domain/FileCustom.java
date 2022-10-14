package com.wangc.domain;

import lombok.Data;

/**
 * @author
 * @Description:
 * @date 2022/10/14 15:53
 */
@Data
public class FileCustom {

    private Integer id;

    private String name;

    private String content;

    private String type;

    private Integer backedUp;

}
