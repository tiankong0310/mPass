package com.ibyte.common.core.constant;


import com.ibyte.common.util.StringHelper;

/**
 * 查询类常量
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 *
 */
public interface QueryConstant {
	/**
	 * 排序方式
	 */
	public enum Direction {
		/** 升序 */
		ASC,
		/** 降序 */
		DESC;
	}

	/**
	 * 操作符
	 */
	public enum Operator implements IEnum<String> {
		/** 等于或IN */
		eq, neq, in, notIn, eqProp, eqDate,
		/** 大于 */
		gt, gtProp, gtDate,
		/** 大于等于 */
		gte, gteProp, gteDate,
		/** 小于 */
		lt, ltProp, ltDate,
		/** 小于等于 */
		lte, lteProp, lteDate,
		/** like x% */
		startsWith, startsWithProp,
		/** like lower(%x%) */
		contains, containsProp,
		/** 使用层级ID查找 */
		child,
		/** 空/非空 */
		isNull, isNotNull;

		Operator() {
			this.value = StringHelper.join('$', this.name());
		}

		private String value;

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public String getMessageKey() {
			return this.name();
		}

		public boolean isPropOperator() {
			return this == eqProp || this == gtProp || this == gteProp
					|| this == ltProp || this == lteProp
					|| this == startsWithProp || this == containsProp;
		}

		public boolean isDateOperator() {
			return this == eqDate || this == gtDate || this == gteDate
					|| this == ltDate || this == lteDate;
		}

		public static Operator get(Object value) {
			return IEnum.valueOf(Operator.class, value);
		}
	}
}
