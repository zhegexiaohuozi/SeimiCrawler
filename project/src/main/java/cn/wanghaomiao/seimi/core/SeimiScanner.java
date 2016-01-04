package cn.wanghaomiao.seimi.core;
/*
   Copyright 2016 汪浩淼(Haomiao Wang)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 上下文加载器
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/6/17.
 */
public class SeimiScanner {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String RESOURCE_PATTERN = "**/%s/**/*.class";
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ScanConfig.class);

    @SafeVarargs
    public final Set<Class<?>> scan(String[] confPkgs, Class<? extends Annotation>... annotationTags){
        Set<Class<?>> resClazzSet = new HashSet<>();
        List<AnnotationTypeFilter> typeFilters = new LinkedList<>();
        if (ArrayUtils.isNotEmpty(annotationTags)){
            for (Class<? extends Annotation> annotation : annotationTags) {
                typeFilters.add(new AnnotationTypeFilter(annotation, false));
            }
        }
        if (ArrayUtils.isNotEmpty(confPkgs)) {
            for (String pkg : confPkgs) {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX  + String.format(RESOURCE_PATTERN,ClassUtils.convertClassNameToResourcePath(pkg));
                try {
                    Resource[] resources = this.resourcePatternResolver.getResources(pattern);
                    MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
                    for (Resource resource : resources) {
                        if (resource.isReadable()) {
                            MetadataReader reader = readerFactory.getMetadataReader(resource);
                            String className = reader.getClassMetadata().getClassName();
                            if (ifMatchesEntityType(reader, readerFactory,typeFilters)) {
                                Class<?> curClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                                context.register(curClass);
                                resClazzSet.add(curClass);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("扫描提取[{}]包路径下，标记了注解[{}]的类出现异常", pattern,StringUtils.join(typeFilters,","));
                }
            }
        }
        if (!context.isActive()){
            context.refresh();
        }
        return resClazzSet;
    }

    /**
     * 检查当前扫描到的类是否含有任何一个指定的注解标记
     * @param reader
     * @param readerFactory
     * @return ture/false
     */
    private boolean ifMatchesEntityType(MetadataReader reader, MetadataReaderFactory readerFactory,List<AnnotationTypeFilter> typeFilters) {
        if (!CollectionUtils.isEmpty(typeFilters)) {
            for (TypeFilter filter : typeFilters) {
                try {
                    if (filter.match(reader, readerFactory)) {
                        return true;
                    }
                } catch (IOException e) {
                    logger.error("过滤匹配类型时出错 {}",e.getMessage());
                }
            }
        }
        return false;
    }

    public ApplicationContext getContext(){
        return this.context;
    }
}
