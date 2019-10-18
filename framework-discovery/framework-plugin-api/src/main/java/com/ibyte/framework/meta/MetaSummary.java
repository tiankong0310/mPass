package com.ibyte.framework.meta;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: <设计摘要信息>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
@Getter
@Setter
@ToString
public class MetaSummary {
    private String id;

    private String label;

    private String messageKey;

    private String md5;

    private String module;
}
