package com.ibyte.common.core.service;

import com.ibyte.common.core.api.IApi;
import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.core.entity.IEntity;

import java.util.Optional;

/**
 * 服务接口
 *
 * @author ibyte
 *
 * @param <E>
 *            Entity实现类
 * @param <V>
 *            ViewObject实现类
 */
public interface IService<E extends IEntity, V extends IViewObject>
		extends IApi<V> {
	/**
	 * 新增
	 * 
	 * @param entity
	 */
	void add(E entity);

	/**
	 * 更新
	 * 
	 * @param entity
	 */
	void update(E entity);

	/**
	 * 删除
	 * 
	 * @param entity
	 */
	void delete(E entity);

	/**
	 * 获取延迟加载的Entity
	 * 
	 * @param id
	 * @return
	 */
	E getOne(String id);
	
	/**
	 * 获取数据总数
	 * 
	 * @return
	 */
	Long getTotal();

	/**
	 * 加载Entity
	 * 
	 * @param id
	 * @return
	 */
	Optional<E> findById(String id);

	/**
	 * 获取Entity的实现类
	 * 
	 * @return
	 */
	Class<E> getEntityClass();

	/**
	 * 获取ViewObject的实现类
	 * 
	 * @return
	 */
	Class<V> getViewObjectClass();
}
