package cn.wanghaomiao.seimi.http;
/*
   Copyright 2015 - now original author

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


import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

/**
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2014/11/13.
 */
public class HttpClientFactory {
    public static HttpClient getHttpClient(){
        return cliBuilder(10000).build();
    }
    public static HttpClient getHttpClient(int timeout){
        return cliBuilder(timeout).build();
    }
    public static HttpClient getHttpClient(int timeout,CookieStore cookieStore){
        return cliBuilder(timeout).setDefaultCookieStore(cookieStore).build();
    }

    public static HttpClientBuilder cliBuilder(int timeout){
        HttpRequestRetryHandler retryHander = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount > 3) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof java.net.SocketTimeoutException) {
                    //特殊处理
                    return true;
                }
                if (exception instanceof InterruptedIOException) {
                    // Timeout
                    return true;
                }
                if (exception instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }

                if (exception instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };
        RedirectStrategy redirectStrategy = new SeimiRedirectStrategy();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = HttpClientConnectionManagerProvider.getHcPoolInstance();
        return HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(poolingHttpClientConnectionManager)
                .setRedirectStrategy(redirectStrategy).setRetryHandler(retryHander);
    }
}
