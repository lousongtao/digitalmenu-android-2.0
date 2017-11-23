package com.shuishou.digitalmenu.ui;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.uibean.ChoosedDish;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/4.
 */

public class ChooseFlavorDialog {
    private static ChooseFlavorDialog instance;
    private float fontSize = 15;
    private MainActivity mainActivity;
    private ChoosedDish choosedDish;
    private LinearLayout frameChoosedFlavor;
    private LinearLayout frameAllFlavor;
    private EditText txtOtherFlavor;
    private TextView txtDishSubitem;
    private Button btnConfirmFlavor;
    private ChooseFlavorListener chooseFlavorListener = new ChooseFlavorListener();
    private UnchooseFlavorListener unchooseFlavorListener = new UnchooseFlavorListener();
    private AlertDialog dlg;
    private ChooseFlavorDialog(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        initUI();
    }

    public static ChooseFlavorDialog getInstance(MainActivity mainActivity){
        if (instance == null)
            instance = new ChooseFlavorDialog(mainActivity);
        return instance;
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.chooseflavor_layout, null);
        frameAllFlavor = (LinearLayout)view.findViewById(R.id.frame_flavors);
        frameChoosedFlavor = (LinearLayout)view.findViewById(R.id.frame_choosedflavor);
        txtDishSubitem = (TextView) view.findViewById(R.id.txtDishSubitem);
        txtOtherFlavor = (EditText)view.findViewById(R.id.txtOtherFlavor);
        btnConfirmFlavor = (Button)view.findViewById(R.id.btnConfirmFlavor);
        btnConfirmFlavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOtherFlavor();
            }
        });
        ArrayList<Flavor> flavors = mainActivity.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (int i = 0; i < flavors.size(); i++) {
                Flavor f = flavors.get(i);
                Button btn = new Button(mainActivity);
                btn.setTag(f);
                btn.setOnClickListener(chooseFlavorListener);
                btn.setTextSize(fontSize);
                if (mainActivity.getLanguage() == MainActivity.LANGUAGE_FIRSTLANGUAGE){
                    btn.setText(f.getFirstLanguageName());
                } else {
                    btn.setText(f.getSecondLanguageName());
                }
                frameAllFlavor.addView(btn);
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

        builder.setTitle("Choose your flavor")
                .setIcon(R.drawable.info)
                .setNegativeButton("OK", null)
                .setView(view);
        dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 0;
        window.setAttributes(param);
    }

    public void initValue(ChoosedDish choosedDish){
        this.choosedDish = choosedDish;
        frameChoosedFlavor.removeAllViews();
        if (choosedDish.getDishSubitemList().isEmpty()){
            txtDishSubitem.setVisibility(View.GONE);
        } else {
            txtDishSubitem.setVisibility(View.VISIBLE);
            StringBuffer sbSubitem = new StringBuffer();
            for (int i = 0; i < choosedDish.getDishSubitemList().size(); i++) {
                DishChooseSubitem si = choosedDish.getDishSubitemList().get(i);
                if (mainActivity.getLanguage() == MainActivity.LANGUAGE_FIRSTLANGUAGE){
                    sbSubitem.append(si.getFirstLanguageName());
                    sbSubitem.append(InstantValue.SPACESTRING);
                } else {
                    sbSubitem.append(si.getSecondLanguageName());
                    sbSubitem.append(InstantValue.SPACESTRING);
                }
            }
            txtDishSubitem.setText(sbSubitem.toString());
        }

        for (int i = 0; i < choosedDish.getFlavorList().size(); i++) {
            Flavor f = choosedDish.getFlavorList().get(i);
            Button btn = new Button(mainActivity);
            btn.setTag(f);
            btn.setOnClickListener(unchooseFlavorListener);
            btn.setTextSize(fontSize);
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_FIRSTLANGUAGE){
                btn.setText(f.getFirstLanguageName());
            } else {
                btn.setText(f.getSecondLanguageName());
            }
            frameChoosedFlavor.addView(btn);
        }
    }

    private void addOtherFlavor(){
        if (txtOtherFlavor.getText() == null || txtOtherFlavor.getText().length() == 0)
            return;
        //create a virtual Flavor object
        Flavor f = new Flavor();
        f.setFirstLanguageName(txtOtherFlavor.getText().toString());
        f.setSecondLanguageName(txtOtherFlavor.getText().toString());
        choosedDish.addFlavorList(f);
        final Button btn = new Button(mainActivity);
        btn.setTag(f);
        btn.setTextSize(fontSize);
        btn.setText(txtOtherFlavor.getText().toString());
        btn.setOnClickListener(unchooseFlavorListener);
        txtOtherFlavor.setText(InstantValue.NULLSTRING);
        frameChoosedFlavor.addView(btn);
        mainActivity.notifyChoosedDishFlavorChanged(choosedDish);
    }
    public void showDialog(){
            dlg.setTitle("Choose your flavor");
            btnConfirmFlavor.setText("ADD");
        dlg.show();
    }


    class ChooseFlavorListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            for (Flavor f : choosedDish.getFlavorList()){
                if (f.equals(v.getTag())){
                    return;
                }
            }
            choosedDish.addFlavorList((Flavor)v.getTag());
            Flavor f = (Flavor)v.getTag();
            Button btn = new Button(mainActivity);
            btn.setTag(f);
            btn.setOnClickListener(unchooseFlavorListener);
            btn.setTextSize(fontSize);
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_FIRSTLANGUAGE){
                btn.setText(f.getFirstLanguageName());
            } else {
                btn.setText(f.getSecondLanguageName());
            }
            frameChoosedFlavor.addView(btn);
            mainActivity.notifyChoosedDishFlavorChanged(choosedDish);
        }
    }

    class UnchooseFlavorListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            choosedDish.getFlavorList().remove(v.getTag());
            frameChoosedFlavor.removeView(v);
            mainActivity.notifyChoosedDishFlavorChanged(choosedDish);
        }
    }
}


