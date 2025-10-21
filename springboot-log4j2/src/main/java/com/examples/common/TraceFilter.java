package com.examples.common;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TraceFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            // 生成并设置TraceID
            String traceId = TraceContext.generateTraceId();
            TraceContext.setTraceId(traceId);
            MDC.put(TRACE_ID, traceId);

            // 透传TraceID到下游
            response.setHeader(TRACE_ID, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 必须清理上下文
            TraceContext.clear();
            MDC.remove(TRACE_ID);
        }
    }
}
