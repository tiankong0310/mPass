package com.ibyte.core.event;

import com.ibyte.core.entity.IEntity;

/**
 * Entity添加事件
 *
 * @author li.shangzhi
 */
public class EntityAddEvent extends AbstractEntityEvent {
	private static final long serialVersionUID = 5222265806945558604L;

	public EntityAddEvent(IEntity source) {
		super(source);
	}
}
