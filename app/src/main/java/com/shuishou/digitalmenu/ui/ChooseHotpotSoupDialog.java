package com.shuishou.digitalmenu.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Desk;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.HttpResult;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/21.
 */

public class ChooseHotpotSoupDialog {
    private AlertDialog dlg;
    private CheckBox cb1;
    private CheckBox cb2;
    private CheckBox cb3;
    private CheckBox cb4;
    private CheckBox cb5;
    private CheckBox cb6;
    private CheckBox cb7;
    private MainActivity mainActivity;

    private Dish dish;
    private int requireamount = 0;
    public ChooseHotpotSoupDialog(@NonNull MainActivity mainActivity, Dish dish) {
        this.mainActivity = mainActivity;
        this.dish = dish;
        initUI();
    }

    private void initUI(){
        int num = 0;
        if (dish.getEnglishName().toLowerCase().indexOf("single") >= 0) {
            num = 1;
        } else if (dish.getEnglishName().toLowerCase().indexOf("double") >= 0){

            num = 2;
        } else if (dish.getEnglishName().toLowerCase().indexOf("triple") >= 0
                || dish.getEnglishName().toLowerCase().indexOf("three" +
                "") >= 0){
            num = 3;
        }
        requireamount = num;
        TableLayout view = new TableLayout(mainActivity);
        cb1 = new CheckBox(mainActivity);
        cb2 = new CheckBox(mainActivity);
        cb3 = new CheckBox(mainActivity);
        cb4 = new CheckBox(mainActivity);
        cb5 = new CheckBox(mainActivity);
        cb6 = new CheckBox(mainActivity);
        cb7 = new CheckBox(mainActivity);
        if (mainActivity.getLanguage() == MainActivity.LANGUAGE_CHINESE){
            cb1.setText("重庆牛油老火锅");
            cb2.setText("麻辣海鲜锅");
            cb3.setText("酸萝卜猪蹄锅");
            cb4.setText("浓香西红柿锅");
            cb5.setText("四川酸菜锅");
            cb6.setText("菌菇汤");
            cb7.setText("鲜香浓汤锅");
        } else {
            cb1.setText("Chongqing Spicy Soup Base");
            cb2.setText("Spicy Seafood Soup Base");
            cb3.setText("Pig Trotter with Pickled Radish Soup Base");
            cb4.setText("Tomato Soup Base");
            cb5.setText("Szechuan Style Pickled Cabbage Soup Base");
            cb6.setText("Mushroom Soup Base");
            cb7.setText("Stock Soup Base");
        }
        TableRow row1 = new TableRow(mainActivity);
        row1.addView(cb1);
        row1.addView(cb2);
        row1.addView(cb3);
        row1.addView(cb4);
        TableRow row2 = new TableRow(mainActivity);
        row2.addView(cb5);
        row2.addView(cb6);
        row2.addView(cb7);
        view.addView(row1);
        view.addView(row2);
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        if (mainActivity.getLanguage() == MainActivity.LANGUAGE_CHINESE){
            builder.setTitle("选择锅底");
            builder.setMessage("您选择的是 "+ dish.getChineseName() +" , 请选择 " + num + " 个锅底.");
        } else {
            builder.setTitle("Flavor");
            builder.setMessage("Please choose "+ num + " flavor for " + dish.getEnglishName());
        }
        //here cannot use listener on the positive button because the dialog will dismiss no matter
        //the input value is valiable or not. I wish the dialog keep while input info is wrong.
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", null);
        builder.setView(view);
        dlg = builder.create();

        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //add listener for YES button
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onConfirm();
                    }
                });
            }
        });
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 50;
        window.setAttributes(param);
    }

    public void showDialog(){
        dlg.show();
    }

    private void onConfirm(){
        ArrayList<String> choosed = new ArrayList<>();
        if (cb1.isChecked()){
            choosed.add(cb1.getText().toString());
        }
        if (cb2.isChecked()){
            choosed.add(cb2.getText().toString());
        }
        if (cb3.isChecked()){
            choosed.add(cb3.getText().toString());
        }
        if (cb4.isChecked()){
            choosed.add(cb4.getText().toString());
        }
        if (cb5.isChecked()){
            choosed.add(cb5.getText().toString());
        }
        if (cb6.isChecked()){
            choosed.add(cb6.getText().toString());
        }
        if (cb7.isChecked()){
            choosed.add(cb7.getText().toString());
        }
        if (choosed.size() != requireamount){
            Toast.makeText(mainActivity, "The choosed amount is not right, you should choose " + requireamount, Toast.LENGTH_LONG).show();
            return;
        }
        mainActivity.addDishInChoosedList(dish, choosed.toString());
        dlg.dismiss();
    }

    public void dismiss(){
        dlg.dismiss();
    }

}
