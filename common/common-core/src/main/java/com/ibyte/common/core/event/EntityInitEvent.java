package com.ibyte.common.core.event;

import com.ibyte.common.core.entity.IEntity;

/**
 * Entity初始化事件<br>
 * 注意：在Add的时候触发Init事件，而是触发Add事件
 *
 * @author li.shangzhi
 */
public class EntityInitEvent extends AbstractEntityEvent {
	private static final long serialVersionUID = -4124788048347495327L;

	public EntityInitEvent(IEntity source) {
		super(source);
	}
}
