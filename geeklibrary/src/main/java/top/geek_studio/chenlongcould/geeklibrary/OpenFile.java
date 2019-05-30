package top.geek_studio.chenlongcould.geeklibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * @author chenlongcould
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class OpenFile {

    private static String AUT;

    public static void init(String aut) {
        AUT = aut;
    }

    public static Intent openFile(Activity context, String filePath) {

        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        /* 取得扩展名 */
		String end = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        /* 依扩展名的类型决定MimeType */
        switch (end) {
            case "m4a":
            case "mp3":
            case "mid":
            case "xmf":
            case "ogg":
            case "wav":
                return getAudioFileIntent(context, filePath);

            case "3gp":
            case "mp4":
                return getAudioFileIntent(context, filePath);

            case "jpg":
            case "gif":
            case "png":
            case "jpeg":
            case "bmp":
                return getImageFileIntent(context, filePath);

            case "apk":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return getApkFileIntent(context, filePath);
                }

            case "ppt":
                return getPptFileIntent(context, filePath);

            case "xls":
                return getExcelFileIntent(context, filePath);

            case "doc":
                return getWordFileIntent(context, filePath);

            case "pdf":
                return getPdfFileIntent(context, filePath);

            case "chm":
                return getChmFileIntent(context, filePath);

            case "txt":
                return getTextFileIntent(filePath, false);

            case "zip":
                return getZipFileIntent(context, filePath);

            default:
                return getAllIntent(context, filePath);
        }
    }

    public static Intent getAllIntent(Context context, String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));

        intent.setDataAndType(uri, "*/*");
        return intent;
    }

    public static Intent getZipFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "application/zip");
        return intent;
    }

    /**
     * getApkIntent
     *
     * @param activity activity
     * @param param    param
     *
     * @return the intent
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Intent getApkFileIntent(Activity activity, String param) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //包名.FileProvider
        Uri apkUri = FileProvider.getUriForFile(activity, AUT, new File(param));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");

        //来判断应用是否有权限安装apk
        boolean installAllowed = activity.getPackageManager().canRequestPackageInstalls();

        //有权限
        if (installAllowed) {
            //安装apk
            return intent;
        } else {
            //无权限 申请权限
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 1);
            Toast.makeText(activity, "Need permissions!", Toast.LENGTH_LONG).show();
            return null;
        }
    }


    /**
     * Android获取一个用于打开VIDEO文件的intent
     *
     * @param context context
     * @param param   param
     *
     * @return the intent
     */
    public static Intent getVideoFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }


    /**
     * Android获取一个用于打开AUDIO文件的intent
     *
     * @param context context
     * @param param   param
     *
     * @return the intent
     */
    public static Intent getAudioFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("oneshot", 0);
//        intent.putExtra("configchange", 0);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    /**
     * Android获取一个用于打开Html文件的intent
     */
    public static Intent getHtmlFileIntent(Context context, String param) {

        Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    /**
     * Android获取一个用于打开图片文件的intent
     */
    public static Intent getImageFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    /**
     * Android获取一个用于打开PPT文件的intent
     */
    public static Intent getPptFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    /**
     * Android获取一个用于打开Excel文件的intent
     */
    public static Intent getExcelFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    /**
     * Android获取一个用于打开Word文件的intent
     */
    public static Intent getWordFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    /**
     * Android获取一个用于打开CHM文件的intent
     */
    public static Intent getChmFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    /**
     * Android获取一个用于打开文本文件的intent
     */
    public static Intent getTextFileIntent(String param, boolean paramBoolean) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean) {
            Uri uri1 = Uri.parse(param);
            intent.setDataAndType(uri1, "text/plain");
        } else {
            Uri uri2 = Uri.fromFile(new File(param));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    /**
     * Android获取一个用于打开PDF文件的intent
     */
    public static Intent getPdfFileIntent(Context context, String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), AUT, new File(param));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }
}
