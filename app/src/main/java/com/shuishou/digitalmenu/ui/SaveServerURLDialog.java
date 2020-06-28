package com.shuishou.digitalmenu.ui;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.io.IOOperator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/21.
 */

class SaveServerURLDialog {

    private EditText txtConfirmCode;
    private EditText txtServerURL;
    private MainActivity mainActivity;
    private CheckBox cbShowDishPic;
    private CheckBox cbNeedPwdPostOrder;

    private AlertDialog dlg;

    public SaveServerURLDialog(@NonNull MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        initUI();
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.config_serverurl_layout, null);

        txtConfirmCode = (EditText) view.findViewById(R.id.txtConfirmCode);
        txtServerURL = (EditText) view.findViewById(R.id.txtServerURL);
        cbShowDishPic = (CheckBox) view.findViewById(R.id.cbShowDishPic);
        cbNeedPwdPostOrder = (CheckBox) view.findViewById(R.id.cbNeedPwdPostOrder);
        loadServerURL();
        loadConfigInfo();

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Configure Server URL")
                .setIcon(R.drawable.info)
                .setPositiveButton(" Save ", null)
                .setNegativeButton("Cancel", null)
                .setView(view);
        dlg = builder.create();
        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //add listener for YES button
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doSaveURL();
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

    private void loadServerURL(){
        String url = IOOperator.loadServerURL(InstantValue.FILE_SERVERURL);
        if (url != null)
            txtServerURL.setText(url);
    }

    private void loadConfigInfo(){
        Map<String, Object> config = IOOperator.loadConfigInfo(InstantValue.FILE_CONFIGINFO);
        if (config != null){
            if(config.get(InstantValue.CONFIGINFO_SHOWDISHPIC) != null){
                cbShowDishPic.setChecked(Boolean.parseBoolean(config.get(InstantValue.CONFIGINFO_SHOWDISHPIC).toString()));
            }
            if (config.get(InstantValue.CONFIGINFO_NEEDPWDPOSTINGORDER) != null){
                cbNeedPwdPostOrder.setChecked(Boolean.parseBoolean(config.get(InstantValue.CONFIGINFO_NEEDPWDPOSTINGORDER).toString()));
            }
        }
    }

    private void doSaveURL(){
        final String code = txtConfirmCode.getText().toString();
        if (code == null || code.length() == 0){
            Toast.makeText(mainActivity, "Please input confirm code.", Toast.LENGTH_LONG).show();
            return;
        }
        final String url = txtServerURL.getText().toString();
        if (url == null || url.length() == 0){
            Toast.makeText(mainActivity, "Please input server URL.", Toast.LENGTH_LONG).show();
            return;
        }

        if (code.equals("2017")){
            IOOperator.saveServerURL(InstantValue.FILE_SERVERURL, url);
            InstantValue.URL_TOMCAT = url;
            Map<String, Object> mapConfig = new HashMap<>();
            mapConfig.put(InstantValue.CONFIGINFO_SHOWDISHPIC, cbShowDishPic.isChecked());
            mapConfig.put(InstantValue.CONFIGINFO_NEEDPWDPOSTINGORDER, cbNeedPwdPostOrder.isChecked());
            IOOperator.saveConfigInfo(InstantValue.FILE_CONFIGINFO, mapConfig);
            InstantValue.SETTING_SHOWDISHPICTURE = cbShowDishPic.isChecked();
            InstantValue.SETTING_NEEDPWDPOSTINGORDER = cbNeedPwdPostOrder.isChecked();
            dlg.dismiss();
            mainActivity.popRestartDialog("Success to configure server URL, Please restart app");
        } else {
            Toast.makeText(mainActivity, "Confirm code is wrong.", Toast.LENGTH_LONG).show();
        }
    }

    public void showDialog(){
        dlg.show();
    }

    public void dismiss(){
        dlg.dismiss();
    }

}
