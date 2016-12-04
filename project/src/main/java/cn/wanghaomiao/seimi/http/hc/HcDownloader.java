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
package cn.wanghaomiao.seimi.http.hc;

import cn.wanghaomiao.seimi.core.SeimiDownloader;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.struct.BodyType;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import cn.wanghaomiao.xpath.exception.NoSuchAxisException;
import cn.wanghaomiao.xpath.exception.NoSuchFunctionException;
import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/6/26.
 */
public class HcDownloader implements SeimiDownloader {
    public HcDownloader(CrawlerModel crawlerModel) {
        this.crawlerModel = crawlerModel;
        if (crawlerModel.isUseCookie()){
            hc = HttpClientFactory.getHttpClient(crawlerModel.getHttpTimeOut(),crawlerModel.getCookieStore());
        }else {
            hc = HttpClientFactory.getHttpClient(crawlerModel.getHttpTimeOut());
        }
    }

    private CrawlerModel crawlerModel;
    private HttpClient hc;
    private RequestBuilder currentReqBuilder;
    private Request currentRequest;
    private HttpResponse httpResponse;
    private HttpContext httpContext = new BasicHttpContext();
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response process(Request request) throws Exception {
        currentReqBuilder = HcRequestGenerator.getHttpRequestBuilder(request,crawlerModel);
        currentRequest = request;
        httpResponse = hc.execute(currentReqBuilder.build(),httpContext);
        return renderResponse(httpResponse,request,httpContext);
    }

    @Override
    public Response metaRefresh(String nextUrl) throws Exception {
        if (!nextUrl.startsWith("http")){
            String prefix = getRealUrl(httpContext);
            nextUrl = prefix + nextUrl;
        }
        logger.info("Seimi refresh url to={} from={}",nextUrl,currentReqBuilder.getUri());
        currentReqBuilder.setUri(nextUrl);
        httpResponse = hc.execute(currentReqBuilder.build(),httpContext);
        return renderResponse(httpResponse,currentRequest,httpContext);
    }

    @Override
    public int statusCode() {
        return httpResponse.getStatusLine().getStatusCode();
    }

    private Response renderResponse(HttpResponse httpResponse, Request request, HttpContext httpContext){
        Response seimiResponse = new Response();
        HttpEntity entity = httpResponse.getEntity();
        seimiResponse.setSeimiHttpType(SeimiHttpType.APACHE_HC);
        seimiResponse.setRealUrl(getRealUrl(httpContext));
        seimiResponse.setUrl(request.getUrl());
        seimiResponse.setRequest(request);
        seimiResponse.setMeta(request.getMeta());

        if (entity != null) {
            Header referer = httpResponse.getFirstHeader("Referer");
            if (referer!=null){
                seimiResponse.setReferer(referer.getValue());
            }
            String contentTypeStr = entity.getContentType().getValue().toLowerCase();
            if (contentTypeStr.contains("text")||contentTypeStr.contains("json")||contentTypeStr.contains("ajax")){
                seimiResponse.setBodyType(BodyType.TEXT);
                try {
                    seimiResponse.setData(EntityUtils.toByteArray(entity));
                    ContentType contentType = ContentType.get(entity);
                    Charset charset = contentType.getCharset();
                    if (charset==null){
                        seimiResponse.setContent(new String(seimiResponse.getData(),"ISO-8859-1"));
                        String docCharset = renderRealCharset(seimiResponse);
                        seimiResponse.setContent(new String(seimiResponse.getContent().getBytes("ISO-8859-1"),docCharset));
                        seimiResponse.setCharset(docCharset);
                    }else {
                        seimiResponse.setContent(new String(seimiResponse.getData(),charset));
                        seimiResponse.setCharset(charset.name());
                    }
                } catch (Exception e) {
                    logger.error("no content data");
                }
            }else {
                seimiResponse.setBodyType(BodyType.BINARY);
                try {
                    seimiResponse.setData(EntityUtils.toByteArray(entity));
                    seimiResponse.setContent(StringUtils.substringAfterLast(request.getUrl(),"/"));
                } catch (Exception e) {
                    logger.error("no data can be read from httpResponse");
                }
            }
        }
        return seimiResponse;
    }

    private String renderRealCharset(Response response) throws NoSuchFunctionException, XpathSyntaxErrorException, NoSuchAxisException {
        String charset;
        JXDocument doc = response.document();
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

    private String getRealUrl(HttpContext httpContext){
        Object target = httpContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        Object reqUri = httpContext.getAttribute(HttpCoreContext.HTTP_REQUEST);
        if (target==null||reqUri==null){
            return null;
        }
        HttpHost t = (HttpHost) target;
        HttpUriRequest r = (HttpUriRequest)reqUri;
        return r.getURI().isAbsolute()?r.getURI().toString():t.toString()+r.getURI().toString();
    }
}
