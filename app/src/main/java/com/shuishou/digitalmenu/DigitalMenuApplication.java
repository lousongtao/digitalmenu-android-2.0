package com.shuishou.digitalmenu;

import android.app.Application;

import com.shuishou.digitalmenu.io.CrashHandler;

import java.io.File;

import pl.brightinventions.slf4android.FileLogHandlerConfiguration;
import pl.brightinventions.slf4android.LoggerConfiguration;

/**
 * Created by Administrator on 2017/10/5.
 */

public class DigitalMenuApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FileLogHandlerConfiguration fileHandler = LoggerConfiguration.fileLogHandler(this);
        File dir = new File(InstantValue.ERRORLOGPATH);
        if (!dir.exists())
            dir.mkdir();
        fileHandler.setFullFilePathPattern(InstantValue.ERRORLOGPATH + "/" + InstantValue.ERRORLOGFILENAME + ".%g.%u.log");

        LoggerConfiguration.configuration().addHandlerToRootLogger(fileHandler);
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(this);
    }
}
