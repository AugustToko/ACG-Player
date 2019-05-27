package top.geek_studio.chenlongcould.musicplayer.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.geeklibrary.HttpUtil;
import top.geek_studio.chenlongcould.musicplayer.App;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutDulTextBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutSingleTextBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutThanksBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityAboutAppBinding;

import java.io.IOException;

/**
 * @author chenlongcould
 */
public class AboutActivity extends BaseCompatActivity {

	private static final String TAG = "AboutActivity";

	private ActivityAboutAppBinding mAppBinding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_app);
		initView(mAppBinding.toolbar, mAppBinding.appbar);

		mAppBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

		// TODO: 2019/3/10 add Thinking
		@SuppressWarnings("unused") final AboutThanksBinding thanksBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_thanks, (ViewGroup) mAppBinding.getRoot(), false);

		setUpVersion();
		setUpLicense();
		setUpShare();
		setUpIssue();
		setUpPrivacyPolicy();

		setUpAuthor();
		setUpWeb();
		setUpUpdate();
		setUpMail();
	}

	private void setUpPrivacyPolicy() {
		final AboutDulTextBinding privacy = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
		privacy.ico.setBackgroundResource(R.drawable.ic_announcement_black_24dp);
		privacy.mainText.setText(getString(R.string.privacy_policy));
		privacy.subText.setText(getString(R.string.click_to_learn_more));
		privacy.getRoot().setOnClickListener(v -> {
			Uri uri = Uri.parse(App.PRIVACY_POLICY_URL);
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		});
		mAppBinding.card1.addView(privacy.getRoot());
	}

	private void setUpIssue() {
		final AboutDulTextBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
		binding.ico.setBackgroundResource(R.drawable.ic_bug_report_black_24dp);
		binding.mainText.setText(getString(R.string.bug_report_or_feature_request));
		binding.subText.setText(App.ISSUE_URL);
		binding.getRoot().setOnClickListener(v -> {
			Uri uri = Uri.parse(App.ISSUE_URL);
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		});
		mAppBinding.card1.addView(binding.getRoot());
	}
	
	private void setUpVersion() {
		final AboutDulTextBinding version = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
		version.ico.setImageResource(R.drawable.ic_info_outline_black_24dp);
		version.mainText.setText(getString(R.string.version));
		try {
			version.subText.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		mAppBinding.card1.addView(version.getRoot());
	}

	private void setUpAuthor() {
		final AboutDulTextBinding author = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
		author.mainText.setText(getString(R.string.geek_aug));
		author.subText.setText(getString(R.string.jiangsu));
		author.ico.setImageResource(R.drawable.ic_person_black_24dp);
		mAppBinding.card2.addView(author.getRoot());
	}

	private void setUpWeb() {
		final AboutDulTextBinding web = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
		web.ico.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
		web.mainText.setText(getString(R.string.open_blog));
		web.subText.setText(App.MY_WEB_SITE);
		web.body.setOnClickListener(v -> {
			Uri uri = Uri.parse(App.MY_WEB_SITE);
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		});
		mAppBinding.card2.addView(web.getRoot());
	}

	private void setUpUpdate() {
		final AboutSingleTextBinding update = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_single_text, (ViewGroup) mAppBinding.getRoot(), false);
		update.ico.setImageResource(R.drawable.ic_cloud_download_black_24dp);
		update.text.setText(getString(R.string.update_log));
		update.body.setOnClickListener(v -> {
			AlertDialog load = DialogUtil.getLoadingDialog(AboutActivity.this, "Loading...");
			load.show();

			final AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
			builder.setCancelable(true);
			builder.setTitle(getString(R.string.update_log));
			builder.setPositiveButton(getString(R.string.close), (dialog, which) -> dialog.cancel());

			HttpUtil.sedOkHttpRequest("https://www.coolapk.com/apk/top.geek_studio.chenlongcould.musicplayer.Common", new Callback() {
				@Override
				public void onFailure(@NonNull Call call, @NonNull IOException e) {
					runOnUiThread(() -> Toast.makeText(AboutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
				}

				@Override
				public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
					StringBuilder responseText;
					if (response.body() != null) {
						responseText = new StringBuilder(response.body().string());
						final Document document = Jsoup.parse(responseText.toString());
						final Elements elements = document.getAllElements();
						String status = elements.select("p[class=apk_left_title_info]").text();
						builder.setMessage(status);
						runOnUiThread(() -> {
							load.dismiss();
							builder.show();
						});
					} else {
						Toast.makeText(AboutActivity.this, "Get Data Error", Toast.LENGTH_SHORT).show();
					}
				}
			});

		});
		mAppBinding.card1.addView(update.getRoot());
	}

	private void setUpMail() {
		final AboutSingleTextBinding mail = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_single_text, (ViewGroup) mAppBinding.getRoot(), false);
		mail.ico.setImageResource(R.drawable.ic_mail_outline_black_24dp);
		mail.text.setText(getString(R.string.send_a_email));
		mail.body.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:827266641@qq.com"));
			intent.putExtra(Intent.EXTRA_EMAIL, "827266641@qq.com");
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
			startActivity(Intent.createChooser(intent, "E-Mail"));
		});
		mAppBinding.card2.addView(mail.getRoot());

	}

	private void setUpLicense() {
		final AboutSingleTextBinding lic = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_single_text, (ViewGroup) mAppBinding.getRoot(), false);
		lic.text.setText(getString(R.string.licenses));
		lic.ico.setImageResource(R.drawable.ic_find_in_page_black_24dp);
		lic.body.setOnClickListener(v -> startActivity(new Intent(AboutActivity.this, AboutLicActivity.class)));
		mAppBinding.card1.addView(lic.getRoot());
	}

	private void setUpShare() {
		final AboutSingleTextBinding share = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_single_text, (ViewGroup) mAppBinding.getRoot(), false);
		share.text.setText(getString(R.string.share));
		share.ico.setImageResource(R.drawable.ic_share_black_24dp);
		share.body.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_SEND).setType("text/plain")
				.putExtra(Intent.EXTRA_TEXT, getResources()
						.getString(R.string.app_name) + "\r\n" + "https://www.coolapk.com/apk/top.geek_studio.chenlongcould.musicplayer.Common")));
		mAppBinding.card1.addView(share.getRoot());

	}

	@Override
	public String getActivityTAG() {
		return TAG;
	}

	@Override
	public void inflateCommonMenu() {
		//not need
	}

	@Override
	public void inflateChooseMenu() {
		//not need
	}
}
