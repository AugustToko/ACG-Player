/*
 * ************************************************************
 * 文件：ItemCoverThreadPool.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月27日 13:08:48
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.threadPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenlongcould
 */
public final class ItemCoverThreadPool {
	private static final int KEEP_ALIVE = 10;
	private static ItemCoverThreadPool mInstance = null;
	private ThreadPoolExecutor mThreadPoolExec;

	private ItemCoverThreadPool() {
		int coreNum = Runtime.getRuntime().availableProcessors();
		int maxPoolSize = coreNum * 2;
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
		mThreadPoolExec = new ThreadPoolExecutor(
				coreNum,
				maxPoolSize,
				KEEP_ALIVE,
				TimeUnit.SECONDS,
				workQueue);
	}

	public static synchronized void post(Runnable runnable) {
		if (mInstance == null) {
			mInstance = new ItemCoverThreadPool();
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
