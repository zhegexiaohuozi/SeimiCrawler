package cn.wanghaomiao.seimi.http;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * 之所以要自行覆盖默认实现，是因为默认实现在post/redirect/post这种情况下不会传递原有请求的数据信息，只会传递一个uri其他的都丢了，
 * 这显然是非常不理想的，所以必须重写覆盖。结果还是很不错的。
 * @author 汪浩淼 [et.tw@163.com]
 */
public class SeimiRedirectStrategy extends LaxRedirectStrategy {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        URI uri = getLocationURI(request, response, context);
        String method = request.getRequestLine().getMethod();
        if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)){
            try {
                HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) request;
                httpRequestWrapper.setURI(uri);
                httpRequestWrapper.removeHeaders("Content-Length");
                return httpRequestWrapper;
            }catch (Exception e){
                logger.error("强转为HttpRequestWrapper出错");
            }
            return new HttpPost(uri);
        }else {
            return new HttpGet(uri);
        }
    }
}
