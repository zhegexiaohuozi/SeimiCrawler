package cn.wanghaomiao.seimi.utils;

import cn.wanghaomiao.seimi.annotation.validate.NotNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/16.
 */
public class StructValidator {
    private static final Logger logger = LoggerFactory.getLogger(StructValidator.class);
    public static boolean validateAnno(Object object){
        for (Field field:object.getClass().getDeclaredFields()){
            NotNull notNullCheck = field.getAnnotation(NotNull.class);
            if (notNullCheck!=null){
                try {
                    Object val = FieldUtils.readField(field,object,true);
                    if (val==null){
                        logger.error("Field={}.{} can not be null!",object.getClass().getSimpleName(),field.getName());
                        return false;
                    }
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return true;
    }

    public static boolean validateAllowRules(String[] rules,String target){
        boolean result = true;
        if (ArrayUtils.isEmpty(rules)){
            return true;
        }
        Assert.notNull(target,"rule target can not be null");
        for (String rule:rules){
            result = result&(target.matches(rule));
        }
        return result;
    }

    public static boolean validateDenyRules(String[] rules,String target){
        boolean result = true;
        if (ArrayUtils.isEmpty(rules)){
            return false;
        }
        Assert.notNull(target,"rule target can not be null");
        for (String rule:rules){
            result = result&(target.matches(rule));
        }
        return result;
    }

}
