package com.ibyte.common.security.encryption.provider;

import com.ibyte.common.security.encryption.IEncrypt;

/**
 * 虚拟加解密提供者类
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public abstract class AbstractEncryptProvider implements IEncrypt {

    /**
     * 加密密钥
     */
    protected final String password;

    /**
     * 构造函数，必须传递加密密钥
     *
     * @param password
     */
    AbstractEncryptProvider(String password) {
        this.password = password;
    }
}
