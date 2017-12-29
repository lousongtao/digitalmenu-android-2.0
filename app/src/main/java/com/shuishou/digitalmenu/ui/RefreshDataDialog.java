package com.shuishou.digitalmenu.ui;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.utils.CommonTool;

/**
 * Created by Administrator on 2017/7/21.
 */

class RefreshDataDialog {

    private EditText txtConfirmCode;
    private MainActivity mainActivity;

    private AlertDialog dlg;

    private final static int MESSAGEWHAT_HTTPERROR = 0;
    private Handler msgHandler = null;

    public RefreshDataDialog(@NonNull MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        initUI();
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.refreshdata_dialog_layout, null);
        txtConfirmCode = (EditText) view.findViewById(R.id.txtConfirmCode);

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Refresh Data")
                .setIcon(R.drawable.info)
                .setPositiveButton("Refresh", null)
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
                        doRefresh();
                    }
                });
            }
        });
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        initHandler();
    }

    private void initHandler() {
        msgHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MESSAGEWHAT_HTTPERROR:
                        Toast.makeText(mainActivity, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void doRefresh(){
        final String code = txtConfirmCode.getText().toString();
        if (code == null || code.length() == 0){
            Toast.makeText(mainActivity, "Please input confirm code.", Toast.LENGTH_LONG).show();
            return;
        }
        dlg.dismiss();
        mainActivity.startProgressDialog("loading", "loading, please wait...");
        if (mainActivity.getConfigsMap() != null && mainActivity.getConfigsMap().get(InstantValue.CONFIGS_CONFIRMCODE) != null){
            new Thread(){
                @Override
                public void run() {
                    if (mainActivity.getConfigsMap().get(InstantValue.CONFIGS_CONFIRMCODE).equals(code)){
                        mainActivity.onRefreshData();
                        dlg.dismiss();
                    } else {
                        msgHandler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_HTTPERROR, "confirm code is wrong"));
                        mainActivity.stopProgressDialog();
                    }
                }
            }.start();
        } else {
            CommonTool.popupToast(mainActivity, "Confirm code is null now, please restart this app to load it", Toast.LENGTH_LONG);
        }

    }

    public void showDialog(){
        dlg.show();
    }

    public void dismiss(){
        dlg.dismiss();
    }
}
