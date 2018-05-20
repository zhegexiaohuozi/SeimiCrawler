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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2015/7/21.
 */
public class StrFormatUtil {
    private static final Pattern charsetPattern = Pattern.compile("charset=([1-9a-zA-Z-]+)$");
    public static String info(String pattern,Object... params){
        for (Object p:params){
            pattern = StringUtils.replaceOnce(pattern, "{}", p.toString());
        }
        return pattern;
    }

    public static String getFirstEmStr(List<Object> list,String def){
        if (CollectionUtils.isEmpty(list)){
            return def;
        }
        return list.get(0).toString();
    }

    public static String parseCharset(String target){
        Matcher matcher = charsetPattern.matcher(target);
        if (matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    public static String getHost(String url){
        String[] pies = url.split("/");
        return StringUtils.join(ArrayUtils.subarray(pies,0,3),"/");
    }

    public static String getDodmain(String url){
        String[] pies = url.split("/");
        return StringUtils.substringAfter(pies[2],".");
    }
}
