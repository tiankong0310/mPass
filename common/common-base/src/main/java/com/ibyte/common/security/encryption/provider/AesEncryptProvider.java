package com.ibyte.common.security.encryption.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * aes加解密处理组件，
 * 兼容原aes算法，仅仅在原算法基础上增加了hex处理
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class AesEncryptProvider extends AbstractEncryptProvider {
    /**
     * 默认密钥，当密钥不够使用于补位
     */
    private final String PASSWORD_DEFAULT = "abcdefghijklmnopqrstuvwxyz1234567890";

    /**
     * aes类型标示
     */
    public static final String ENCRYPT_AES = "AES";

    /**
     * 加密特征
     */
    private final String AES_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 加密器
     */
    private final Cipher encryptor;

    /**
     * 解密器
     */
    private final Cipher decryptor;

    /**
     * key
     */
    private final SecretKey secretKey;

    /**
     * iv信息
     */
    private AlgorithmParameterSpec ivParam;

    /**
     * 构造函数
     *
     * @param password
     */
    public AesEncryptProvider(String password) {
        super(password);
        String str = new StringBuffer(password).append(PASSWORD_DEFAULT).toString();
        String key = str.substring(0, 16);
        String iv = str.substring(16, 32);
        try {
            this.encryptor = Cipher.getInstance(AES_CBC_ALGORITHM);
            this.decryptor = Cipher.getInstance(AES_CBC_ALGORITHM);
        } catch (NoSuchAlgorithmException e1) {
            log.error("Not a valid encryption algorithm", e1);
            throw new IllegalArgumentException("Not a valid encryption algorithm", e1);
        } catch (NoSuchPaddingException e2) {
            log.error("Not a valid encryption algorithm", e2);
            throw new IllegalStateException("Should not happen", e2);
        }
        this.secretKey = new SecretKeySpec(key.getBytes(CHARSET_DEFAULT), ENCRYPT_AES);
        this.ivParam = new IvParameterSpec(iv.getBytes(CHARSET_DEFAULT));
    }

    /**
     * 加密
     *
     * @param encryptStr
     * @return
     */
    @Override
    public String encrypt(String encryptStr) {
        synchronized (this.encryptor) {
            try {
                encryptor.init(Cipher.ENCRYPT_MODE, secretKey, ivParam);
                byte[] encodes = Base64.getEncoder().encode(encryptor.doFinal(encryptStr.getBytes(CHARSET_DEFAULT)));
                return HexUtils.toHexString(encodes);
            } catch (Exception e) {
                log.error(ENCRYPT_AES + "加密出错", e);
            }
        }
        return null;
    }

    /**
     * 解密
     *
     * @param decryptStr
     * @return
     */
    @Override
    public String decrypt(String decryptStr) {
        synchronized (this.decryptor) {
            try {
                decryptor.init(Cipher.DECRYPT_MODE, secretKey, ivParam);
                return new String(decryptor.doFinal(Base64.getDecoder().decode(HexUtils.fromHexString(decryptStr))), CHARSET_DEFAULT);
            } catch (Exception e) {
                log.error(ENCRYPT_AES + "解密出错", e);
            }
        }
        return null;
    }

}
