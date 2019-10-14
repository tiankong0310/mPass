package com.ibyte.common.core.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * @Description: <数据对象>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public interface IData {

    /**
     * 读-主键
     *
     * @return
     */
    String getFdId();

    /**
     * 写-主键
     *
     * @param fdId
     */
    void setFdId(String fdId);

    /**
     * 读-机制类数据
     *
     * @return
     */
    Map<String, Object> getMechanisms();

    /**
     * 写-机制类数据
     *
     * @param mechanisms
     */
    void setMechanisms(Map<String, Object> mechanisms);

    /**
     * 读-动态数据
     *
     * @return
     */
    Map<String, Object> getDynamicProps();

    /**
     * 写-动态属性
     *
     * @param dynamicProps
     */
    void setDynamicProps(Map<String, Object> dynamicProps);

    /**
     * 读-固定的扩展属性
     *
     * @return
     */
    @JsonIgnore
    Map<String, Object> getExtendProps();
}
