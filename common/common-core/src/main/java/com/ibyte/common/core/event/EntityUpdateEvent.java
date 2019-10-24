package com.ibyte.common.core.event;

import com.ibyte.common.core.entity.IEntity;

/**
 * Entity更新事件
 *
 * @author li.shangzhi
 */
public class EntityUpdateEvent extends AbstractEntityEvent {
	private static final long serialVersionUID = -4553680187970174219L;

	public EntityUpdateEvent(IEntity source) {
		super(source);
	}
}
