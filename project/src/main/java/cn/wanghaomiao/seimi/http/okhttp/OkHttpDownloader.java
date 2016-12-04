/*
   Copyright 2015 Wang Haomiao<et.tw@163.com>

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
package cn.wanghaomiao.seimi.http.okhttp;

import cn.wanghaomiao.seimi.core.SeimiDownloader;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.struct.BodyType;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/6/26.
 */
public class OkHttpDownloader implements SeimiDownloader {

    public OkHttpDownloader(CrawlerModel crawlerModel){
        this.crawlerModel = crawlerModel;
    }

    private CrawlerModel crawlerModel;
    private Request currentRequest;
    private okhttp3.Request.Builder currentRequestBuilder;
    private OkHttpClient okHttpClient;
    private okhttp3.Response lastResponse;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response process(Request request) throws Exception {
        currentRequest = request;
        OkHttpClient.Builder hcBuilder = OkHttpClientBuilderProvider.getInstance();
        if (crawlerModel.isUseCookie()){
            hcBuilder.cookieJar(crawlerModel.getOkHttpCookiesManager());
        }
        if (crawlerModel.getStdProxy()!=null){
            hcBuilder.proxy(crawlerModel.getStdProxy());
        }
        hcBuilder.readTimeout(crawlerModel.getHttpTimeOut(), TimeUnit.MILLISECONDS);
        okHttpClient = hcBuilder.build();
        currentRequestBuilder = OkHttpRequestGenerator.getOkHttpRequesBuilder(request,crawlerModel);
        lastResponse = okHttpClient.newCall(currentRequestBuilder.build()).execute();
        return renderResponse(lastResponse,request);
    }

    @Override
    public Response metaRefresh(String nextUrl) throws Exception {
        HttpUrl lastUrl = lastResponse.request().url();
        if (!nextUrl.startsWith("http")){
            String prefix = lastUrl.scheme()+"://"+lastUrl.host()+lastUrl.encodedPath();
            nextUrl = prefix + nextUrl;
        }
        logger.info("Seimi refresh url to={} from={}",nextUrl,lastUrl.toString());
        currentRequestBuilder.url(nextUrl);
        lastResponse = okHttpClient.newCall(currentRequestBuilder.build()).execute();
        return renderResponse(lastResponse,currentRequest);
    }

    @Override
    public int statusCode() {
        return lastResponse.code();
    }


    private Response renderResponse(okhttp3.Response hcResponse,Request request){
        Response seimiResponse = new Response();
        seimiResponse.setSeimiHttpType(SeimiHttpType.OK_HTTP3);
        seimiResponse.setRealUrl(lastResponse.request().url().toString());
        seimiResponse.setUrl(request.getUrl());
        seimiResponse.setRequest(request);
        seimiResponse.setMeta(request.getMeta());
        seimiResponse.setReferer(hcResponse.header("Referer"));

        ResponseBody okResponseBody = hcResponse.body();
        if (okResponseBody!=null){
            String type = okResponseBody.contentType().type().toLowerCase();
            String subtype = okResponseBody.contentType().subtype().toLowerCase();
            if (type.contains("text")||type.contains("json")||type.contains("ajax")||subtype.contains("json")
                    ||subtype.contains("ajax")){
                seimiResponse.setBodyType(BodyType.TEXT);
                try {
                    byte[] data = okResponseBody.bytes();
                    String utfContent = new String(data,"UTF-8");
                    String charsetFinal = renderRealCharset(utfContent);
                    if (charsetFinal.equals("UTF-8")){
                        seimiResponse.setContent(utfContent);
                    }else {
                        seimiResponse.setContent(new String(data,charsetFinal));
                    }
                } catch (Exception e) {
                    logger.error("no content data");
                }
            }else {
                seimiResponse.setBodyType(BodyType.BINARY);
                try {
                    seimiResponse.setData(okResponseBody.bytes());
                } catch (Exception e) {
                    logger.error("no content data");
                }
            }
        }
        return seimiResponse;
    }

    private String renderRealCharset(String content) throws XpathSyntaxErrorException {
        String charset;
        JXDocument doc = new JXDocument(content);
        charset = StrFormatUtil.getFirstEmStr(doc.sel("//meta[@charset]/@charset"),"").trim();
        if (StringUtils.isBlank(charset)){
            charset = StrFormatUtil.getFirstEmStr(doc.sel("//meta[@http-equiv='charset']/@content"),"").trim();
        }
        if (StringUtils.isBlank(charset)){
            String ct = StringUtils.join(doc.sel("//meta[@http-equiv='Content-Type']/@content|//meta[@http-equiv='content-type']/@content"),";").trim();
            charset = StrFormatUtil.parseCharset(ct.toLowerCase());
        }
        return StringUtils.isNotBlank(charset)?charset:"UTF-8";
    }
}
