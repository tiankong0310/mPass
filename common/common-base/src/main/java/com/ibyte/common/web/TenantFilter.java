package com.ibyte.common.web;

import com.ibyte.common.config.LicenseConfig;
import com.ibyte.common.config.TenantConfig;
import com.ibyte.common.config.tenant.TenantInfo;
import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.security.encryption.IEncrypt;
import com.ibyte.common.util.TenantUtil;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @Description: <租户信息过滤器>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11 23
 */
@Slf4j
public class TenantFilter extends BaseWebFilter {
    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 租户配置
     */
    private TenantConfig tenantConfig;

    /**
     * 授权控制
     */
    private LicenseConfig licenseConfig;

    /**
     * 租户id信息
     */
    public final static String HEADER_TENANT_ID = NamingConstant.HEADER_PREFIX + "TENANT-ID";

    /**
     * 租户对应名称
     */
    public final static String HEADER_TENANT_NAME = NamingConstant.HEADER_PREFIX + "TENANT-NAME";

    /**
     * 初始化
     * @throws ServletException
     */
    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        tenantConfig = applicationContext.getBean(TenantConfig.class);
        licenseConfig = applicationContext.getBean(LicenseConfig.class);
    }

    /**
     * 过滤处理
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
        String tenantId = ThreadLocalUtil.getTranVar(TenantUtil.class.getName());
        TenantInfo info;
        if (tenantConfig.isEnable()) {
            if (tenantId == null) {
                info = getCurTenantConfig(request, response);
            } else {
                info = tenantConfig.getConfig().get(tenantId);
            }
        } else {
            info = getSystemTenant();
        }
        handleTenant(request, response, info);
        chain.doFilter(request, response);
    }

    /**
     * 租户相关操作
     *
     * @param request
     * @param response
     * @param tenantInfo
     * @throws UnsupportedEncodingException
     */
    private void handleTenant(HttpServletRequest request,
                              HttpServletResponse response,
                              TenantInfo tenantInfo) throws UnsupportedEncodingException {
        //租户信息处理
        if (tenantInfo != null) {
            String tenantId = tenantInfo.getId();
            String tenantName = tenantInfo.getName();
            ThreadLocalUtil.setTranVar(TenantUtil.class.getName(), tenantId);
            response.setHeader(HEADER_TENANT_ID, tenantId);
            response.setHeader(HEADER_TENANT_NAME, URLEncoder.encode(tenantName, IEncrypt.CHARSET_DEFAULT.name()));
        }
    }

    /**
     * 根据请求获取租户信息
     *
     * @param request
     * @param response
     * @return
     */
    private TenantInfo getCurTenantConfig(HttpServletRequest request,
                                          HttpServletResponse response) {
        String domain = request.getServerName();
        int port = request.getServerPort();
        TenantInfo tenantInfo = null;
        for (Map.Entry<String, TenantInfo> entry : tenantConfig.getConfig().entrySet()) {
            TenantInfo tmpTenant = entry.getValue();
            String domainInfo = tmpTenant.getDomain();
            String portInfo = tmpTenant.getPort();
            if (domain.equals(domainInfo) && String.valueOf(port).equals(portInfo)) {
                tenantInfo = tmpTenant;
                break;
            }
        }
        return tenantInfo;
    }

    /**
     * 获取系统默认租户
     *
     * @return
     */
    private TenantInfo getSystemTenant() {
        TenantInfo info = new TenantInfo();
        info.setId("" + TenantUtil.SYSTEM_TENANT);
        info.setName(licenseConfig.getTo());
        return info;
    }


}
