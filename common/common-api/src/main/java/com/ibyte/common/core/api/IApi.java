package com.ibyte.common.core.api;

import com.ibyte.common.core.dto.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

/**
 * @Description: <API服务接口>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public interface IApi<V extends IViewObject> {
    /**
     * 初始化VO
     *
     * @param vo
     * @return
     */
    @PostMapping("init")
    V init(@RequestBody Optional<V> vo);

    /**
     * 新增
     *
     * @param vo
     */
    @PostMapping("add")
    void add(@RequestBody V vo);

    /**
     * 更新
     *
     * @param vo
     */
    @PostMapping("update")
    void update(@RequestBody V vo);

    /**
     * 删除
     *
     * @param id
     */
    @PostMapping("delete")
    void delete(@RequestBody IdVO id);

    /**
     * 删除
     *
     * @param ids
     */
    @PostMapping("deleteAll")
    void deleteAll(@RequestBody IdsDTO ids);

    /**
     * 加载VO和相关机制
     *
     * @param id
     * @return
     */
    @PostMapping("loadById")
    Optional<V> loadById(@RequestBody IdVO id);

    /**
     * 查询
     *
     * @param request
     * @return
     */
    @PostMapping("findAll")
    QueryResult<V> findAll(@RequestBody QueryRequest request);

    /**
     * 获取Entity的类名
     *
     * @return
     */
    @PostMapping("getEntityName")
    String getEntityName();
}
