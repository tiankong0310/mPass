package com.ibyte.common.security.encryption.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.ibyte.common.security.encryption.IEncrypt.CHARSET_DEFAULT;

/**
 * md5加解密处理组件，兼容原有算法
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class Md5EncryptProvider extends AbstractEncryptProvider {
    /**
     * md5加密标示
     */
    public static final String ENCRYPT_MD5 = "MD5";


    public Md5EncryptProvider(String salt) {
        super(salt);
    }

    @Override
    public String encrypt(String encryptStr) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPT_MD5);
            if (!StringUtils.isEmpty(this.password)) {
                messageDigest.update((encryptStr + this.password).getBytes(CHARSET_DEFAULT));
            } else {
                messageDigest.update(encryptStr.getBytes(CHARSET_DEFAULT));
            }
            return String.valueOf(HexUtils.toHexString(messageDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            log.error("Not a valid encryption algorithm", e);
            throw new IllegalArgumentException("Not a valid encryption algorithm", e);
        }
    }

    @Override
    public String decrypt(String decryptStr) {
        throw new UnsupportedOperationException("md5无法解密.");
    }

}
