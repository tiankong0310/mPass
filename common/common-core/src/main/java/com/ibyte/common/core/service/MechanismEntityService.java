package com.ibyte.common.core.service;

import com.ibyte.common.core.dto.QueryRequest;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.query.IteratorQueryTemplate;
import com.ibyte.common.core.query.PageQueryTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaDelete;
import java.util.Iterator;
import java.util.List;

/**
 * 机制类Entity常用操作
 * 
 * @author li.shangzhi
 */
@Service
public class MechanismEntityService {
	@Autowired
	private EntityManager entityManager;

	/**
	 * 根据主文档查找，fdEntityKey可为空，结果最多显示前1000条
	 */
	public <E extends IEntity> List<E> findByEntity(Class<E> clazz,
			String fdEntityName, String fdEntityId, String fdEntityKey) {
		return new PageQueryTemplate<>(entityManager, clazz, clazz)
				.findAll(buildRequest(fdEntityName, fdEntityId, fdEntityKey))
				.getContent();
	}

	/**
	 * 根据主文档删除，fdEntityKey可为空，可触发相关事件
	 */
	public <E extends IEntity> void deleteByEntity(Class<E> clazz,
			String fdEntityName, String fdEntityId, String fdEntityKey,
			IService<E, ?> service) {
		IteratorQueryTemplate<E> template = new IteratorQueryTemplate<>(
				entityManager, clazz).setFetchSize(1).setAutoFlush(false);
		Iterator<E> iterator = template
				.iterator(buildRequest(fdEntityName, fdEntityId, fdEntityKey));
		while (iterator.hasNext()) {
			service.delete(iterator.next());
		}
	}

	/**
	 * 根据主文档删除，fdEntityKey可为空，不触发相关事件
	 */
	public <E extends IEntity> void deleteByEntity(Class<E> clazz,
			String fdEntityName, String fdEntityId, String fdEntityKey) {
		IteratorQueryTemplate<E> template = new IteratorQueryTemplate<>(
				entityManager, clazz).setFetchSize(1).setAutoFlush(false);
		IteratorQueryTemplate<E>.QueryIterator iterator = template
				.iterator(buildRequest(fdEntityName, fdEntityId, fdEntityKey));
		if (iterator.isEmpty()) {
			return;
		}
		CriteriaDelete<E> query = entityManager.getCriteriaBuilder()
				.createCriteriaDelete(clazz);
		query.where(query.from(clazz).get("fdId").in(iterator.getIds()));
		entityManager.createQuery(query).executeUpdate();
	}

	/**
	 * 构造查询请求
	 */
	private QueryRequest buildRequest(String fdEntityName, String fdEntityId,
			String fdEntityKey) {
		QueryRequest request = new QueryRequest();
		request.setCount(false);
		request.setPageSize(QueryRequest.MAX_PAGESIZE);
		request.addCondition("fdEntityName", fdEntityName);
		if (StringUtils.isNotBlank(fdEntityId)) {
			request.addCondition("fdEntityId", fdEntityId);
		}
		if (StringUtils.isNotBlank(fdEntityKey)) {
			request.addCondition("fdEntityKey", fdEntityKey);
		}
		return request;
	}
}
