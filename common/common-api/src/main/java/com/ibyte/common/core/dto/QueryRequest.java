package com.ibyte.common.core.dto;

import com.ibyte.common.core.constant.QueryConstant.Direction;
import com.ibyte.common.core.constant.QueryConstant.Operator;
import com.ibyte.common.util.StringHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询请求信息
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
@Setter
@Getter
@ToString
public class QueryRequest {
	/** 每页最大条目数 */
	public static final int MAX_PAGESIZE = 1000;
	/** 每页默认条目数 */
	public static final int DEFAULT_PAGESIZE = 20;

	/** 返回列 */
	private List<String> columns;

	/** 查询条件 */
	private Map<String, Object> conditions;

	/** 排序列 */
	private Map<String, String> sorts;

	/** 过滤器 */
	private Map<String, Object> filters;

	/** 获取条目数 */
	private boolean count = true;

	/** 获取内容 */
	private boolean content = true;

	/** 从第?条开始查询，起始值为0 */
	private int offset = 0;

	/** 每页?条 */
	private int pageSize = DEFAULT_PAGESIZE;

	/**
	 * 格式化
	 */
	public QueryRequest format() {
		if (offset < 0) {
			offset = 0;
		}
		if (pageSize < 1) {
			pageSize = DEFAULT_PAGESIZE;
		} else if (pageSize > MAX_PAGESIZE) {
			pageSize = MAX_PAGESIZE;
		}
		return this;
	}

	/**
	 * 追加返回列
	 */
	public QueryRequest addColumn(String... columns) {
		if (this.columns == null) {
			this.columns = new ArrayList<>();
		}
		for (String column : columns) {
			if (!this.columns.contains(column)) {
				this.columns.add(column);
			}
		}
		return this;
	}

	/**
	 * 添加EQ条件
	 */
	public QueryRequest addCondition(String column, Object value) {
		return addCondition(column, Operator.eq, value);
	}

	/**
	 * 添加条件
	 */
	@SuppressWarnings("unchecked")
	public QueryRequest addCondition(String column, Operator opt,
			Object value) {
		Map<Object, Object> map;
		if (this.conditions == null) {
			this.conditions = new LinkedHashMap<>();
			map = new LinkedHashMap<>(16);
			this.conditions.put(column, map);
		} else {
			Object val = this.conditions.get(column);
			if (val == null) {
				map = new LinkedHashMap<>();
				this.conditions.put(column, map);
			} else if (val instanceof Map) {
				map = (Map<Object, Object>) val;
			} else {
				map = new LinkedHashMap<>(16);
				this.conditions.put(column, map);
				map.put(Operator.eq, val);
			}
		}
		map.put(opt.getValue(), value);
		return this;
	}

	/**
	 * 添加排序
	 */
	public QueryRequest addSort(String column, Direction direction) {
		if (this.sorts == null) {
			this.sorts = new LinkedHashMap<>();
			this.sorts.put(column, direction.name());
		} else {
			if (!this.sorts.containsKey(column)) {
				this.sorts.put(column, direction.name());
			}
		}
		return this;
	}

	/**
	 * 重置过滤器
	 */
	public QueryRequest resetFilter() {
		if (this.filters != null) {
			this.filters.clear();
		}
		return this;
	}

	/**
	 * 添加过滤器
	 */
    public QueryRequest addFilter(String filterName, Object filterParam) {
        if (this.filters == null) {
            this.filters = new LinkedHashMap<>();
        }
        Object temp = this.filters.get(filterName);
        if (temp instanceof String) {
            this.filters.put(filterName, StringHelper.join(temp, ";", filterParam));
        } else {
            this.filters.put(filterName, filterParam);
        }
        return this;
    }
}
