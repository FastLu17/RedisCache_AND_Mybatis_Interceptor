package com.luxf.mybatis.bootmybatisdemo.resolver;

import com.alibaba.fastjson.JSONObject;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.List;

/**
 * 必须要注册到WebMvc-->实现{@link WebMvcConfigurationSupport#addArgumentResolvers(List)}，否则解析器无法生效、
 *
 * @author 小66
 * @date 2020-06-17 9:10
 **/
@Component
public class ArgumentResolverHandler implements HandlerMethodArgumentResolver {
    private static final String CONTENT_TYPE = "application/json";
    private static final String GET_METHOD_NAME = "GET";

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> type = methodParameter.getParameterType();
        // 参数的类型是RestParameterBean、才进行 resolveArgument() 操作、
        return type.equals(RestParameterBean.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        assert request != null;

        if (!GET_METHOD_NAME.equals(request.getMethod()) && request.getContentType().contains(CONTENT_TYPE)) {
            // 获取请求的JSON参数
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            int i;
            while ((i = reader.read(buf)) != -1) {
                sb.append(buf, 0, i);
            }
            return new RestParameterBean(JSONObject.parseObject(sb.toString()), sb.toString());
        }
        return RestParameterBean.EMPTY;
    }
}
