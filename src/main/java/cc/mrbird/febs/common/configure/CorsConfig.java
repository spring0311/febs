package cc.mrbird.febs.common.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * AJAX请求跨域
 * @author weiZiHao
 * @date 2020/9/16
 */
@Configuration
public class CorsConfig extends WebMvcConfigurerAdapter {
    static final String ORIGINS[] = new String[]{"GET", "POST", "PUT", "DELETE"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowCredentials(true).allowedMethods(ORIGINS)
                .maxAge(3600);
    }
}
