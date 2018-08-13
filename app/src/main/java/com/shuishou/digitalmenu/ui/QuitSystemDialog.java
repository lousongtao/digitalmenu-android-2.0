package com.shuishou.digitalmenu.ui;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;

/**
 * Created by Administrator on 2017/7/21.
 */

class QuitSystemDialog {

    private EditText txtConfirmCode;
    private MainActivity mainActivity;

    private AlertDialog dlg;

    private final static int MESSAGEWHAT_HTTPERROR = 0;

    public QuitSystemDialog(@NonNull MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        initUI();
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.refreshdata_dialog_layout, null);
        txtConfirmCode = (EditText) view.findViewById(R.id.txtConfirmCode);

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("Exit System")
                .setIcon(R.drawable.info)
                .setPositiveButton("Quit", null)
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
                        doQuit();
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
        initHandler();
    }

    private void initHandler() {
        Handler msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGEWHAT_HTTPERROR:
                        Toast.makeText(mainActivity, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void doQuit(){
        final String code = txtConfirmCode.getText().toString();
        if (code == null || code.length() == 0){
            Toast.makeText(mainActivity, "Please input confirm code.", Toast.LENGTH_LONG).show();
            return;
        }
        if (mainActivity.getConfigsMap() != null && mainActivity.getConfigsMap().get(InstantValue.CONFIGS_CONFIRMCODE) != null){
            if (!code.equals(mainActivity.getConfigsMap().get(InstantValue.CONFIGS_CONFIRMCODE))){
                Toast.makeText(mainActivity, "Confirm code is wrong.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        System.exit(0);

    }

    public void showDialog(){
        dlg.show();
    }

    public void dismiss(){
        dlg.dismiss();
    }
}
