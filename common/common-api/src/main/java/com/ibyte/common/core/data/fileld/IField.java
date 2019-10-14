package com.ibyte.common.core.data.fileld;

/**
 * 字段接口，仅支持以下注解：
 * <li>javax.persistence.Basic</li>
 * <li>javax.persistence.Column(name, length, nullable, updatable, precision,
 * scale)</li>
 * <li>javax.persistence.ElementCollection：不读取属性</li>
 * <li>javax.persistence.JoinColumn(name, nullable, updatable)</li>
 * <li>javax.persistence.JoinTable(name, joinColumns[0].name,
 * inverseJoinColumns[0].name)</li>
 * <li>javax.persistence.Lob</li>
 * <li>javax.persistence.ManyToMany：不读取属性</li>
 * <li>javax.persistence.ManyToOne(fetch, optional)</li>
 * <li>javax.persistence.OrderColumn(name)</li>
 * <li>javax.persistence.Temporal</li>
 * <li>javax.persistence.Transient</li>
 * <li>javax.persistence.Version</li>
 * <li>org.hibernate.validator.constraints.Length</li><br>
 * 表注解：
 * <li>javax.persistence.Table(indexes)</li>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public interface IField {

}
