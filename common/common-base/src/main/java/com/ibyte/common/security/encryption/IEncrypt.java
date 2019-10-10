package com.ibyte.common.security.encryption;

import java.nio.charset.Charset;

/**
 * @Description: <加解密支持接口>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public interface IEncrypt {

    /**
     * 默认编码
     */
    Charset CHARSET_DEFAULT = Charset.defaultCharset();

    /**
     * 加密方法
     * @param encryptStr
     * @return
     */
    String encrypt(String encryptStr);


    /**
     * 解密方法
     * @param decryptStr
     * @return
     */
    String decrypt(String decryptStr);
}
