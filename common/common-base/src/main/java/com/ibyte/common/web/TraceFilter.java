package com.ibyte.common.web;

import com.ibyte.common.config.trace.TraceConfig;
import com.ibyte.common.util.IDGenerator;
import com.ibyte.common.util.TraceUtil;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 链路id信息过滤器
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class TraceFilter extends BaseWebFilter {
    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    private TraceConfig traceConfig;

    /**
     * 初始化
     *
     * @throws ServletException
     */
    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        traceConfig=applicationContext.getBean(TraceConfig.class);
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
        String traceId = TraceUtil.getTraceId();
        if (StringUtils.isBlank(traceId)) {
            traceId = IDGenerator.generateID();
        }
        handleTenant(request, response, traceId);
        chain.doFilter(request, response);
    }

    /**
     * 租户相关操作
     *
     * @param request
     * @param response
     * @param traceId
     * @throws UnsupportedEncodingException
     */
    private void handleTenant(HttpServletRequest request,
                              HttpServletResponse response,
                              String traceId) throws UnsupportedEncodingException {
        //链路信息处理
        if (StringUtils.isNotBlank(traceId)) {
            ThreadLocalUtil.setTranVar(TraceUtil.TRACE_ID, traceId);
        }
        //把远程获取的服务名称设置到线程变量中
        String fromServer = ThreadLocalUtil.getTranVar(TraceUtil.FROM_SERVER);
        ThreadLocalUtil.setLocalVar(TraceUtil.FROM_SERVER,fromServer);
        //把本服务器名设置为 调用服务名
        if(traceConfig!=null) {
            ThreadLocalUtil.setLocalVar(TraceUtil.CURRENT_SERVER,traceConfig.getName());
            ThreadLocalUtil.setTranVar(TraceUtil.FROM_SERVER, traceConfig.getName());
        }

    }

}
