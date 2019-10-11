package com.ibyte.common.web;

import com.ibyte.common.config.CorsConfig;
import com.ibyte.common.util.thread.ThreadLocalHolder;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Web线程拦截器，用于统一处理线程变量
 *      该过滤器执行顺序早于springsecurity的过滤器
 *
 * @author li.Shangzhi
 * @Date: 2019-10-11
 */
public class WebThreadFilter extends CompositeFilterProxy {

    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 应用上下文
     */
    private CorsConfig corsConfig;
    /**
     * 带参数构造器
     *
     * @param filterDefs
     */
    public WebThreadFilter(List<IWebFilterDef> filterDefs) {
        super(filterDefs);
    }

    @Override
    protected void initFilterBean() throws ServletException {
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        corsConfig = applicationContext.getBean(CorsConfig.class);
        super.initFilterBean();
    }

    @Override
    protected void handleInnerFilters(List<Filter> innerFilters) {
        super.handleInnerFilters(innerFilters);
        innerFilters.add(0, new TranVarFilter());
        innerFilters.add(1, new TenantFilter());
        innerFilters.add(2, new LangFilter());
        innerFilters.add(3, new TraceFilter());
        innerFilters.add(4, new CorsSecurityFilter(corsConfig));
        innerFilters.add(5, new RsaPublicKeyFilter());

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        ThreadLocalHolder.begin();
        ThreadLocalUtil.setLocalVar(ThreadLocalUtil.REQUEST_KEY,
                request);
        try {
            compositeFilter.doFilter(request, response, chain);
        } finally {
            ThreadLocalHolder.end();
        }
    }
}

