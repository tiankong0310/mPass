package com.ibyte.common.core.entity;

import com.ibyte.common.core.data.IData;

/**
 * 数据库实体对象接口
 * 
 * @author 李尚志
 * @time   2019年10月25日 01:09:34
 */
public interface IEntity extends IData {
	/**
	 * 读-租户ID
	 * 
	 * @return
	 */
	Integer getFdTenantId();

	/**
	 * 写-租户ID
	 * 
	 * @param fdTenantId
	 */
	void setFdTenantId(Integer fdTenantId);
}
