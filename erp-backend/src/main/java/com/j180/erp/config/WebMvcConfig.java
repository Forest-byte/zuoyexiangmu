package com.j180.erp.config;

import com.j180.erp.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：鉴权拦截器注册（登录接口放行）+ CORS + 前端静态资源伺服
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    /**
     * 前端静态站点（Vue3 + Element Plus 静态资源）由后端伺服：
     * 依次尝试多个候选位置，首个存在者生效——
     * ① 从 erp-backend 目录启动：../erp-system/erp-frontend/
     * ② 从 erp-system 目录启动/旧路径兼容：../erp-frontend/
     * ③ classpath 静态目录兜底
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "file:C:/Users/syz/Desktop/erp-system/erp-frontend/",
                        "file:../erp-system/erp-frontend/",
                        "file:../erp-frontend/",
                        "classpath:/static/",
                        "classpath:/public/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
