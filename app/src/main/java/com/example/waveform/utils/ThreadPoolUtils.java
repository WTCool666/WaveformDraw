package com.example.waveform.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtils {
	
	private final int CORE_POOL_SIZE=5;
	private final int MAX_POOL_SIZE=10;
	private ThreadPoolExecutor mExecutor;

    public ThreadPoolUtils() {
        initThreadPoolExecutor();
    }

    private void initThreadPoolExecutor() {
        if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
            synchronized (ThreadPoolUtils.class) {
                if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
                    long keepAliveTime = 3000;
                    TimeUnit unit = TimeUnit.MILLISECONDS;
                    BlockingQueue workQueue = new LinkedBlockingDeque<Object>();
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();

                    mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, keepAliveTime, unit, workQueue,
                            threadFactory, handler);
                }
            }
        }
    }

    public void execute(Runnable task) {
    	initThreadPoolExecutor();
        mExecutor.execute(task);
    }

    public Future submit(Runnable task) {
    	initThreadPoolExecutor();
        return mExecutor.submit(task);
    }

    public void remove(Runnable task) {
    	initThreadPoolExecutor();
        mExecutor.remove(task);
    }

    public void shutdown() {
        mExecutor.shutdownNow();
    }

}
