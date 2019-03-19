package com.ibasco.sourcebuddy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(SpringUtil.class);

    private static ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanId) {
        return (T) applicationContext.getBean(beanId);
    }

    public static <T> T getBean(String beanId, Class<T> beanClass) {
        return applicationContext.getBean(beanId, beanClass);
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }

    public static boolean beanExists(String beanId) {
        return applicationContext.containsBeanDefinition(beanId);
    }

    public static <T> T createBean(String beanId, Class<T> beanClass) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
        AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        registry.registerBeanDefinition(beanId, builder.getBeanDefinition());
        return context.getBean(beanId, beanClass);
    }

    /**
     * Register and create a {@link org.springframework.beans.factory.config.BeanDefinition} to an existing instance
     *
     * @param beanId
     *         The bean id to be associated with the instance
     * @param instance
     *         The instance to be registered to spring
     */
    public static void registerBean(String beanId, Object instance) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(instance.getClass());
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        registry.registerBeanDefinition(beanId, beanDef);
        context.getBeanFactory().registerSingleton(beanId, instance);
        log.debug("Registered bean instance: {} = {} (Result: {} = {}, Has Definition = {})", instance.getClass().getSimpleName(), instance.hashCode(), context.getBean(beanId).getClass().getSimpleName(), context.getBean(beanId).hashCode(), context.containsBeanDefinition(beanId));
    }
}
