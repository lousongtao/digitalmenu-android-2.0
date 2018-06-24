package com.shuishou.digitalmenu.ui.upgradeapp;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.utils.CommonTool;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.download.DownloadQueue;
import com.yanzhenjie.nohttp.download.DownloadRequest;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 22/06/2018.
 */

public class UpgradeAppDialog{
    private MainActivity mainActivity;
    private EditText txtConfirmCode;
    private TextView txtCurrentVersion;
    private TableLayout versionLayout;
    private ArrayList<RadioButton> rbVersions = new ArrayList<>();
    private ArrayList<String> versions = new ArrayList<>();
    private RadioButtonListener listener = new RadioButtonListener();
    private AlertDialog dlg;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_FINISHQUERY = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dealHandlerMessage(msg);
            super.handleMessage(msg);
        }
    };
    public UpgradeAppDialog(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        initUI();
        initData();
    }

    private void dealHandlerMessage(Message msg){
        int margin = 5;
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.setMargins(margin, margin ,0 ,0);
        TableRow tr = null;
        if (versions == null || versions.size() == 0){
            tr = new TableRow(mainActivity);
            TextView tv = new TextView(mainActivity);
            tv.setTextSize(18);
            tv.setText("Cannot find any version on server!");
            tr.addView(tv);
            versionLayout.addView(tr);
        } else {
            for (int i = 0; i < versions.size(); i++) {
                if (i % 4 == 0){
                    tr = new TableRow(mainActivity);
                    versionLayout.addView(tr);
                }
                RadioButton rb = new RadioButton(mainActivity);
                rbVersions.add(rb);
                rb.setOnClickListener(listener);
                rb.setText(versions.get(i));
                tr.addView(rb, trlp);
            }
        }
    }

    private void initData(){
        new Thread(){
            @Override
            public void run() {
                versions = mainActivity.getHttpOperator().getUpgradeApkFiles();
                handler.sendMessage(CommonTool.buildMessage(PROGRESSDLGHANDLER_MSGWHAT_FINISHQUERY, null));
            }
        }.start();
        PackageManager pm = mainActivity.getPackageManager();
        try {
            PackageInfo packInfo = pm.getPackageInfo(mainActivity.getPackageName(), 0);
            txtCurrentVersion.setText("Current Version : "+packInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            MainActivity.LOG.error(InstantValue.DFYMDHMS.format(new Date()) + e.getMessage());
        }


    }

    private void initUI() {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.upgrade_dialog_layout, null);
        txtConfirmCode = (EditText) view.findViewById(R.id.txtConfirmCode);
        versionLayout = (TableLayout) view.findViewById(R.id.versionLayout);
        txtCurrentVersion = (TextView) view.findViewById(R.id.txtCurrentVersion);

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
        builder.setNegativeButton("Close", null);
        builder.setPositiveButton("Upgrade", null);
        builder.setView(view);
        dlg = builder.create();
        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //add listener for buttons
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doUpgrade();
                    }
                });
            }
        });
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 0;
        window.setAttributes(param);
    }

    private void doUpgrade(){
        if (txtConfirmCode.getText() == null || txtConfirmCode.getText().length() == 0) {
            Toast.makeText(mainActivity, "Please input the Confirmation Code to post this order!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mainActivity.getConfigsMap().get(InstantValue.CONFIGS_CONFIRMCODE).equals(txtConfirmCode.getText().toString())){
            Toast.makeText(mainActivity, "The Confirmation Code is wrong!", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedVersion = null;
        for (int i = 0; i < rbVersions.size(); i++) {
            if (rbVersions.get(i).isChecked()){
                selectedVersion = rbVersions.get(i).getText().toString();
                break;
            }
        }
        if (selectedVersion == null){
            Toast.makeText(mainActivity, "must select one version to upgrade", Toast.LENGTH_LONG).show();
            return;
        }

        mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH, "dowloading apk"));
        DownloadApkListener listener = new DownloadApkListener(mainActivity);
        DownloadQueue queue = NoHttp.newDownloadQueue();
        String url = InstantValue.URL_TOMCAT + "/../" + InstantValue.SERVER_CATEGORY_UPGRADEAPK + "/" + selectedVersion;
        DownloadRequest requestbig = NoHttp.createDownloadRequest(url, RequestMethod.GET, InstantValue.LOCAL_CATEGORY_UPGRADEAPK, "digitalmenu.apk", true, true);
        queue.add(0, requestbig, listener);
    }

    public void showDialog(){
        dlg.show();
    }

    class RadioButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v instanceof RadioButton){
                for (int i = 0; i < rbVersions.size(); i++) {
                    rbVersions.get(i).setChecked(false);
                }
                ((RadioButton) v).setChecked(true);
            }
        }
    }
}
