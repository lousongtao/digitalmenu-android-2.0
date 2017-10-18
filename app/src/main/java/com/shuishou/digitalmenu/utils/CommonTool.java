package com.shuishou.digitalmenu.utils;

import android.content.Context;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/10/5.
 */

public class CommonTool {
    public static Message buildMessage(int what, Object obj, int arg1, int arg2){
        Message msg = new Message();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.what = what;
        msg.obj = obj;
        return msg;
    }

    public static Message buildMessage(int what, Object obj){
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        return msg;
    }

    public static Message buildMessage(int what){
        Message msg = new Message();
        msg.what = what;
        return msg;
    }

    public static void popupToast(Context context, String msg, int shortlong){
        Toast.makeText(context, msg, shortlong).show();
    }

    public static void popupWarnDialog(Context context, int iconId, String title, String msg){
        new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton("OK", null)
                .create().show();
    }
}
