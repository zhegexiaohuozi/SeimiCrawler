package cn.wanghaomiao.seimi.httpd;

import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.struct.Request;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.handler.StringHttpHandler;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/16.
 */
public class PushRequestHttpdHandler extends StringHttpHandler {
    private SeimiQueue seimiQueue;
    private String crawlerName;
    private final static String HTTP_API_REQ_DATA_PARAM_KEY = "req";
    private Logger logger = LoggerFactory.getLogger(getClass());
    public PushRequestHttpdHandler(SeimiQueue seimiQueue, String crawlerName) {
        super("application/json", "");
        this.seimiQueue = seimiQueue;
        this.crawlerName = crawlerName;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
        String seimiReq = request.queryParam(HTTP_API_REQ_DATA_PARAM_KEY);
        if (StringUtils.isBlank(seimiReq)){
            seimiReq = request.postParam(HTTP_API_REQ_DATA_PARAM_KEY);
        }
        Map<String,String> body = new HashMap<>();
        try {
            Request seimiRequest = JSON.parseObject(seimiReq,Request.class);
            seimiRequest.setCrawlerName(crawlerName);
            //todo:必填校验
            if (seimiRequest.getUrl()!=null&&seimiRequest.getCallBack()!=null){
                seimiQueue.push(seimiRequest);
            }else {
                logger.warn("SeimiRequest={} is illegal",JSON.toJSONString(seimiRequest));
            }
            body.put("data","ok");
            body.put("code","0");
        }catch (Exception e){
            logger.error("parse Seimi request error,receive data={}",seimiReq,e);
            body.put("data","err:"+e.getMessage());
            body.put("code","1");
        }
        response.content(JSON.toJSONString(body)).charset(Charset.forName("UTF-8")).header("server", "SeimiCrawler")
                .header("Content-Type", "application/json; charset=UTF-8").end();
    }
}
