package com.ibyte.common.core.entity;

import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.common.util.IDGenerator;
import com.ibyte.common.util.TenantUtil;
import com.ibyte.framework.meta.annotation.MetaProperty;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库实体基类
 *
 * @author li.shangzhi
 */
@MappedSuperclass
public abstract class AbstractEntity implements IEntity {
    @Id
    @Access(AccessType.PROPERTY)
    @Length(max = IDGenerator.LEN)
    @MetaProperty(messageKey = "property.fdId")
    private String fdId;

    @Access(AccessType.PROPERTY)
    @MetaProperty(messageKey = "property.fdTenantId")
    private Integer fdTenantId;

    @Transient
    private final Map<String, Object> extendProps = new HashMap<>(16);

    @Transient
    private Map<String, Object> dynamicProps;

    @Transient
    private Map<String, Object> mechanisms;

    @Override
    public String getFdId() {
        if (fdId == null) {
            fdId = IDGenerator.generateID();
        }
        return fdId;
    }

    @Override
    public void setFdId(String fdId) {
        this.fdId = fdId;
    }

    @Override
    public Integer getFdTenantId() {
        if (fdTenantId == null) {
            fdTenantId = TenantUtil.getTenantId();
        }
        return fdTenantId;
    }

    @Override
    public void setFdTenantId(Integer fdTenantId) {
        this.fdTenantId = fdTenantId;
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
    public int hashCode() {
        return new HashCodeBuilder()
                .append(EntityUtil.getEntityClassName(this))
                .append(getFdId()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof IEntity)) {
            return false;
        }
        if (!getFdId().equals(((IEntity) other).getFdId())) {
            return false;
        }
        return EntityUtil.getEntityClassName(this)
                .equals(EntityUtil.getEntityClassName(other));
    }
}
