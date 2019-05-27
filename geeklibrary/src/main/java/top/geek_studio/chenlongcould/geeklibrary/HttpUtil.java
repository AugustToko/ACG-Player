package top.geek_studio.chenlongcould.geeklibrary;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.TimeUnit;

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

//		OkHttpClient client = new OkHttpClient();
//
//		Request request = new Request.Builder()
//				.url(address)
//				.build();
//		client.newCall(request).enqueue(callback);

	}
}
