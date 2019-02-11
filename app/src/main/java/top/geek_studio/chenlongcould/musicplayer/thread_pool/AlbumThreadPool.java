/*
 * ************************************************************
 * 文件：AlbumThreadPool.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:50
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.thread_pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * for load albumCover
 */
public class AlbumThreadPool {

    private static final int KEEP_ALIVE = 10;
    private static AlbumThreadPool mInstance = null;
    @SuppressWarnings("FieldCanBeLocal")
    private static int MAX_POOL_SIZE;
    @SuppressWarnings("FieldCanBeLocal")
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor mThreadPoolExec;

    private AlbumThreadPool() {
        int coreNum = Runtime.getRuntime().availableProcessors();
        MAX_POOL_SIZE = coreNum * 2;
        mThreadPoolExec = new ThreadPoolExecutor(
                coreNum,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                workQueue);
    }

    public static synchronized void post(Runnable runnable) {
        if (mInstance == null) {
            mInstance = new AlbumThreadPool();
        }
        mInstance.mThreadPoolExec.execute(runnable);
    }

    public static void finish() {
        if (mInstance != null) {
            mInstance.mThreadPoolExec.shutdown();
            mInstance = null;
        }
    }
}
