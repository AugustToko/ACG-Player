package top.geek_studio.chenlongcould.geeklibrary;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;

/**
 * Some package tools
 *
 * @author : chenlongcould
 * @date : 2019/06/08/17
 * @see android.content.pm.PackageManager
 */
public class PackageTool {
	/**
	 * @return version Code
	 */
	public static long getVerCode(@NonNull final Context context) {
		long verCode = -1;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				verCode = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0).getLongVersionCode();
			} else {
				verCode = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0).versionCode;
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return verCode;
	}
}
