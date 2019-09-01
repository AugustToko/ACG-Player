package top.geek_studio.chenlongcould.musicplayer.live2d.utils.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * You can modify and use this source freely
 * only for the development of application related Live2D.
 * <p>
 * (c) Live2D Inc. All rights reserved.
 */
public class FileManager {

	private static WeakReference<Context> context;

	public static void init(Context c) {
		if (context != null && context.get() != null) return;
		context = new WeakReference<>(c);
	}

	public static boolean exists_resource(String path) {
		try {
			InputStream afd = context.get().getAssets().open(path);
			afd.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static InputStream open_resource(String path) throws IOException {
		return context.get().getAssets().open(path);
	}

	public static boolean exists_cache(String path) {
		File f = new File(context.get().getCacheDir(), path);
		return f.exists();
	}

	public static InputStream open_cache(String path) throws FileNotFoundException {
		File f = new File(context.get().getCacheDir(), path);
		return new FileInputStream(f);
	}

	public static InputStream open(String path, boolean isCache) throws IOException {
		if (isCache) {
			return open_cache(path);
		} else {
			return open_resource(path);
		}

	}

	public static InputStream open(String path) throws IOException {
		return open(path, false);
	}

	public static AssetFileDescriptor openFd(String path) throws IOException {
		return context.get().getAssets().openFd(path);
	}

	public static void clearContext() {
		context.clear();
	}
}
