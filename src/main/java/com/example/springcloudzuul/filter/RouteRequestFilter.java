package com.example.springcloudzuul.filter;

import com.example.springcloudzuul.constant.ZuulConst;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ChangLF 2021-12-22
 */
@Component
public class RouteRequestFilter extends ZuulFilter {

    private final static Logger logger = LoggerFactory.getLogger(RouteRequestFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
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
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse httpServletResponse = ctx.getResponse();
        HttpServletRequest httpServletRequest = ctx.getRequest();
        Thread thread = Thread.currentThread();
        int status = httpServletResponse.getStatus();
        long startTime = (long) ctx.get(ZuulConst.START_TIME_KEY);

        logger.info("【转发过程中】原始地址：{}，响应地址:{}，请求类型:{}，线程id:{}，线程名称:{}，响应状态:{}，耗时：{}ms",
                ctx.get(ZuulConst.ORIGINAL_REQUEST_URI),
                httpServletRequest.getRequestURI(),
                httpServletRequest.getMethod(),
                thread.getId(),
                thread.getName(),
                status, (System.currentTimeMillis() - startTime));
        return null;
    }
}
