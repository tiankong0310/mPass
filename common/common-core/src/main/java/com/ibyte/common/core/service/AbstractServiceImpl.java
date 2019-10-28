package com.ibyte.common.core.service;

import com.ibyte.common.core.data.handler.FieldHandlerManager;
import com.ibyte.common.core.dto.*;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.event.*;
import com.ibyte.common.core.mapper.EntityToVoMapper;
import com.ibyte.common.core.mapper.VoToEntityMapper;
import com.ibyte.common.core.repository.IRepository;
import com.ibyte.common.core.validation.ValidationUtil;
import com.ibyte.common.exception.NoRecordException;
import com.ibyte.common.exception.RecordExistException;
import com.ibyte.common.util.IDGenerator;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.framework.meta.Meta;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.validation.groups.Default;
import java.util.Optional;

/**
 * 服务的基类
 *
 * @author li.shangzhi
 *
 * @param <R>
 *            Repository实现类
 * @param <E>
 *            Entity实现类
 */
@Transactional(readOnly = true, rollbackFor = {})
public abstract class AbstractServiceImpl<R extends IRepository<E>, E extends IEntity, V extends IViewObject>
		implements IService<E, V>, ApplicationContextAware {
	// ==================== 全局变量 ====================

	@Autowired
	protected R repository;
	@Autowired
	protected EntityManager entityManager;
	protected Class<E> entityClass;
	protected Class<V> viewObjectClass;
	protected ApplicationContext applicationContext;

	// ==================== 推荐重载 ====================
	/** Entity初始化 */
	protected void doInit(E entity) {
		FieldHandlerManager.doInit(entity);
	}

	/** Entity合法化校验 */
	protected void doValidate(E entity, Class<?>... groups) {
		ValidationUtil.validate(entity);
	}

	/** Entity新增/更新记录前 */
	protected void beforeSaveOrUpdate(E entity, boolean isAdd) {
		FieldHandlerManager.beforeSaveOrUpdate(entity, isAdd);
	}

	/** Entity新增/更新记录后 */
	protected void afterSaveOrUpdate(E entity, boolean isAdd) {
	}

	/** VO更新前 */
	protected void beforeUpdateVO(E entity) {
		FieldHandlerManager.beforeVOUpdate(entity);
	}

	/** Entity删除记录前 */
	protected void beforeDelete(E entity) {
	}

	/** Entity转VO */
	protected void entityToVo(E entity, V vo, boolean isAdd) {
		EntityToVoMapper.getInstance().entityToVo(entity, vo, false);
	}

	/** VO转Entity */
	protected void voToEntity(V vo, E entity, boolean isAdd) {
		VoToEntityMapper.getInstance().voToEntity(vo, entity);
	}

	// ==================== 新建/初始化 ====================

	@Override
	public V init(Optional<V> oVO) {
		// 创建一个Entity，并接收vo的参数
		E entity = ReflectUtil.newInstance(getEntityClass());
		V vo = null;
		if (oVO.isPresent()) {
			vo = oVO.get();
			vo.setFdId(null);
			voToEntity(vo, entity, true);
		} else {
			vo = ReflectUtil.newInstance(getViewObjectClass());
		}
		// 初始化
		doInit(entity);
		if (entity.getMechanisms() != null
				&& entity.getMechanisms().get("load") != null) {
			applicationContext.publishEvent(new EntityInitEvent(entity));
		}
		// 转换回VO
		entityToVo(entity, vo, true);
		return vo;
	}

	@Transactional(rollbackFor = {})
	@Override
	public void add(V vo) {
		E entity = ReflectUtil.newInstance(getEntityClass());
		voToEntity(vo, entity, true);
		add(entity);
	}

	@Transactional(rollbackFor = {})
	@Override
	public void add(E entity) {
		if (repository.existsById(entity.getFdId())) {
			throw new RecordExistException();
		}
		doInit(entity);
		beforeSaveOrUpdate(entity, true);
		doValidate(entity, AddGroup.class, Default.class);
		repository.save(entity);
		afterSaveOrUpdate(entity, true);
		//TODO  日志 add-处理
		applicationContext
				.publishEvent(new EntityAddEvent(entity));
	}

	// ==================== 更新 ====================

	@Transactional(rollbackFor = {})
	@Override
	public void update(V vo) {
		Optional<E> oEntity = findById(vo.getFdId());
		if (oEntity.isPresent()) {
			E entity = oEntity.get();
			//TODO  日志 update-处理
			voToEntity(vo, entity, false);
			beforeUpdateVO(entity);
			update(entity);
		} else {
			throw new NoRecordException();
		}
	}

	@Transactional(rollbackFor = {})
	@Override
	public void update(E entity) {
		beforeSaveOrUpdate(entity, false);
		doValidate(entity, UpdateGroup.class, Default.class);
		repository.save(entity);
		afterSaveOrUpdate(entity, false);
		applicationContext
				.publishEvent(new EntityUpdateEvent(entity));
	}

	// ==================== 删除 ====================

	@Transactional(rollbackFor = {})
	@Override
	public void delete(IdVO id) {
		E entity = getOne(id.getFdId());
		delete(entity);
	}

	@Transactional(rollbackFor = {})
	@Override
	public void deleteAll(IdsDTO ids) {
		for (String id : ids.getFdIds()) {
			delete(IdVO.of(id));
		}
	}

	@Transactional(rollbackFor = {})
	@Override
	public void delete(E entity) {
		beforeDelete(entity);
		applicationContext
				.publishEvent(new EntityDeleteEvent(entity));
		repository.delete(entity);
		//TODO  日志 delete-处理
	}

	// ==================== 查询 ====================
	
	@Override
	public Long getTotal() {
		MetaEntity metaEntity = Meta.getEntity(entityClass.getName());
		MetaProperty metaProperty = metaEntity.getProperty("fdDeleted");
		if(metaProperty == null){
			return repository.count();
		}else{
			return repository.count((Specification<E>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("fdDeleted"), Boolean.FALSE));
		}
	}

	@Override
	public Optional<V> loadById(IdVO id) {
		Optional<E> oEntity = findById(id.getFdId());
		if (oEntity.isPresent()) {
			E entity = oEntity.get();
			if (id.getMechanisms() != null
					&& id.getMechanisms().get("load") != null) {
				entity.setMechanisms(id.getMechanisms());
				applicationContext.publishEvent(new EntityLoadEvent(entity));
			}
			V vo = ReflectUtil.newInstance(getViewObjectClass());
			entityToVo(entity, vo, false);
			//TODO  日志 find-处理
			return Optional.of(vo);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public E getOne(String id) {
		if (!IDGenerator.isCurrentTenantId(id)) {
			throw new NoRecordException();
		}
		E entity = repository.getOne(id);
		//TODO  日志 find-处理
		return entity;
	}

	@Override
	public Optional<E> findById(String id) {
		if (!IDGenerator.isCurrentTenantId(id)) {
			return Optional.empty();
		}
		Optional<E> result = repository.findById(id);
		if (result.isPresent()) {
			//TODO  日志 find-处理
		}
		return result;
	}

	@Override
	public QueryResult<V> findAll(QueryRequest request) {
		PageQueryTemplate<E, V> template = new PageQueryTemplate<>(
				entityManager,
				getEntityClass(), getViewObjectClass());
		return template.findAll(request);
	}

	// ==================== 辅助 ====================

	@Override
	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass() {
		if (entityClass == null) {
			entityClass = (Class<E>) ReflectUtil.getActualClass(getClass(),
					AbstractServiceImpl.class, "E");
		}
		return entityClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<V> getViewObjectClass() {
		if (viewObjectClass == null) {
			viewObjectClass = (Class<V>) ReflectUtil.getActualClass(getClass(),
					AbstractServiceImpl.class, "V");
		}
		return viewObjectClass;
	}

	@Override
	public String getEntityName() {
		return getEntityClass().getName();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
