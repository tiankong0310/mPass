package com.ibyte.common.core.util;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.*;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.Items;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema.SingleItems;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.*;
import com.ibyte.framework.meta.MetaConstant.ShowType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.Map.Entry;

import static com.ibyte.common.core.util.TypeUtil.IDKEYS;

/**
 * JsonSchema工具类
 *
 * @author li.shangzhi
 */
@Slf4j
public class JsonSchemaUtil {
	/** 数据字典转换成JsonSchema */
	public static JsonSchema byEntity(String entityName) {
		MetaEntity meta = Meta.getEntity(entityName);
		if (meta == null) {
			return null;
		}
		ObjectSchema root = new ObjectSchema();
		root.setDescription(meta.getLabel());
		for (MetaProperty property : meta.getProperties().values()) {
			appendJsonSchema(root, property);
		}
		return root;
	}

	/** 解析字段，并追加到parent */
	public static JsonSchema appendJsonSchema(ObjectSchema parent,
			MetaProperty property) {
		if (property.getShowType() == ShowType.NONE) {
			return null;
		}
		JsonSchema schema = toJsonSchema(property);
		if (property.isCollection()) {
			ArraySchema array = new ArraySchema();
			completeJsonSchema(array, property);
			if (schema != null) {
				array.setItems(new SingleItems(schema));
			}
			schema = array;
		} else {
			if (schema == null) {
				return null;
			}
			completeJsonSchema(schema, property);
		}
		if (schema.getRequired() == null) {
			parent.putOptionalProperty(property.getName(), schema);
		} else {
			parent.putProperty(property.getName(), schema);
		}
		return schema;
	}

	/** 根据不同的类型构造JsonSchema */
	private static JsonSchema toJsonSchema(MetaProperty property) {
		// 根据不同的类型构造JsonSchema
		String type = property.getType();
		if (MetaConstant.isAssociation(type)) {
			MetaEntity meta = Meta.getEntity(type);
			if (meta == null) {
				return null;
			}
			ObjectSchema schema = new ObjectSchema();
			schema.setDescription(meta.getLabel());
			if (property.getVoProperties() != null) {
				for (String voPropName : property.getVoProperties()) {
					MetaProperty voProp = meta.getProperty(voPropName);
					if (voProp == null) {
						continue;
					}
					appendJsonSchema(schema, voProp);
				}
			}
			return schema;
		} else if (MetaConstant.isDate(type)) {
			IntegerSchema schema = new IntegerSchema();
			schema.setFormat(JsonValueFormat.UTC_MILLISEC);
			return schema;
		} else if (MetaConstant.isString(type)) {
			StringSchema schema = new StringSchema();
			schema.setMaxLength(property.getLength());
			return schema;
		} else if (MetaConstant.isNumber(type)) {
			if (MetaConstant.TYPE_LONG.equals(type)
					|| MetaConstant.TYPE_INTEGER.equals(type)
					|| MetaConstant.TYPE_SHORT.equals(type)) {
				return new IntegerSchema();
			} else {
				return new NumberSchema();
			}
		} else if (MetaConstant.TYPE_BOOLEAN.equals(type)) {
			return new BooleanSchema();
		} else {
			// 到这里只有二进制流了
			log.warn(StringHelper.join("无法将属性转换成JsonSchema：",
					property.getName()));
			return null;
		}
	}

	/** 完善JsonSchema信息 */
	private static void completeJsonSchema(JsonSchema schema,
			MetaProperty property) {
		schema.setDescription(property.getLabel());
		if (property.isReadOnly()) {
			schema.setReadonly(true);
		}
		if (property.isNotNull()) {
			schema.setRequired(true);
		}
		if (MetaConstant.isEnum(property)
				&& schema instanceof ValueTypeSchema) {
			Set<String> enums = new HashSet<>();
			for (EnumItem item : property.getEnumList()) {
				enums.add(item.getValue());
			}
			((ValueTypeSchema) schema).setEnums(enums);
		}
	}

