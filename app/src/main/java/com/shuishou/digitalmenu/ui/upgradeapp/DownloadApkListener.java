package com.shuishou.digitalmenu.ui.upgradeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;

import com.shuishou.digitalmenu.BuildConfig;
import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.utils.CommonTool;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.download.DownloadListener;

import java.io.File;

/**
 * Created by Administrator on 23/06/2018.
 */

public class DownloadApkListener  implements DownloadListener {
    private MainActivity mainActivity;

    public DownloadApkListener(MainActivity mainActivity){
        this.mainActivity = mainActivity;

    }
    @Override
    public void onDownloadError(int what, Exception exception) {
        new AlertDialog.Builder(mainActivity)
                .setIcon(R.drawable.error)
                .setTitle("WRONG")
                .setMessage("Failed to load apk file!")
                .setNegativeButton("Close", null)
                .create().show();
    }

    @Override
    public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {

    }

    @Override
    public void onProgress(int what, int progress, long fileCount, long speed) {

    }

    @Override
    public void onFinish(int what, String filePath) {
        mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG, ""));
        File file = new File(InstantValue.LOCAL_CATEGORY_UPGRADEAPK + "digitalmenu.apk");
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
//        Uri uri = FileProvider.getUriForFile(mainActivity, BuildConfig.APPLICATION_ID + ".provider", file);
//        intent.setDataAndType(uri, "application/vnd.android.package-archive");
//        mainActivity.startActivity(intent);
    }

    @Override
    public void onCancel(int what) {

    }
}
