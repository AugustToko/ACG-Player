/*
 * ************************************************************
 * 文件：AboutActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月12日 20:26:06
 * 上次修改时间：2019年01月12日 20:25:40
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutTemplateDulTextBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutTemplateSingleTextBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutTemplateThanksBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityAboutAppBinding;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutAppBinding mAppBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_app);

        mAppBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        AboutTemplateDulTextBinding version = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
        AboutTemplateDulTextBinding author = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
        AboutTemplateDulTextBinding web = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
        AboutTemplateSingleTextBinding update = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        AboutTemplateSingleTextBinding mail = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        AboutTemplateSingleTextBinding lic = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        AboutTemplateSingleTextBinding share = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        AboutTemplateThanksBinding thanksBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_thanks, (ViewGroup) mAppBinding.getRoot(), false);

        version.ico.setImageResource(R.drawable.ic_info_outline_black_24dp);
        version.mainText.setText("Version");
        try {
            version.subText.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        update.ico.setImageResource(R.drawable.ic_cloud_download_black_24dp);
        update.text.setText("Update");

        lic.text.setText("Licence");
        lic.ico.setImageResource(R.drawable.ic_find_in_page_black_24dp);
        lic.body.setOnClickListener(v -> startActivity(new Intent(AboutActivity.this, AboutLic.class)));

        share.text.setText("Share");
        share.ico.setImageResource(R.drawable.ic_share_black_24dp);

        author.mainText.setText("Chang Long");
        author.subText.setText("Jiangsu China");
        author.ico.setImageResource(R.drawable.ic_person_black_24dp);

        mail.ico.setImageResource(R.drawable.ic_mail_outline_black_24dp);
        mail.text.setText("Send a E-Mail");

        web.ico.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
        web.mainText.setText("Open my blog");
        web.subText.setText("https://blog.geek-studio.top");

        mAppBinding.card1.addView(version.getRoot());
        mAppBinding.card1.addView(update.getRoot());
        mAppBinding.card1.addView(lic.getRoot());
        mAppBinding.card1.addView(share.getRoot());

        mAppBinding.card2.addView(author.getRoot());
        mAppBinding.card2.addView(mail.getRoot());
        mAppBinding.card2.addView(web.getRoot());

    }
}
