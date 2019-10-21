package com.ibyte.framework.config.spi;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.dto.Response;
import com.ibyte.framework.meta.Meta;
import com.ibyte.framework.meta.MetaApplication;
import com.ibyte.framework.meta.MetaSummary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

/**
 * @Description: <元数据的数据获取,供前端页面使用>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
public interface MetaBrowseController {

    /**
     * 应用列表
     *
     * @return
     */
    @PostMapping("applications")
    default Response<List<MetaApplication>> applications() {
        return Response.ok(Meta.getApplications());
    }

    /**
     * 模块列表
     *
     * @return
     */
    @PostMapping("modules")
    default Response<List<MetaSummary>> modules() {
        return Response.ok(Meta.getModules());
    }

    /**
     * Entity列表，可指定模块
     *
     * @param json
     *            {module:''}
     * @return
     */
    @PostMapping("entities")
    default Response<List<MetaSummary>>
    entities(@RequestBody Optional<JSONObject> json) {
        String module = json.isPresent() ? json.get().getString("module")
                : null;
        return Response.ok(Meta.getEntities(module));
    }

    /**
     * Entity详情
     *
     * @param json
     *            {id:''}
     * @return
     */
    @PostMapping("entity")
    default Response<Object> entity(@RequestBody JSONObject json) {
        String id = json.getString("id");
        return Response.ok(Meta.getEntity(id));
    }
}