	/** 根据schema转换 */
	public static Object convert(Object src, JsonSchema schema,
			boolean checkReadOnly, boolean ignoreException, String path) {
		if (src == null) {
			return null;
		}
		if (schema.isArraySchema()) {
			return convertArray(src, schema.asArraySchema(), checkReadOnly,
					ignoreException, path);
		} else if (schema.isObjectSchema()) {
			return convertObject(src, schema.asObjectSchema(), checkReadOnly,
					ignoreException, path);
		} else if (schema.isBooleanSchema()) {
			return TypeUtil.cast(src, Boolean.class);
		} else if (schema.isIntegerSchema()) {
			return TypeUtil.cast(src, Long.class);
		} else if (schema.isNumberSchema()) {
			return TypeUtil.cast(src, Double.class);
		} else if (schema.isStringSchema()) {
			return TypeUtil.cast(src, String.class);
		} else if (schema.isNullSchema()) {
			return null;
		} else {
			return src;
		}
	}

	/** 根据schema转换 */
	private static Object convertArray(Object src, ArraySchema schema,
			boolean checkReadOnly, boolean ignoreException, String path) {
		List<Object> values = new ArrayList<>();
		Items items = schema.getItems();
		path = StringHelper.join(path, "[]");
		if (src instanceof Collection) {
			if (items == null) {
				return src;
			}
			for (Object val : (Collection<?>) src) {
				values.add(convertArrayItem(val, items, checkReadOnly,
						ignoreException, path));
			}
		} else if (src instanceof Object[]) {
			if (items == null) {
				return src;
			}
			for (Object val : (Object[]) src) {
				values.add(convertArrayItem(val, items, checkReadOnly,
						ignoreException, path));
			}
		} else {
			throw new ParamsNotValidException(
					StringHelper.join(path, "：类型必须是数组"));
		}
		return values;
	}

	/** 根据schema转换 */
	private static Object convertArrayItem(Object src, Items items,
			boolean checkReadOnly, boolean ignoreException, String path) {
		if (src == null) {
			return null;
		}
		if (items.isSingleItems()) {
			JsonSchema schema = items.asSingleItems().getSchema();
			if (schema == null) {
				return src;
			}
			return convert(src, schema, checkReadOnly, ignoreException, path);
		} else if (items.isArrayItems()) {
			return src;
		} else {
			return src;
		}
	}

	/** 根据schema转换 */
	@SuppressWarnings("unchecked")
	private static Object convertObject(Object src, ObjectSchema schema,
			boolean checkReadOnly, boolean ignoreException, String path) {
		if (src instanceof String) {
			for (String key : IDKEYS) {
				JsonSchema id = schema.getProperties().get(key);
				if (id != null && id.isStringSchema()) {
					Map<String, Object> result = new LinkedHashMap<>();
					result.put(key, src);
					return result;
				}
			}
		}
		if (!(src instanceof Map)) {
			throw new ParamsNotValidException(
					StringHelper.join(path, "：类型必须是Map"));
		}
		Map<String, ?> map = (Map<String, ?>) src;
		Map<String, Object> result = new LinkedHashMap<>();
		for (Entry<String, ?> entry : map.entrySet()) {
			String key = entry.getKey();
			JsonSchema prop = schema.getProperties().get(key);
			if (prop == null) {
				String message = StringHelper.join(path, ".", key, "未定义");
				if (ignoreException) {
					log.warn(message);
					continue;
				} else {
					throw new ParamsNotValidException(message);
				}
			}
			if (checkReadOnly && prop.getReadonly() != null
					&& prop.getReadonly().booleanValue()) {
				String message = StringHelper.join(path, ".", key,
						"为只读属性");
				if (ignoreException) {
					log.warn(message);
					continue;
				} else {
					throw new ParamsNotValidException(message);
				}
			}
			result.put(key, convert(entry.getValue(), prop, checkReadOnly,
					ignoreException, StringHelper.join(path, ".", key)));
		}
		return result;
	}
}
