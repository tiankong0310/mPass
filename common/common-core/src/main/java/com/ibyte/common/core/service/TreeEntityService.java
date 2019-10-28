package com.ibyte.common.core.service;

import com.ibyte.common.core.entity.TreeEntity;
import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.common.exception.KmssRuntimeException;
import com.ibyte.common.exception.TreeCycleException;
import com.ibyte.common.util.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * 树形Entity操作，通过依赖注入调用
 *
 * @author li.shangzhi
 */
@Service
public class TreeEntityService {
    @Autowired
    private EntityManager entityManager;

    private String getTreeHierarchyId(TreeEntity<?> entity) {
        if (entity.getFdParent() != null) {
            return ((TreeEntity<?>) entity.getFdParent()).getFdHierarchyId()
                    + entity.getFdId() + TreeEntity.HIERARCHY_ID_SPLIT;
        } else {
            return TreeEntity.HIERARCHY_ID_SPLIT + entity.getFdId()
                    + TreeEntity.HIERARCHY_ID_SPLIT;
        }
    }

    private Integer getTreeLevel(TreeEntity<?> entity) {
        if (entity.getFdParent() != null) {
            Integer treeLevel = ((TreeEntity<?>) entity.getFdParent()).getFdTreeLevel();
            if (treeLevel == null) {
                return 1;
            }
            return treeLevel + 1;
        } else {
            return (int) TreeEntity.MIN_TREE_LEVEL;
        }
    }

    /**
     * TreeEntity新增或更新前调用
     * <p>由各个应用模块的Service去调用</p>
     */
    public void beforeSaveOrUpdate(TreeEntity<?> entity, boolean isAdd) {
        String newHierarchyId = getTreeHierarchyId(entity);
        String oldHierarchyId = entity.getFdHierarchyId();
        entity.setFdHierarchyId(newHierarchyId);

        Integer newLevel = getTreeLevel(entity);
        Integer oldLevel = entity.getFdTreeLevel() != null ? entity.getFdTreeLevel() : 1;
        entity.setFdTreeLevel(newLevel);
        if (!isAdd && oldHierarchyId != null && !newHierarchyId.equals(oldHierarchyId)) {
            // 处理层级ID
            String hql = StringHelper.join("update ",
                    EntityUtil.getEntityClassName(entity),
                    " set fdHierarchyId=:newHierarchyId || substring(fdHierarchyId, :len, length(fdHierarchyId)) where fdHierarchyId like :oldHierarchyId || '%'");
            Query query = entityManager.createQuery(hql);
            query.setParameter("newHierarchyId", newHierarchyId);
            query.setParameter("len", oldHierarchyId.length() + 1);
            query.setParameter("oldHierarchyId", oldHierarchyId);
            query.executeUpdate();
            // 处理层级
            if (!newLevel.equals(oldLevel)) {
                String operator = "+";
                if (newLevel < oldLevel) {
                    operator = "-";
                }
                hql = StringHelper.join("update ",
                        EntityUtil.getEntityClassName(entity),
                        " set fdTreeLevel=fdTreeLevel", operator, ":fdTreeLevel where fdHierarchyId like :newHierarchyId || '%'",
                        " and fdId <> :fdId");
                query = entityManager.createQuery(hql);
                query.setParameter("fdTreeLevel", Long.valueOf(Math.abs(newLevel - oldLevel)).intValue());
                query.setParameter("newHierarchyId", newHierarchyId);
                query.setParameter("fdId", entity.getFdId());
                query.executeUpdate();
            }
        }
        // 检查树结构的域模型中是否出现了循环嵌套
        checkTreeCycle(entity.getFdHierarchyId());
    }

    /**
     * 检查树结构的域模型中是否出现了循环嵌套，校验失败后抛出TreeCycleException异常
     *
     * @param hierarchyId
     * @throws TreeCycleException
     */
    public void checkTreeCycle(String hierarchyId) throws TreeCycleException {
        String[] hierarchyIds = hierarchyId.split(TreeEntity.HIERARCHY_ID_SPLIT);
        // 通过层级ID判断是否出现循环嵌套
        List<String> parentList = new ArrayList();
        try {
            for (String hierarchy : hierarchyIds) {
                if (StringUtils.isBlank(hierarchy)) {
                    continue;
                }
                if (parentList.contains(hierarchy)) {
                    throw new TreeCycleException();
                }
                parentList.add(hierarchy);
            }
        } catch (TreeCycleException e) {
            throw e;
        } catch (Exception e) {
            throw new KmssRuntimeException("errors.unknown", e);
        }
    }
}
