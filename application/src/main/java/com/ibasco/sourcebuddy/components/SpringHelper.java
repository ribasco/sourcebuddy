package com.ibasco.sourcebuddy.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringHelper {

    private static final Logger log = LoggerFactory.getLogger(SpringHelper.class);

    private ApplicationContext context;

    private ApplicationEventPublisher publisher;

    private static SpringHelper instance;

    @Autowired
    private SpringHelper(ApplicationEventPublisher publisher, ApplicationContext context) {
        log.debug("Initializing SpringUtil");
        this.context = context;
        this.publisher = publisher;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanId) {
        return (T) context.getBean(beanId);
    }

    public <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public <T> T getBean(Class<T> beanClass, Object... args) {
        return getBean(beanClass, true, args);
    }

    public <T> T getBean(Class<T> beanClass, boolean procssOptional, Object... args) {
        if (procssOptional) {
            //Automatically proceess Optional types
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Optional) {
                    Optional optArg = (Optional) arg;
                    if (optArg.isPresent())
                        args[i] = optArg.get();
                    else
                        args[i] = null;
                }
            }
        }
        return context.getBean(beanClass, args);
    }

    public <T> void autowire(T bean) {
        getContext().getAutowireCapableBeanFactory().autowireBean(bean);
    }

    public ConfigurableApplicationContext getContext() {
        return (ConfigurableApplicationContext) context;
    }

    public <T> T getBean(String beanId, Class<T> beanClass) {
        return context.getBean(beanId, beanClass);
    }

    public boolean beanExists(String beanId) {
        return context.containsBeanDefinition(beanId);
    }

    public <T> T createBean(String beanId, Class<T> beanClass) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.context;
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
    public void registerBean(String beanId, Object instance) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.context;
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(instance.getClass());
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        registry.registerBeanDefinition(beanId, beanDef);
        context.getBeanFactory().registerSingleton(beanId, instance);
        log.debug("registerBean() :: Registered bean instance: Id: {}, Class Name: {}, Instance Id: {} (Result: {} = {}, Has Definition = {})", beanId, instance.getClass().getSimpleName(), instance.hashCode(), context.getBean(beanId).getClass().getSimpleName(), context.getBean(beanId).hashCode(), context.containsBeanDefinition(beanId));
    }

    public void registerSingleton(String beanId, Object instance) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.context;
        context.getBeanFactory().registerSingleton(beanId, instance);
    }

    public void publishEvent(ApplicationEvent event) {
        if (event == null)
            throw new IllegalArgumentException("Event cannot be null");
        publisher.publishEvent(event);
    }
}
