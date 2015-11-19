package cn.wanghaomiao.seimi.httpd;

import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.struct.Request;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/16.
 */
public class PushRequestHttpdHandler extends HttpRequestProcessor {
    private final static String HTTP_API_REQ_DATA_PARAM_KEY = "req";
    private Logger logger = LoggerFactory.getLogger(getClass());
    public PushRequestHttpdHandler(SeimiQueue seimiQueue, String crawlerName) {
        super(seimiQueue,crawlerName);
    }

    @Override
    public void handleHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json; charset=utf-8");
        String seimiReq = request.getParameter(HTTP_API_REQ_DATA_PARAM_KEY);
        Map<String,String> body = new HashMap<>();
        try {
            Request seimiRequest = JSON.parseObject(seimiReq,Request.class);
            seimiRequest.setCrawlerName(crawlerName);
            //todo:必填校验
            if (seimiRequest.getUrl()!=null&&seimiRequest.getCallBack()!=null){
                seimiQueue.push(seimiRequest);
                logger.info("Receive an request from http api,request={}",JSON.toJSONString(seimiReq));
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
        PrintWriter out = response.getWriter();
        out.println(JSON.toJSONString(body));
    }
}
