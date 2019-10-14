package com.ibyte.common.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

/**
 * 查询结果
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 *
 * @param <V>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("返回结果")
public class QueryResult<V> {
	
	@ApiModelProperty("返回内容")
	private List<V> content;

	@ApiModelProperty("分页位移量")
	private int offset;

	@ApiModelProperty("每页数目")
	private int pageSize;

	@ApiModelProperty("总数")
	private long totalSize;

	public static <T> QueryResult<T> empty() {
		QueryResult<T> result = new QueryResult<>();
		result.content = Collections.emptyList();
		return result;
	}

}
