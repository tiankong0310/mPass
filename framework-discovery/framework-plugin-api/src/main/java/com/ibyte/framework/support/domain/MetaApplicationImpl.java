package com.ibyte.framework.support.domain;

import com.ibyte.framework.meta.MetaApplication;
import lombok.Setter;

/**
 * 应用信息实现类
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Setter
public class MetaApplicationImpl implements MetaApplication {
	private String appName;

	private String dbId;

	@Override
	public String getAppName() {
		return appName;
	}

	@Override
	public String getDbId() {
		return dbId;
	}
}
