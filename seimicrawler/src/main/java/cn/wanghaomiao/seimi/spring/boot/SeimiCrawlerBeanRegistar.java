package cn.wanghaomiao.seimi.spring.boot;

import cn.wanghaomiao.seimi.spring.common.SeimiCrawlerBaseConfig;
import cn.wanghaomiao.seimi.spring.common.SeimiCrawlerBeanPostProcessor;
import cn.wanghaomiao.seimi.spring.common.SeimiCrawlerBootstrapListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

/**
 * @author: github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2018/5/7.
 */
public class SeimiCrawlerBeanRegistar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinitionIfNotExists(registry, SeimiCrawlerBaseConfig.class, null);
        registerBeanDefinitionIfNotExists(registry, SeimiCrawlerBeanPostProcessor.class, null);
        registerBeanDefinitionIfNotExists(registry, SeimiCrawlerBootstrapListener.class, true);
    }

    private boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, Class<?> beanClass, Object... args) {
        if (registry.containsBeanDefinition(beanClass.getName())) {
            return false;
        }
        String[] candidates = registry.getBeanDefinitionNames();
        for (String candidate : candidates) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(candidate);
            if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {
                return false;
            }
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                beanDefinitionBuilder.addConstructorArgValue(arg);
            }
        }
        BeanDefinition annotationProcessor = beanDefinitionBuilder.getBeanDefinition();
        registry.registerBeanDefinition(beanClass.getName(), annotationProcessor);
        return true;
    }

}
