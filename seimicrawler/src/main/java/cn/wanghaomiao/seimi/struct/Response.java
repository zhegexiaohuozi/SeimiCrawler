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
package cn.wanghaomiao.seimi.struct;

import cn.wanghaomiao.seimi.annotation.Xpath;
import cn.wanghaomiao.seimi.exception.SeimiBeanResolveException;
import cn.wanghaomiao.seimi.exception.SeimiProcessExcepiton;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.utils.GenericUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 抓取请求的返回结果
 *
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 *         Date: 2015/05/12.
 */
public class Response extends CommonObject {
    private BodyType bodyType;
    private Request request;
    private String charset;
    private String referer;
    private byte[] data;
    private String content;
    /**
     * 这个主要用于存储上游传递的一些自定义数据
     */
    private Map<String, Object> meta;
    private String url;
    private Map<String, String> params;
    /**
     * 网页内容真实源地址
     */
    private String realUrl;
    /**
     * 此次请求结果的http处理器类型
     */
    private SeimiHttpType seimiHttpType;


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getRealUrl() {
        return realUrl;
    }

    public void setRealUrl(String realUrl) {
        this.realUrl = realUrl;
    }

    public SeimiHttpType getSeimiHttpType() {
        return seimiHttpType;
    }

    public void setSeimiHttpType(SeimiHttpType seimiHttpType) {
        this.seimiHttpType = seimiHttpType;
    }

    /**
     * 通过bean中定义的Xpath注解进行自动填充
     *
     * @param bean --
     * @param <T> --
     * @return --
     * @throws Exception --
     */
    public <T> T render(Class<T> bean) throws Exception {
        if (bodyType.equals(BodyType.TEXT)) {
            return parse(bean, this.content);
        } else {
            throw new SeimiProcessExcepiton("can not parse struct from binary");
        }
    }

    public JXDocument document() {
        return BodyType.TEXT.equals(bodyType) && content != null ? JXDocument.create(content) : null;
    }

    public void saveTo(File targetFile) {
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                FileChannel fo = fileOutputStream.getChannel();
        ) {

            File pf = targetFile.getParentFile();
            if (!pf.exists()) {
                pf.mkdirs();
            }
            if (BodyType.TEXT.equals(bodyType)) {
                fo.write(ByteBuffer.wrap(getContent().getBytes()));
            } else {
                fo.write(ByteBuffer.wrap(getData()));
            }
        } catch (Exception e) {
            throw new SeimiProcessExcepiton(e);
        }
    }

    private <T> T parse(Class<T> target, String text) throws Exception {
        T bean = target.newInstance();
        final List<Field> props = new LinkedList<>();
        ReflectionUtils.doWithFields(target, props::add);
        JXDocument jxDocument = JXDocument.create(text);
        for (Field f:props){
            Xpath xpathInfo = f.getAnnotation(Xpath.class);
            if (xpathInfo!=null){
                String xpath = xpathInfo.value();
                List<Object> res = jxDocument.sel(xpath);
                synchronized (f){
                    boolean accessFlag = f.isAccessible();
                    f.setAccessible(true);
                    f.set(bean,defaultCastToTargetValue(target, f, res));
                    f.setAccessible(accessFlag);
                }
            }
        }
        return bean;
    }

    private String upperFirst(String str){
        return str!=null?str.substring(0,1).toUpperCase()+str.substring(1):null;
    }

    private Object defaultCastToTargetValue(Class target,Field field,List<Object> xpathRes){
        Method getter = ReflectionUtils.findMethod(target,"get"+upperFirst(field.getName()));
        if (getter!=null){
            if (List.class.equals(getter.getReturnType())){
                Class[] componentClazzs = GenericUtils.getActualClass(getter.getGenericReturnType());
                if (componentClazzs!=null&&componentClazzs.length>0){
                    Class componentClass = componentClazzs[0];
                    return getObject(xpathRes, componentClass);
                }
            }else if (!Collection.class.isAssignableFrom(getter.getReturnType())&&getter.getReturnType().isArray()){
                Class componentClass = getter.getReturnType().getComponentType();
                return getObject(xpathRes, componentClass);
            }else if (!Collection.class.isAssignableFrom(getter.getReturnType())&&GenericUtils.isNumber(field.getType())){
                return GenericUtils.castToNumber(field.getType(), StringUtils.join(xpathRes,""));
            }else if (!Collection.class.isAssignableFrom(getter.getReturnType())&&String.class.isAssignableFrom(field.getType())){
                return StringUtils.join(xpathRes,"");
            }
        }
        return null;
    }

    private Object getObject(List<Object> xpathRes, Class componentClass) {
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
}
