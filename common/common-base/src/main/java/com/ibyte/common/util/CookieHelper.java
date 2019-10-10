package com.ibyte.common.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: <cookie帮助类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public class CookieHelper {

    /**
     * 获取制定cookie信息
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookie(HttpServletRequest request, String cookieName) {
        // 解决Cookie值等于号取不到的问题
        String cookieHeader = request.getHeader("Cookie");
        if (!StringUtils.isEmpty(cookieHeader)) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.indexOf("=") > -1 && cookie.substring(0, cookie.indexOf("="))
                        .equalsIgnoreCase(cookieName)) {
                    return cookie.substring(cookie.indexOf("=") + 1, cookie
                            .length());
                }
            }
        }
        return null;
    }
}
