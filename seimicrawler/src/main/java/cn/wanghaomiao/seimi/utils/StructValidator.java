/*
   Copyright 2015 Wang Haomiao<seimimaster@gmail.com>

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
package cn.wanghaomiao.seimi.utils;

import cn.wanghaomiao.seimi.annotation.validate.NotNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * @author SeimiMaster seimimaster@gmail.com
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
                    if (StringUtils.isBlank(String.valueOf(val))){
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
        if (ArrayUtils.isEmpty(rules)){
            return true;
        }
        Assert.notNull(target,"rule target can not be null");
        for (String rule:rules){
            if (target.matches(rule)){
                return true;
            }
        }
        return false;
    }

    public static boolean validateDenyRules(String[] rules,String target){
        if (ArrayUtils.isEmpty(rules)){
            return false;
        }
        Assert.notNull(target,"rule target can not be null");
        for (String rule:rules){
            if (target.matches(rule)){
                return true;
            }
        }
        return false;
    }

}
