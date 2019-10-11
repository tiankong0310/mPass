package com.ibyte.common.web;

import com.ibyte.common.config.SystemConfig;
import com.ibyte.common.constant.NamingConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

/**
 * 链路id信息过滤器
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class RsaPublicKeyFilter extends BaseWebFilter {

    /**
     * RSA publickey请求头
     */
    public final static String HEADER_NEED_KEY = NamingConstant.HEADER_PREFIX + "NEED-PKEY";
    public final static String HEADER_PUBKEY = NamingConstant.HEADER_PREFIX + "PUBKEY";

    public final static String RSA_KEY = "RSA";
    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    private SystemConfig systemConfig;

    /**
     * RSA base64 公钥
     */
    private String  base64PublicKey;

    /**
     * 初始化
     *
     * @throws ServletException
     */
    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        systemConfig = applicationContext.getBean(SystemConfig.class);
    }

    /**
     * 过滤处理
     *
     * @param request
     * @param response
     * @param chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String pubKey = request.getHeader(HEADER_NEED_KEY);
        handleTenant(request, response, pubKey);
        chain.doFilter(request, response);
    }

    /**
     * RSA publickey相关操作
     *
     * @param request
     * @param response
     * @param pubKey
     */
    private void handleTenant(HttpServletRequest request,
                              HttpServletResponse response,
                              String pubKey) {
        if (RSA_KEY.equals(pubKey)) {
            if(base64PublicKey==null) {
                //publicCode base64 编码
                //由于配置文件中的RSA 密钥是Hex 编码，需要转换
                if (StringUtils.isNotBlank(systemConfig.getPublicCode())) {
                    base64PublicKey= Base64.getEncoder().encodeToString(Hex.decode(systemConfig.getPublicCode()));
                }
            }
            response.setHeader(HEADER_PUBKEY, base64PublicKey);
        }
    }

}
