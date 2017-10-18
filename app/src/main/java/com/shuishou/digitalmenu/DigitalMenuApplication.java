package com.shuishou.digitalmenu;

import android.app.Application;

import com.shuishou.digitalmenu.io.CrashHandler;

/**
 * Created by Administrator on 2017/10/5.
 */

public class DigitalMenuApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(this);
    }
}
