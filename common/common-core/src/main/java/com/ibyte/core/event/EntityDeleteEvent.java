package com.ibyte.core.event;

import com.ibyte.core.entity.IEntity;

/**
 * Entity删除事件
 *
 * @author li.shangzhi
 * 
 */
public class EntityDeleteEvent extends AbstractEntityEvent {
	private static final long serialVersionUID = 3286242784061272924L;

	public EntityDeleteEvent(IEntity source) {
		super(source);
	}
}
