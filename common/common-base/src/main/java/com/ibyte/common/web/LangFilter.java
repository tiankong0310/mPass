package com.ibyte.common.web;

import com.ibyte.common.config.lang.LangInfo;
import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.util.LangUtil;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 接收语言信息过滤器
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
@Slf4j
public class LangFilter extends BaseWebFilter {

    /**
     * 多语言请求头
     */
    public final static String HEADER_LANG_CLIENT = NamingConstant.HEADER_PREFIX + "ACCEPT-LANGUAGE";

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
    }

    /**
     * 接收前端多语言切换请求，无则不处理
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
        //多语言信息获取
        String lang = request.getHeader(HEADER_LANG_CLIENT);
        if (LangUtil.isSuportEnabled()) {
            if (!StringUtils.isEmpty(lang)) {
                List<LangInfo> blankList = LangUtil.getSupportLangs();
                lang = lang.replace(NamingConstant.UNDERLINE, NamingConstant.STRIKE);
                boolean inList = false;
                if (!CollectionUtils.isEmpty(blankList)) {
                    for (LangInfo info : blankList) {
                        if (info.getLangCode().equalsIgnoreCase(lang)) {
                            inList = true;
                            break;
                    }
                }
                }
                if (inList) {
                    //在白名单内，则使用该语言
                    ThreadLocalUtil.setLocalVar(ResourceUtil.class.getName() + "_switch", lang);
                    ResourceUtil.switchLocale(ResourceUtil.toLocale(lang));
                } else {
                    log.warn("客户端发出'" + lang + "'语言的请求，目前系统不支持或不在配置列表内，故设定转为官方语言。");
                }
            }
        }
        chain.doFilter(request, response);
    }
}
