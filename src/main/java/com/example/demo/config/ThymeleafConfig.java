package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.context.IExpressionContext;
import java.util.Collections;
import java.util.Set;

/**
 * Cấu hình Thymeleaf Custom Dialect
 * Cho phép sử dụng các utility methods trong templates
 * Sử dụng: [[${#format.vnd(amount)}]] trong templates
 */
@Configuration
public class ThymeleafConfig {

    /**
     * Đăng ký FormatUtility như một bean
     * Sau đó inject vào Model của controller thông qua FormatInterceptor
     */
    @Bean
    FormatUtility formatUtility() {
        return new FormatUtility();
    }

    /**
     * Register FormatUtility as #format in Thymeleaf
     */
    @Bean
    IExpressionObjectDialect formatDialect(FormatUtility formatUtility) {
        return new IExpressionObjectDialect() {
            @Override
            public String getName() {
                return "format";
            }
            
            @Override
            public IExpressionObjectFactory getExpressionObjectFactory() {
                return new IExpressionObjectFactory() {
                    @Override
                    public Object buildObject(IExpressionContext context, String expressionObjectName) {
                        if ("format".equals(expressionObjectName)) {
                            return formatUtility;
                        }
                        return null;
                    }
                    
                    @Override
                    public boolean isCacheable(String expressionObjectName) {
                        return true;
                    }
                    
                    @Override
                    public Set<String> getAllExpressionObjectNames() {
                        return Collections.singleton("format");
                    }
                };
            }
        };
    }
}
