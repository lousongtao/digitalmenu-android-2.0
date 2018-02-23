package com.shuishou.digitalmenu.http;

import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.io.CrashHandler;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.utils.CommonTool;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.download.DownloadListener;

/**
 * Created by Administrator on 2017/10/5.
 */

public class DownloadDishImageListener implements DownloadListener {
    private MainActivity mainActivity;
    private Handler handler;
//    //record the picture files need to be download, after all finish, then rebuild the UI
//    private Hashtable<String, Boolean> flagFinishLoadDishPictures = new Hashtable<>();

    //use a int key to point to a file name, as well as the 'what' value,
    //this what value can be used in success/fail/finish function
    //IMPORTANT: SparseArray is not thread-safe
    private SparseArray<String> filelist = new SparseArray<>();
    private int totalFileAmount ;

    //to avoid generate too many objects, define some string as global variables
    private String strLoading = "Loading ";
    private String strSlash =  " / ";
    private String strPic =  " dish pictures";
    public DownloadDishImageListener(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.handler = mainActivity.getProgressDlgHandler();
    }

    @Override
    public void onDownloadError(int what, Exception exception) {
        new AlertDialog.Builder(mainActivity)
                .setIcon(R.drawable.error)
                .setTitle("WRONG")
                .setMessage("Failed to load dish image. Please restart app!")
                .setNegativeButton("OK", null)
                .create().show();
        CrashHandler.getInstance().handleException(exception, false); //write exception to log file
        getThreadSafeArray().remove(what);
        int left = getThreadSafeArray().size();
        //while all download finish, start to build menu
        if (left == 0){
            handler.sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH, "start to rebuild menu"));
            mainActivity.buildMenu();
        }
    }

    @Override
    public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {

    }

    @Override
    public void onProgress(int what, int progress, long fileCount, long speed) {

    }

    @Override
    public void onFinish(int what, String filePath) {
        getThreadSafeArray().remove(what);
        int left = getThreadSafeArray().size();
        //change progress dialog text
        handler.sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_SHOWPROGRESS,
                strLoading + (totalFileAmount - left) + strSlash + totalFileAmount + strPic));
        Log.d("lousongtao", "onFinish: left = " + left + ", what = " + what);
        //while all download finish, start to build menu
        if (left == 0){
            handler.sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH, "start to rebuild menu"));
            mainActivity.popRestartDialog("Data refresh finish successfully. Please restart the app.");
        }
    }

    @Override
    public void onCancel(int what) {

    }

    public void addFiletoList(int key, String filename){
        getThreadSafeArray().append(key, filename);
    }

    public void setTotalFileAmount(int total){
        totalFileAmount = total;
    }

    private synchronized SparseArray<String> getThreadSafeArray(){
        return filelist;
    }
}
