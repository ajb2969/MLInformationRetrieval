package query;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/img/**",
                "/css/**",
                "/js/**",
                "/documents/**",
                "/html_documents/**")
                .addResourceLocations(
                        "classpath:/img/",
                        "classpath:/css/",
                        "classpath:/js/",
                        "classpath:/documents/",
                        "classpath:/html_documents/")
                .resourceChain(true)
            .addResolver(new PathResourceResolver());
    }

}


