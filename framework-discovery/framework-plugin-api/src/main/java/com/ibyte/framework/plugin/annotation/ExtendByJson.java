package com.ibyte.framework.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用Json的方式声明扩展。<br>
 * 仅在当前模块不想依赖提供扩展点的模块的时候使用。<br>
 * JSON的格式：
 * 
 * <pre>
 * {
 *    extensionPoint: {
 *      id: '必填',
 *      config: '配置类名，extension不为空的时候必填',
 *      superPoints: ['扩展点的继承关系']
 *    }
 *    extension: {
 *    }
 * }
 * </pre>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtendByJson {
	/** JSON文件地址 */
	String value();
}
