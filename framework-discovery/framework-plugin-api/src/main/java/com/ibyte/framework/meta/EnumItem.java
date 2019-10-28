package com.ibyte.framework.meta;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: <元数据>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-28
 */
@Getter
@Setter
@ToString
public class EnumItem {

    private String value;

    private String messageKey;

    private String label;
}
