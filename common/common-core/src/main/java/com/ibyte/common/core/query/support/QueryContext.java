package com.ibyte.common.core.query.support;
import com.ibyte.common.core.constant.QueryConstant.Operator;
import com.ibyte.common.core.dto.QueryRequest;
import com.ibyte.framework.meta.MetaEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 * 查询模板上下文
 *
 * @author li.Shangzhi
 * @Date: 2019-10-28
 */
public interface QueryContext {
	/**
	 * 获取元数据
	 * 
	 * @return
	 */
	MetaEntity getMeta();

	/**
	 * 查询请求
	 * 
	 * @return
	 */
	QueryRequest getRequest();

	/**
	 * 查询条件构造器
	 * 
	 * @return
	 */
	CriteriaBuilder getBuilder();

	/**
	 * 获取一个字段的表达式
	 * 
	 * @param property
	 * @return
	 */
	Path<?> simplePath(String property);

	/**
	 * 获取一个字段的表达式
	 * 
	 * @param property
	 * @return
	 */
	Path<?> leftJoinPath(String property);

	/**
	 * 获取一个字段的表达式
	 * 
	 * @param property
	 * @return
	 */
	Path<?> innerJoinPath(String property);

	/**
	 * 构造查询条件
	 * 
	 * @param property
	 * @param opt
	 * @param value
	 * @return
	 */
	Predicate toPredicate(String property, Operator opt, Object value);

	/**
	 * 标记当前查询上下文处于and的查询，可以用inner join提升性能
	 * 
	 * @return
	 */
	boolean isAndCriteria();

	/**
	 * 当出现OR的查询是需要设置环境参数
	 * 
	 * @param andCriteria
	 */
	void setAndCriteria(boolean andCriteria);
}
