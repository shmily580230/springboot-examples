package com.examples.common;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.UUID;

public class TraceContext {


    private static final TransmittableThreadLocal<String> TRACE_ID = new TransmittableThreadLocal<>();

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static void clear() {
        TRACE_ID.remove();
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
