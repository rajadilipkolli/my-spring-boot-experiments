package com.example.locks.config;

import lombok.NonNull;
import net.ttddyy.dsproxy.listener.ThreadQueryCountHolder;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

import static com.example.locks.utils.AppConstants.PROFILE_NOT_PROD;

@Configuration(proxyBeanMethods = false)
@Profile(PROFILE_NOT_PROD)
public class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
        if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {

            final ProxyFactory factory = new ProxyFactory(bean);
            factory.setProxyTargetClass(true);
            factory.addAdvice(new ProxyDataSourceInterceptor((DataSource) bean));
            return factory.getProxy();
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) {
        return bean;
    }

    private record ProxyDataSourceInterceptor(DataSource dataSource) implements MethodInterceptor {
        private ProxyDataSourceInterceptor(final DataSource dataSource) {
            this.dataSource =
                    ProxyDataSourceBuilder.create(dataSource)
                            .name("DS-Proxy")
                            .multiline()
                            .logQueryBySlf4j(SLF4JLogLevel.INFO)
                            .countQuery(new ThreadQueryCountHolder())
                            .build();
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            final Method proxyMethod =
                    ReflectionUtils.findMethod(
                            this.dataSource.getClass(), invocation.getMethod().getName());
            if (proxyMethod != null) {
                return proxyMethod.invoke(this.dataSource, invocation.getArguments());
            }
            return invocation.proceed();
        }
    }
}
