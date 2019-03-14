/*
 * ************************************************************
 * 文件：HttpUtil.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月19日 12:17:57
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author chenlongcould
 */
public final class HttpUtil {

    /**
     * sample okHttp util
     */
	public static void sedOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
        //传入地址
        Request request = new Request.Builder().url(address).build();
        //处理服务器响应
        okHttpClient.newCall(request).enqueue(callback);
    }
}
