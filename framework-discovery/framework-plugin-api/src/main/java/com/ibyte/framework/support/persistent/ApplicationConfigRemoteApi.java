package com.ibyte.framework.support.persistent;

import com.ibyte.framework.support.persistent.dto.ApplicationConfigVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 配置持久化远程调用接口，仅提供给ApplicationConfigApi使用
 *
 * @author li.Shangzhi
 * @Date: 2019-10-23
 */
public interface ApplicationConfigRemoteApi {
	/**
	 * 保存
	 * 
	 * @param vo
	 */
	@PostMapping("save")
	void save(@RequestBody ApplicationConfigVO vo);

	/**
	 * 读取配置详细信息，返回content内容
	 * 
	 * @param id
	 * @return
	 */
	@PostMapping("get")
	String get(@RequestBody String id);
}
