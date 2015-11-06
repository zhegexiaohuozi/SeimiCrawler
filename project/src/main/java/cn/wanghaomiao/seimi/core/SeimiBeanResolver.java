package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.annotation.Xpath;
import cn.wanghaomiao.seimi.exception.SeimiBeanResolveException;
import cn.wanghaomiao.seimi.utils.GenericUtils;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 根据Bean中字段定义的XPath路径自动提取数据
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/6/17.
 */
@SuppressWarnings("uncheck")
public class SeimiBeanResolver {
    public static <T> T parse(Class<T> target,String text) throws Exception {
        T bean = target.newInstance();
        final List<Field> props = new LinkedList<>();
        ReflectionUtils.doWithFields(target, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                props.add(field);
            }
        });
        JXDocument jxDocument = new JXDocument(text);
        for (Field f:props){
            Xpath xpathInfo = f.getAnnotation(Xpath.class);
            if (xpathInfo!=null){
                String xpath = xpathInfo.value();
                List<Object> res = jxDocument.sel(xpath);
                boolean accessFlag = f.isAccessible();
                f.setAccessible(true);
                f.set(bean,defaultCastToTargetValue(target, f, res));
                f.setAccessible(accessFlag);
            }
        }
        return bean;
    }

    private static String upperFirst(String str){
        return str!=null?str.substring(0,1).toUpperCase()+str.substring(1):null;
    }

    private static Object defaultCastToTargetValue(Class target,Field field,List<Object> xpathRes){
        Method getter = ReflectionUtils.findMethod(target,"get"+upperFirst(field.getName()));
        if (getter!=null){
            if (List.class.equals(getter.getReturnType())){
                Class[] componentClazzs = GenericUtils.getActualClass(getter.getGenericReturnType());
                if (componentClazzs!=null&&componentClazzs.length>0){
                    Class componentClass = componentClazzs[0];
                    if (String.class.isAssignableFrom(componentClass)){
                        List<String> resTmp = new LinkedList<>();
                        for (Object obj:xpathRes){
                            if (obj instanceof Element){
                                resTmp.add(((Element)obj).html());
                            }else {
                                resTmp.add(obj.toString());
                            }
                        }
                        return resTmp;
                    }else if (Element.class.isAssignableFrom(componentClass)){
                        return xpathRes;
                    }else if (GenericUtils.isNumber(componentClass)){
                        List resTmp = new LinkedList();
                        for (Object obj:xpathRes){
                            resTmp.add(GenericUtils.castToNumber(componentClass,obj.toString()));
                        }
                        return resTmp;
                    }else {
                        throw new SeimiBeanResolveException("not support field type");
                    }
                }
            }else if (!Collection.class.isAssignableFrom(getter.getReturnType())&&getter.getReturnType().isArray()){
                Class componentClass = getter.getReturnType().getComponentType();
                if (String.class.isAssignableFrom(componentClass)){
                    List<String> resTmp = new LinkedList<>();
                    for (Object obj:xpathRes){
                        if (obj instanceof Element){
                            resTmp.add(((Element)obj).html());
                        }else {
                            resTmp.add(obj.toString());
                        }
                    }
                    return resTmp;
                }else if (Element.class.isAssignableFrom(componentClass)){
                    return xpathRes;
                }else if (GenericUtils.isNumber(componentClass)){
                    List resTmp = new LinkedList();
                    for (Object obj:xpathRes){
                        resTmp.add(GenericUtils.castToNumber(componentClass,obj.toString()));
                    }
                    return resTmp;
                }else {
                    throw new SeimiBeanResolveException("not support field type");
                }
            }else if (!Collection.class.isAssignableFrom(getter.getReturnType())&&GenericUtils.isNumber(field.getType())){
                return GenericUtils.castToNumber(field.getType(), StringUtils.join(xpathRes,""));
            }else if (!Collection.class.isAssignableFrom(getter.getReturnType())&&String.class.isAssignableFrom(field.getType())){
                return StringUtils.join(xpathRes,"");
            }
        }
        return null;
    }
}
