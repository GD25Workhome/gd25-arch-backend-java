package com.lance.testall.lock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lance.testall.lock.entity.DeductResult;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批次扣减结果计数器：按 {@link DeductResult} 与编排层未捕获异常分别累加，可序列化为 JSON 落库。
 */
public class RunResultCounter {

    /** 编排层 catch 块计数，与 {@link DeductResult} 区分 */
    public static final String KEY_UNCAUGHT_EXCEPTION = "UNCAUGHT_EXCEPTION";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final EnumMap<DeductResult, AtomicInteger> byResult = new EnumMap<>(DeductResult.class);
    private final AtomicInteger uncaughtExceptionCount = new AtomicInteger();

    public RunResultCounter() {
        for (DeductResult result : DeductResult.values()) {
            byResult.put(result, new AtomicInteger());
        }
    }

    public void record(DeductResult result) {
        AtomicInteger counter = byResult.get(result);
        if (counter != null) {
            counter.incrementAndGet();
        } else {
            byResult.get(DeductResult.ERROR).incrementAndGet();
        }
    }

    public void recordUncaughtException() {
        uncaughtExceptionCount.incrementAndGet();
    }

    public int getSuccessCount() {
        return byResult.get(DeductResult.SUCCESS).get();
    }

    public int getFailCount() {
        return byResult.get(DeductResult.INSUFFICIENT).get()
                + byResult.get(DeductResult.VERSION_CONFLICT).get();
    }

    /** 与历史语义一致：锁超时 + 业务 ERROR + 未捕获异常 */
    public int getErrorCount() {
        return byResult.get(DeductResult.LOCK_TIMEOUT).get()
                + byResult.get(DeductResult.ERROR).get()
                + uncaughtExceptionCount.get();
    }

    public Map<String, Integer> toBreakdownMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (DeductResult result : DeductResult.values()) {
            map.put(result.name(), byResult.get(result).get());
        }
        map.put(KEY_UNCAUGHT_EXCEPTION, uncaughtExceptionCount.get());
        return map;
    }

    public String toBreakdownJson() {
        try {
            return MAPPER.writeValueAsString(toBreakdownMap());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("序列化 result_breakdown 失败", ex);
        }
    }

    public static Map<String, Integer> parseBreakdownJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, Integer>>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("解析 result_breakdown 失败: " + json, ex);
        }
    }
}
