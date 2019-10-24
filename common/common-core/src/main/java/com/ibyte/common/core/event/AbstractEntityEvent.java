package com.ibyte.common.core.event;

import com.ibyte.common.core.entity.IEntity;
import org.springframework.context.ApplicationEvent;

/**
 * Entity相关事件
 *
 * @author li.shangzhi
 */
public abstract class AbstractEntityEvent extends ApplicationEvent {
	private static final long serialVersionUID = 4202605141052367802L;

	public AbstractEntityEvent(IEntity source) {
		super(source);
	}

	public IEntity getEntity() {
		return (IEntity) getSource();
	}
}
