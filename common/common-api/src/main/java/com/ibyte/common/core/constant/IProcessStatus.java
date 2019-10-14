package com.ibyte.common.core.constant;

/**
 * 流程状态枚举接口
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public interface IProcessStatus extends IEnum<String> {
	/**
	 * 废弃中：废弃(00)/未提交(01)
	 * 
	 * @return
	 */
	default boolean isDiscard() {
		return getValue().startsWith("0");
	}

	/**
	 * 起草中：草稿(10)/驳回(11)
	 * 
	 * @return
	 */
	default boolean isDraft() {
		return getValue().startsWith("1");
	}

	/**
	 * 审批中：待审(20)
	 * 
	 * @return
	 */
	default boolean isExamine() {
		return getValue().startsWith("2");
	}

	/**
	 * 发布中：发布(30)
	 * 
	 * @return
	 */
	default boolean isPublish() {
		return getValue().charAt(0) >= '3';
	}
}
