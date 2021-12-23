package com.example.springcloudzuul.filter;

import com.example.springcloudzuul.constant.ZuulConst;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName PostRequestFilter
 * @Author Spark
 * @Date 2019/7/23 15:07
 * @Description TODO
 * @Version v1.2
 **/
@Component
public class PostRequestFilter extends ZuulFilter {

    private final static Logger logger = LoggerFactory.getLogger(PostRequestFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        //过滤各种POST请求
        if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return false;
        }
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse httpServletResponse = ctx.getResponse();
        HttpServletRequest httpServletRequest = ctx.getRequest();
        Thread thread = Thread.currentThread();
        int status = httpServletResponse.getStatus();
        long startTime = (long) ctx.get(ZuulConst.START_TIME_KEY);

        logger.info("【转发之后】原始地址：{}，响应地址:{}，请求类型:{}，线程id:{}，线程名称:{}，响应状态:{}，耗时：{}ms，响应级别：{}",
                ctx.get(ZuulConst.ORIGINAL_REQUEST_URI),
                httpServletRequest.getRequestURI(),
                httpServletRequest.getMethod(),
                thread.getId(),
                thread.getName(),
                status, (System.currentTimeMillis() - startTime),
                getLevel(System.currentTimeMillis() - startTime));

        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT");
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.addHeader("Access-Control-Allow-Headers",
                "Content-Type,Accept,Accept-Encoding,Accept-Language,"
                        + "Connection,Content-Length,Host,Origin,User-Agent,version,platform,deviceId,token,aid,rid,"
                        + "tg,X-Requested-With,"
                        + "Referer,enableFaker," + "AuthToken,authtoken,AppCode,appCode,appcode,cid");
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "*");
        httpServletResponse.addHeader("Access-Control-Max-Age", "18000L");
        httpServletResponse.addHeader("Vary", "Origin,Access-Control-Request-Method,Access-Control-Request-Headers");
        httpServletResponse.setCharacterEncoding("UTF-8");

//        String server = getServer(httpServletRequest.getRequestURI());
//        if(!"website".equals(server)){
//            Map<String,Object> map = new HashMap<>();
//            map.put("ip",IpUtil.getIpAddr(httpServletRequest));
//            map.put("url",httpServletRequest.getRequestURI());
//            map.put("duration",System.currentTimeMillis() - startTime);
//            rabbitSender.send(RabbitConfig.BASIC_INTERFACE_COUNT_QUEUE,map);
//        }
//        Map<String,Object> map = new HashMap<>();
//        map.put("ip",IpUtil.getIpAddr(httpServletRequest));
//        map.put("url",httpServletRequest.getRequestURI());
//        map.put("duration",System.currentTimeMillis() - startTime);
//        rabbitTemplate.convertAndSend(RabbitConfig.BASIC_INTERFACE_COUNT_QUEUE,map);
//
//        map = null;

        ctx.setSendZuulResponse(true);
        return null;
    }

    private String getServer(String url) {
        if (StringUtils.isEmpty(url)) {
            return "";
        }
        String server;
        if (url.startsWith("/")) {
            server = url.split("/")[1];
        } else {
            server = url.split("/")[0];
        }

        return server;
    }

    private Integer getLevel(Long duration) {
        if (duration == null) {
            return 1;
        }
        if (duration <= 500) {
            return 1;
        } else if (duration <= 1000) {
            return 2;
        } else if (duration <= 3000) {
            return 3;
        } else if (duration <= 5000) {
            return 4;
        } else {
            return 5;
        }
    }

}
