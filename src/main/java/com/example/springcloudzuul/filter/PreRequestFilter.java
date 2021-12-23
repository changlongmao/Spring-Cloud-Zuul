package com.example.springcloudzuul.filter;

import com.example.springcloudzuul.constant.ZuulConst;
import com.example.springcloudzuul.utils.IpUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @ClassName MyZuulFilter
 * @Author Spark
 * @Date 2019/7/19 10:32
 * @Description * PRE:这种过滤器在请求被路由之前调用。可利用这种过滤器实现身份验证、在集群中选择请求的微服务，记录调试信息等。
 * *ROUTING:这种过滤器将请求路由到微服务。这种过滤器用于构建发送给微服务的请求，并使用Apache HttpClient或Netflix Ribbon请求微服务。
 * *POST:这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的HTTP header、收集统计信息和指标、将响应从微服务发送给客户端等。
 * *ERROR:在其他阶段发送错误时执行该过滤器。
 * @Version v1.2
 **/
@Component
public class PreRequestFilter extends ZuulFilter {

    private final static Logger logger = LoggerFactory.getLogger(PreRequestFilter.class);

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    /**
     * 过滤顺序
     *
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * true 执行过滤逻辑
     * false 不执行
     *
     * @return
     */
    @Override
    public boolean shouldFilter() {

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        //只过滤OPTIONS 请求
        if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        // 记录接口请求时间
        long startTime = System.currentTimeMillis();
        RequestContext.getCurrentContext().set(ZuulConst.START_TIME_KEY, startTime);
        RequestContext.getCurrentContext().set(ZuulConst.ORIGINAL_REQUEST_URI, request.getRequestURI());

        return false;
    }

    /**
     * 过滤逻辑处理
     * 请求类型为：OPTIONS 时会执行该过滤器
     * @return
     */
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse httpServletResponse = ctx.getResponse();
        HttpServletRequest httpServletRequest = ctx.getRequest();

        logger.info("HTTP版本:{}，客户端IP地址:{}", httpServletRequest.getProtocol(), IpUtil.getIpAddr(httpServletRequest));
        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT");
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,Accept,Accept-Encoding,"
                + "Accept-Language,Connection,Content-Length,Host,Origin,User-Agent,version,"
                + "platform,deviceId,token,aid,rid,tg,X-Requested-With,Referer,enableFaker,"
                + "AuthToken,authtoken,AppCode,appCode,appcode,cid");
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "*");
        httpServletResponse.addHeader("Access-Control-Max-Age", "18000L");
        httpServletResponse.addHeader("Vary", "Origin,Access-Control-Request-Method,Access-Control-Request-Headers");

        Thread thread = Thread.currentThread();
        logger.info("【转发之前】请求地址:{}，请求类型:{}，线程id:{}，线程名称:{}",
                httpServletRequest.getRequestURI(), httpServletRequest.getMethod(), thread.getId(), thread.getName());

        //不再路由
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(200);
        return null;
    }


    private String getBodyData(HttpServletRequest request) {
        BufferedReader reader = null;
        StringBuffer data = new StringBuffer();
        try {
            String line = "";
            reader = request.getReader();
            while (null != (line = reader.readLine())) {
                data.append(line);
            }
        } catch (IOException e) {
            logger.error("get request params error: ", e);
        } finally {
            if (null == reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("close BufferedReader error: ", e);
                }
            }
        }
        return data.toString();
    }
}
