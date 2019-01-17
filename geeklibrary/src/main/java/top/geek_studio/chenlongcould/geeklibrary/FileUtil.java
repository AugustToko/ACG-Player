/*
 * ************************************************************
 * 文件：FileUtil.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:28:59
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {


    private static final String TAG = "FileUtil";

    public static void Unzip(String zipFile, String targetDir) {
        int BUFFER = 4096;          //这里缓冲区我们使用4KB，
        String strEntry;            //保存每个zip的条目名称

        try {
            BufferedOutputStream dest;          //缓冲输出流
            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;         //每个zip条目的实例

            while ((entry = zis.getNextEntry()) != null) {

                try {
                    Log.i("Unzip: ", "=" + entry);

                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();

                    File entryFile = new File(targetDir + strEntry);
                    File entryDir = new File(entryFile.getParent());

                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }

                    if (!entry.isDirectory()) {
                        FileOutputStream fos = new FileOutputStream(entryFile);

                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = zis.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                    } else {
                        entryFile.mkdir();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zis.close();
        } catch (Exception cwj) {
            cwj.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            File myFilePath = new File(folderPath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"UnusedReturnValue", "ConstantConditions", "ResultOfMethodCallIgnored"})
    private static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (String aTempList : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + aTempList);
            } else {
                temp = new File(path + File.separator + aTempList);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + aTempList);//先删除文件夹里面的文件
                delFolder(path + "/" + aTempList);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 压缩一个文件夹
     */
    public static void zipDirectory(String path, String savePath) throws IOException {
        File file = new File(path);
        File zipFile = new File(savePath);
        Log.d(TAG, "zipDirectory: " + zipFile.getAbsolutePath());
        zipFile.createNewFile();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        zip(zos, file, file.getName());
        zos.flush();
        zos.close();
    }

    /**
     * @param zos  压缩输出流
     * @param file 当前需要压缩的文件
     * @param path 当前文件相对于压缩文件夹的路径
     */
    private static void zip(ZipOutputStream zos, File file, String path) throws IOException {
        // 首先判断是文件，还是文件夹，文件直接写入目录进入点，文件夹则遍历
        if (file.isDirectory()) {
            ZipEntry entry = new ZipEntry(path + File.separator);// 文件夹的目录进入点必须以名称分隔符结尾
            zos.putNextEntry(entry);
            File[] files = file.listFiles();
            for (File x : files) {
                zip(zos, x, path + File.separator + x.getName());
            }
        } else {
            FileInputStream fis = new FileInputStream(file);// 目录进入点的名字是文件在压缩文件中的路径
            ZipEntry entry = new ZipEntry(path);
            zos.putNextEntry(entry);// 建立一个目录进入点

            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = fis.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.flush();
            fis.close();
            zos.closeEntry();// 关闭当前目录进入点，将输入流移动下一个目录进入点
        }
    }

}
