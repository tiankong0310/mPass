package com.ibyte.common.core.entity;

import com.ibyte.common.core.data.IData;
import com.ibyte.common.core.data.fileld.IField;
import com.ibyte.framework.meta.annotation.MetaProperty;
import com.ibyte.framework.meta.MetaConstant.ShowType;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Min;

/**
 * 树模型，注意：fdHierarchyId的构造不是默认生成的，请在Service里面，使用TreeEntityUtil对应的方法
 *
 * @param <E>
 * @author 李尚志
 */
@Table(indexes = @Index(columnList = "fdHierarchyId"))
public interface TreeEntity<E extends IEntity> extends IData, IField {
    String HIERARCHY_ID_SPLIT = "x";

    String HIERARCHY_INVALID_FLAG = "0";

    long MIN_TREE_LEVEL = 1L;

    /**
     * 读-上级
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fd_parent_id")
    @MetaProperty(messageKey = "property.fdParent")
    default E getFdParent() {
        return (E) getExtendProps().get("fdParent");
    }

    /**
     * 写-上级
     *
     * @param fdParent
     */
    default void setFdParent(E fdParent) {
        getExtendProps().put("fdParent", fdParent);
    }

    /**
     * 读-层级ID
     *
     * @return
     */
    @MetaProperty(showType = ShowType.NONE, messageKey = "property.fdHierarchyId")
    @Length(max = 900)
    @Column(columnDefinition = "varchar(900)")
    default String getFdHierarchyId() {
        return (String) getExtendProps().get("fdHierarchyId");
    }

    /**
     * 写-层级ID
     *
     * @param fdHierarchyId
     */
    default void setFdHierarchyId(String fdHierarchyId) {
        getExtendProps().put("fdHierarchyId", fdHierarchyId);
    }

    /**
     * 读-层级
     *
     * @return
     */
    @MetaProperty(showType = ShowType.NONE, messageKey = "property.fdTreeLevel")
    @Min(MIN_TREE_LEVEL)
    default Integer getFdTreeLevel() {
        return (Integer) getExtendProps().get("fdTreeLevel");
    }

    /**
     * 写-层级
     *
     * @param fdTreeLevel
     */
    default void setFdTreeLevel(Integer fdTreeLevel) {
        getExtendProps().put("fdTreeLevel", fdTreeLevel);
    }
}
