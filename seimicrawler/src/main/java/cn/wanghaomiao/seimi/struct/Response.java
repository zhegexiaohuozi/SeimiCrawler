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

import cn.wanghaomiao.seimi.core.SeimiBeanResolver;
import cn.wanghaomiao.seimi.exception.SeimiProcessExcepiton;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import org.seimicrawler.xpath.JXDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
     * @param bean
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T render(Class<T> bean) throws Exception {
        if (bodyType.equals(BodyType.TEXT)) {
            return SeimiBeanResolver.parse(bean, this.content);
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
}
