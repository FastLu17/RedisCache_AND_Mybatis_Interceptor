package com.luxf.mybatis.bootmybatisdemo.config;

import com.luxf.mybatis.bootmybatisdemo.resolver.ArgumentResolverHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * WebMvcConfiguration的配置类、
 *
 * @author 小66
 * @date 2020-06-17 10:21
 **/
@Configuration
// 扫描@Controller和@RestController
@ComponentScan(
        useDefaultFilters = false,
        basePackages = {"com.luxf"},
        includeFilters = {@ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                value = {Controller.class}
        ), @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                value = {RestController.class}
        )}
)
public class ApplicationMvcConfiguration extends WebMvcConfigurationSupport {
    /**
     * 自定义的请求参数解析处理器、
     */
    private final ArgumentResolverHandler argumentResolverHandler;

    @Autowired
    public ApplicationMvcConfiguration(ArgumentResolverHandler argumentResolverHandler) {
        this.argumentResolverHandler = argumentResolverHandler;
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // 将自定义的HandlerMethodArgumentResolver注册、
        argumentResolvers.add(argumentResolverHandler);
        super.addArgumentResolvers(argumentResolvers);
    }


}
