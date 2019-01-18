/*
 * ************************************************************
 * 文件：TaskUtil.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月18日 18:58:29
 * 上次修改时间：2019年01月18日 18:57:37
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary;

import android.app.ActivityManager;
import android.content.Context;

import java.util.ArrayList;

public class TaskUtil {

    public static final String TAG = "TaskUtil";

    /**
     * 判断服务是否已经正在运行
     *
     * @param mContext  上下文对象
     * @param className Service类的全路径类名 "包名+类名" 如com.demo.test.MyService
     * @return
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        ActivityManager myManager = (ActivityManager) mContext
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName()
                    .equals(className)) {
                return true;
            }
        }
        return false;
    }

}
