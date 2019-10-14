package com.ibyte.common.core.api;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.core.dto.IdVO;
import com.ibyte.common.core.dto.IdsDTO;
import com.ibyte.common.core.dto.QueryRequest;
import com.ibyte.common.core.dto.QueryResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

/**
 * API服务的另外一个版本，跟IApi的功能一样，只是所有的VO都改成了JSON。<br>
 * 使用方法：IGreenApi api = Plugin.getApi(entity.getApiName(), IGreenApi.class);
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public interface IGreenApi {

    /**
     * 初始化VO
     *
     * @param vo
     * @return
     */
    @PostMapping("init")
    JSONObject init(@RequestBody Optional<JSONObject> vo);

    /**
     * 新增
     *
     * @param vo
     */
    @PostMapping("add")
    void add(@RequestBody JSONObject vo);

    /**
     * 更新
     *
     * @param vo
     */
    @PostMapping("update")
    void update(@RequestBody JSONObject vo);

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
     * 获取数据总条数
     *
     * @return
     */
    @PostMapping("getTotal")
    Long getTotal();

    /**
     * 加载VO和相关机制
     *
     * @param id
     * @return
     */
    @PostMapping("loadById")
    Optional<JSONObject> loadById(@RequestBody IdVO id);

    /**
     * 查询
     *
     * @param request
     * @return
     */
    @PostMapping("findAll")
    QueryResult<JSONObject> findAll(@RequestBody QueryRequest request);

    /**
     * 获取Entity的类名
     *
     * @return
     */
    @PostMapping("getEntityName")
    String getEntityName();
}
