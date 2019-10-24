package com.ibyte.core.repository;

import com.ibyte.core.entity.IEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * 仓库层接口
 *
 * @author li.shangzhi
 *
 */
@NoRepositoryBean
public interface IRepository<E extends IEntity>
		extends CrudRepository<E, String>, JpaSpecificationExecutor<E> {
	/**
	 * 获取延迟加载的Entity
	 * 
	 * @param id
	 * @return
	 */
	E getOne(String id);
}
