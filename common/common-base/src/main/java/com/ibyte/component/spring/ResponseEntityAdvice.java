package com.ibyte.component.spring;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.dto.Response;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @Description: <Spring内置返回格式处理>
 *
 * @author li.Shangzhi
 * @Date: 2019年10月9日 21:22:12
 */
@ControllerAdvice({NamingConstant.BASE_PACKAGE, "org.springframework"})
public class ResponseEntityAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> aClass) {
        String requeireType = methodParameter.getGenericParameterType().getTypeName();
        return requeireType.startsWith(ResponseEntity.class.getTypeName())
                && AbstractJackson2HttpMessageConverter.class.isAssignableFrom(aClass);
    }

    @Override
    public Object beforeBodyWrite(Object o,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse HttpServletResponse) {
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            javax.servlet.http.HttpServletResponse response = ((ServletServerHttpResponse) HttpServletResponse).getServletResponse();
            if (hasError(HttpStatus.valueOf(response.getStatus()))) {
                return new Response(false, "status." + response.getStatus(), null, o);
            } else {
                return Response.ok(o);
            }
        }
        return o;
    }

    /**
     * 是否有错误处理
     *
     * @param statusCode
     * @return
     */
    protected boolean hasError(HttpStatus statusCode) {
        return (statusCode.series() == HttpStatus.Series.CLIENT_ERROR ||
                statusCode.series() == HttpStatus.Series.SERVER_ERROR);
    }
}