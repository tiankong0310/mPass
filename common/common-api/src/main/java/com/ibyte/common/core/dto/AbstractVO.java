package com.ibyte.common.core.dto;

import com.ibyte.common.util.IDGenerator;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 展现对象基类
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public abstract class AbstractVO implements IViewObject {
	private String fdId;

	private final Map<String, Object> extendProps = new HashMap<>(16);

	private Map<String, Object> dynamicProps;

	private Map<String, Object> mechanisms;

	private List<String> nullValueProps;

	@Override
	public String getFdId() {
		return fdId;
	}

	@Override
	public void setFdId(String fdId) {
		if (fdId != null) {
			IDGenerator.validate(fdId);
		}
		this.fdId = fdId;
	}

	@Override
	public Map<String, Object> getExtendProps() {
		return extendProps;
	}

	@Override
	public Map<String, Object> getDynamicProps() {
		return dynamicProps;
	}

	@Override
	public void setDynamicProps(Map<String, Object> dynamicProps) {
		this.dynamicProps = dynamicProps;
	}

	@Override
	public Map<String, Object> getMechanisms() {
		return mechanisms;
	}

	@Override
	public void setMechanisms(Map<String, Object> mechanisms) {
		this.mechanisms = mechanisms;
	}

	@Override
	public List<String> getNullValueProps() {
		return nullValueProps;
	}

	@Override
	public void setNullValueProps(List<String> nullValueProps) {
		this.nullValueProps = nullValueProps;
	}

	@Override
	public void addNullValueProps(String... props) {
		if (nullValueProps == null) {
			nullValueProps = new ArrayList<>();
		}
		for (String prop : props) {
			nullValueProps.add(prop);
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getClass().getName())
				.append(getFdId()).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof IViewObject)) {
			return false;
		}
		if (!getFdId().equals(((IViewObject) other).getFdId())) {
			return false;
		}
		return getClass().getName()
				.equals(other.getClass().getName());
	}
}
