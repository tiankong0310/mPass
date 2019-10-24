package com.ibyte.common.core.mapper;

import com.ibyte.common.exception.KmssRuntimeException;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.util.LangUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Entity与VO的转换工具默认实现
 * 
 * @author li.shangzhi
 */
@Slf4j
public abstract class AbstractMapper {
	protected static final String EXCEPTION_KEY = "errors.dataTranException";

	/** 输出成功日志 */
	protected void logSuccess(MapperContext context) {
		if (log.isDebugEnabled()) {
			String path = context.path == null ? null
					: StringHelper.join("[", context.path, "]");
			log.debug(StringHelper.join(
					context.source.getClass().getSimpleName(), ".",
					context.propName, " -> ",
					context.target.getClass().getSimpleName(), ".",
					context.propName, path, " : success!"));
		}
	}

	/** 处理异常 */
	protected void handleException(MapperContext context, Exception e) {
		String message = StringHelper.join(
				context.source.getClass().getSimpleName(), ".",
				context.propName, " -> ",
				context.target.getClass().getSimpleName(), ".",
				context.propName);
		message = ResourceUtil.replaceArgs(
				ResourceUtil.getString(EXCEPTION_KEY), message);
		throw new KmssRuntimeException(EXCEPTION_KEY, message, e);
	}

	/**
	 * 转换上下文
	 * 
	 * @author 叶中奇
	 */
	static class MapperContext {
		// 环境参数

		final boolean langSupport = LangUtil.isSuportEnabled();
		final boolean inList;
		final List<String> excludes;

		// Bean级参数，递归下层时修改

		MetaEntity meta;
		Object source;
		Object target;
		String path;

		// 字段级参数，更换字段修改

		String propName;
		PropertyDescriptor targetDesc;
		MetaProperty metaProp;

		public MapperContext(MetaEntity meta, Object source, Object target,
				boolean inList, String... ignoreProperties) {
			this.inList = inList;
			this.excludes = ignoreProperties == null
					? Collections.emptyList()
					: Arrays.asList(ignoreProperties);

			this.meta = meta;
			this.source = source;
			this.target = target;
		}

		public MapperContext(MapperContext parent, MetaEntity meta,
				Object source, Object target) {
			this.inList = parent.inList;
			this.excludes = parent.excludes;

			this.meta = meta;
			this.source = source;
			this.target = target;
			this.path = parent.path == null ? parent.propName
					: StringHelper.join(parent.path, ".", parent.propName);
		}

		public Object getSourceValue(String name)
				throws ReflectiveOperationException {
			PropertyDescriptor desc = BeanUtils
					.getPropertyDescriptor(source.getClass(), name);
			if (desc == null) {
				return null;
			}
			Method read = desc.getReadMethod();
			if (!accessable(read)) {
				return null;
			}
			return read.invoke(source);
		}
	}
}
