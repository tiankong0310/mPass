package com.ibyte.common.web;

import com.ibyte.common.util.thread.ThreadLocalUtil;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 传播线程变量处理
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class TranVarFilter extends BaseWebFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String key = headers.nextElement();
            if (!StringUtils.isEmpty(key)
                    && key.startsWith(ThreadLocalUtil.TRAN_PREFIX)) {
                ThreadLocalUtil.setTranVar(key.substring(ThreadLocalUtil.TRAN_PREFIX.length()), request.getHeader(key));
            }
        }
        chain.doFilter(request, response);
    }
}
