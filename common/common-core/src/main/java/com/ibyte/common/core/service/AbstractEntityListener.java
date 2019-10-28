package com.ibyte.common.core.service;

import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.event.*;
import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.common.core.util.TransactionUtil;
import com.ibyte.common.exception.KmssServiceException;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.Meta;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaModule;
import org.springframework.context.ApplicationListener;

import java.util.Collection;

/**
 * 主文档监听基类
 *
 * @author ibyte
 *
 * @param <E>
 */
public abstract class AbstractEntityListener<E extends AbstractEntityEvent>
		implements ApplicationListener<E> {
	/**
	 * 是否支持
	 * 
	 * @param entity
	 * @return
	 */
	protected abstract boolean support(IEntity entity);

	/**
	 * 机制所属模块
	 * 
	 * @return
	 */
	protected abstract String getModuleName();

	/**
	 * Entity/VO的mechanisms属性中，机制对应的key，无相关数据返回null
	 */
	protected String getMechanismKey() {
		return null;
	}

	/**
	 * 读取数据字典机制特征
	 */
	protected <T> T getFeature(IEntity entity, Class<T> feature) {
		MetaEntity meta = MetaEntity
				.localEntity(EntityUtil.getEntityClassName(entity));
		if (meta != null) {
			return meta.getFeature(feature);
		}
		return null;
	}

	/**
	 * 加载初始化数据
	 */
	protected void doInit(IEntity entity) {
	}

	/**
	 * 检查新增时的数据是否正确
	 */
	protected void checkAdd(IEntity entity) {
	}

	/**
	 * 事务提交后触发，执行添加的操作
	 */
	protected void doAdd(IEntity entity) {
	}

	/**
	 * 是否加载机制数据
	 */
	protected boolean shouldLoad(IEntity entity) {
		if (getMechanismKey() == null) {
			return false;
		}
		if (entity.getMechanisms() == null) {
			return true;
		}
		Object load = entity.getMechanisms().get("load");
		if (load == null) {
			return false;
		}
		if (load instanceof Collection) {
			return ((Collection<?>) load).contains(getMechanismKey());
		} else {
			return "*".equals(load) || getMechanismKey().equals(load);
		}
	}

	protected void checkAlive() {
		if (getModuleName() == null) {
			return;
		}
		MetaModule module = Meta.getModule(getModuleName());
		if (!Meta.isApplicationAlive(module.getAppName())) {
			throw new KmssServiceException("global.service.unavalable",
					StringHelper.join("当前模块依赖的服务：", getModuleName(), " @ ",
							module.getAppName(), "未启动"));
		}
	}

	/**
	 * 加载查看页数据
	 */
	protected void doLoad(IEntity entity) {
	}

	/**
	 * 检查更新时的数据是否正确
	 */
	protected void checkUpdate(IEntity entity) {
	}

	/**
	 * 事务提交后触发，执行更新的操作
	 */
	protected void doUpdate(IEntity entity) {
	}

	/**
	 * 检查删除时的数据是否正确
	 */
	protected void checkDelete(IEntity entity) {
	}

	/**
	 * 事务提交后触发，执行删除的操作
	 */
	protected void doDelete(IEntity entity) {
	}

	@Override
	public void onApplicationEvent(E event) {
		IEntity entity = event.getEntity();
		if (!support(entity)) {
			return;
		}
		if (event instanceof EntityLoadEvent) {
			if (shouldLoad(entity)) {
				doLoad(entity);
			}
		} else if (event instanceof EntityInitEvent) {
			if (shouldLoad(entity)) {
				doInit(entity);
			}
		} else if (event instanceof EntityUpdateEvent) {
			checkAlive();
			checkUpdate(entity);
			TransactionUtil.afterCommit(() -> {
				doUpdate(entity);
			});
		} else if (event instanceof EntityAddEvent) {
			checkAlive();
			checkAdd(entity);
			TransactionUtil.afterCommit(() -> {
				doAdd(entity);
			});
		} else if (event instanceof EntityDeleteEvent) {
			checkAlive();
			checkDelete(entity);
			TransactionUtil.afterCommit(() -> {
				doDelete(entity);
			});
		}
	}
}
