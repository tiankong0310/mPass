package com.ibyte.common.security.encryption.provider;

import com.ibyte.common.security.encryption.IEncrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.codec.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加解密处理组件
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class RsaEncryptProvider implements IEncrypt {

    public static final String RSA = "RSA";

    public static final String CHARSET_NAME = "UTF-8";
    /**
     * RSA私钥
     */
    private byte [] privateCodeByte;

    /**
     * RSA公钥
     */
    private byte [] publicCodeByte;

    /**
     * RSA公钥加密
     *
     * @param encryptStr 字符串
     * @return base64 编码后的密文
     */
    @Override
    public String encrypt(String encryptStr) {
        String outStr = null;
        try {
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(publicCodeByte));
            //RSA加密
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            outStr = Base64.getEncoder().encodeToString(cipher.doFinal(encryptStr.getBytes(CHARSET_NAME)));
        } catch (InvalidKeySpecException e) {
           log.error("无效的密钥规范",e);
        } catch (NoSuchAlgorithmException e) {
            log.error("无效的算法",e);
        } catch (NoSuchPaddingException e) {
            log.error("无效的算法",e);
        } catch (InvalidKeyException e) {
            log.error("无效的密钥",e);
        } catch (IllegalBlockSizeException e) {
            log.error("非法块大小",e);
        } catch (BadPaddingException e) {
            log.error("错误填充异常",e);
        } catch (UnsupportedEncodingException e) {
            log.error("不支持的编码异常",e);
        }
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param decryptStr Base64 编码的加密字符串
     * @return 明文
     */
    @Override
    public String decrypt(String decryptStr){
        String outStr = null;
        try {
            //64位解码加密后的字符串
            byte[] inputByte = Base64.getDecoder().decode(decryptStr.getBytes(CHARSET_NAME));
            //私钥
            RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(privateCodeByte));
            //RSA解密
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            outStr = new String(cipher.doFinal(inputByte));
        } catch (Exception e) {
           log.error("RSA解密失败",e);
        }
        return outStr;
    }


    /**
     * 随机生成密钥对
     * @throws NoSuchAlgorithmException
     */
    public static void genKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024,new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        String publicKeyString = new String(Hex.encode(publicKey.getEncoded()));
        // 得到私钥字符串
        String privateKeyString = new String(Hex.encode((privateKey.getEncoded())));
        // 将公钥和私钥保存到Map
        //0表示公钥
        System.out.println("公钥 16进制:"+publicKeyString);
        //1表示私钥
        System.out.println("私钥 16进制："+privateKeyString);
    }



    public RsaEncryptProvider(byte [] privateCodeByte, byte [] publicCodeByte) {
        this.privateCodeByte = privateCodeByte;
        this.publicCodeByte = publicCodeByte;
    }



    public static void main(String[] args) throws Exception {
        //生成公钥和私钥

        String p = "30819f300d06092a864886f70d010101050003818d00308189028181009b44b604191d16ca3d47f7cddb0220535242eb54bc3b5391707ca568ce76fc22f251ab36c32a531eeb9c1222fc3e7f4a2a53eed609958a164f28a63a6bd6958b3d25d574bea89cd3ac37d85ef8f2f15d8dfc7d1dc442f166022250f058c9ac63491b7d67b79f9414a445d742ea40f95868bbbbf2f5f4dc868603aa655840f6f30203010001";
        String pr = "30820274020100300d06092a864886f70d01010105000482025e3082025a020100028181009b44b604191d16ca3d47f7cddb0220535242eb54bc3b5391707ca568ce76fc22f251ab36c32a531eeb9c1222fc3e7f4a2a53eed609958a164f28a63a6bd6958b3d25d574bea89cd3ac37d85ef8f2f15d8dfc7d1dc442f166022250f058c9ac63491b7d67b79f9414a445d742ea40f95868bbbbf2f5f4dc868603aa655840f6f30203010001027f527c2e35ff2174f9fb9440111c4804e43ecf4e35b5c9ce9b7526c2b8eaf64afc45bf1d35fa7881afeb7afe2797d99bc5cab6cc2ae8ec886f519e46b0c2993cd7e7c465b3b25741bd394442b29b0ef45c18430fb6363343f343f8fe2623b14857ac562c0dd2097b314ad22caff8e974473e8aab74280cc9d4e61f1515b75f41024100cb6a95ae12f3583a2e514bb75cb4ac1d3230fc094ef86a6ab3a0c4e9bed1058fd9159fc5897ee99509d73b835921294abac8738b28c2657fbc09f4b13c9479bf024100c367db20d8cdd381ffc5a34f643b5567cb90db7b7938b01c308897174b35f9956737192231b89198800dbaca95a4a82a7b7735973003828b246aafab1348c7cd02407eb2e0901015a9fa732707b629f40ed010971a24e21f30894d60f1c575a8d3820980c2875360cd349b70880d03d2f7d92805fa91cecd6652ed5876247ecb2c1902402b999dc43b7bd988b52845232ecb2a68497c9dec7404d90ec1298904f28c2d8f4f8374c13374ea4fabaae495d543f31ddf849ed988261057b99d706aec979db5024100b046eb05db41b88649c0995c0201191276c9495e293634f9ccc8f4e5e98969c4e79afb8b018afad1742fa2688da2ec1f0ef8578cdc7552d86eb8e443fbae9c76";
        RsaEncryptProvider rsaEncryptProvider = new RsaEncryptProvider(Hex.decode(pr), Hex.decode(p));
        //加密字符串
        String message = "1";
        String messageEn = rsaEncryptProvider.encrypt(message);
        System.out.println(message + "\t加密后的字符串为:" + messageEn);
        String messageDe = rsaEncryptProvider.decrypt(messageEn);
        System.out.println("还原后的字符串为:" + messageDe);
    }
}
