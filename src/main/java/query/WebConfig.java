package query;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/img/**",
                "/css/**",
                "/js/**",
                "/documents/**")
                .addResourceLocations(
                        "classpath:/img/",
                        "classpath:/css/",
                        "classpath:/js/",
                        "classpath:/documents/")
                .resourceChain(true)
            .addResolver(new PathResourceResolver());
    }

}


