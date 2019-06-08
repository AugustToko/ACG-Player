package top.geek_studio.chenlongcould.geeklibrary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import top.geek_studio.chenlongcould.geeklibrary.model.Project;

import java.io.IOException;
import java.util.Locale;

/**
 * Geek Cloud Service utils
 *
 * @author : chenlongcould
 * @date : 2019/06/08/16
 */
final public class GCSutil {

	public static final String GCS_ADDRESS = "https://geek-cloud.top";

	private static final String TAG = "GCS_util";

	/**
	 * check update
	 */
	public static void checkUpdate(@NonNull final Activity activity, @NonNull Project project, long currentVersionCode) {
		if (currentVersionCode == -1) {
			Toast.makeText(activity, "Get version code filed.", Toast.LENGTH_SHORT).show();
			return;
		}
		HttpUtil.sedOkHttpRequest(GCS_ADDRESS + project.getInfoUrl(), new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				activity.runOnUiThread(() -> Toast.makeText(activity, "Check Update Failed!"
						+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
				final Document document = Jsoup.parse(response.body().string());
				final Elements elements = document.select("div[class=appInfo]");
				final String info = elements.select("h4").text();
				final Long integer = Long.valueOf(info.split(" ")[2]);

				Log.d(TAG, "checkUpdate: remote code is: " + info);

				if (currentVersionCode < integer) {
					activity.runOnUiThread(() -> {
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						builder.setTitle("Need to update!")
								.setMessage(String.format(Locale.CHINA, "This version of %d is need to update!"
										, currentVersionCode))
								.setCancelable(true)
								.setNegativeButton("OK", (dialog, which) -> {
									final Uri uri = Uri.parse(GCS_ADDRESS + project.getReleaseUrl());
									activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
								});
						builder.show();
						Toast.makeText(activity, "Need Update", Toast.LENGTH_SHORT).show();
					});
				}
			}
		});
	}
}
