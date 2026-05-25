package com.lance.testall.threadpool.entity;

/**
 * 线程池实现类型，用于实验对比（JDK 原生 vs Spring 封装）。
 */
public final class ExecutorType {

    public static final String JDK = "JDK";
    public static final String SPRING = "SPRING";

    private ExecutorType() {
    }
}
