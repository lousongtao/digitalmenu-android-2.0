package com.shuishou.digitalmenu.ui;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.uibean.ChoosedDish;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/4.
 */

public class ChooseFlavorDialog {
    private float fontSize = 20;
    private MainActivity mainActivity;
    private ChoosedDish choosedDish;
    private LinearLayout frameChoosedFlavor;
    private LinearLayout frameAllFlavor;
    private LinearLayout frameChoosedSubitem;
    private ChooseFlavorListener chooseFlavorListener = new ChooseFlavorListener();
    private UnchooseFlavorListener unchooseFlavorListener = new UnchooseFlavorListener();
    private AlertDialog dlg;
    public ChooseFlavorDialog(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        initUI();
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.chooseflavor_layout, null);
        frameAllFlavor = (LinearLayout)view.findViewById(R.id.frame_flavors);
        frameChoosedFlavor = (LinearLayout)view.findViewById(R.id.frame_choosedflavor);
        frameChoosedSubitem = (LinearLayout)view.findViewById(R.id.frame_choosedSubitem);

        ArrayList<Flavor> flavors = mainActivity.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (int i = 0; i < flavors.size(); i++) {
                Flavor f = flavors.get(i);
                Button btn = new Button(mainActivity);
                btn.setTag(f);
                btn.setOnClickListener(chooseFlavorListener);
                btn.setTextSize(fontSize);
                if (mainActivity.getLanguage() == MainActivity.LANGUAGE_ENGLISH){
                    btn.setText(f.getEnglishName());
                } else {
                    btn.setText(f.getChineseName());
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
    }

    public void initValue(ChoosedDish choosedDish){
        this.choosedDish = choosedDish;
        frameChoosedFlavor.removeAllViews();
        frameChoosedSubitem.removeAllViews();
        if (choosedDish.getDishSubitemList().isEmpty()){
            frameChoosedSubitem.setVisibility(View.GONE);
        } else {
            frameChoosedSubitem.setVisibility(View.VISIBLE);

            for (int i = 0; i < choosedDish.getDishSubitemList().size(); i++) {
                DishChooseSubitem si = choosedDish.getDishSubitemList().get(i);
                TextView tv = new TextView(mainActivity);
                tv.setTag(si);
                tv.setTextSize(fontSize);
                if (mainActivity.getLanguage() == MainActivity.LANGUAGE_ENGLISH){
                    tv.setText(si.getEnglishName());
                } else {
                    tv.setText(si.getChineseName());
                }
                frameChoosedSubitem.addView(tv);
            }
        }

        for (int i = 0; i < choosedDish.getFlavorList().size(); i++) {
            Flavor f = choosedDish.getFlavorList().get(i);
            Button btn = new Button(mainActivity);
            btn.setTag(f);
            btn.setOnClickListener(unchooseFlavorListener);
            btn.setTextSize(fontSize);
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_ENGLISH){
                btn.setText(f.getEnglishName());
            } else {
                btn.setText(f.getChineseName());
            }
            frameChoosedFlavor.addView(btn);
        }
    }

//    public ChoosedDish getChoosedDish() {
//        return choosedDish;
//    }
//
//    public void setChoosedDish(ChoosedDish choosedDish) {
//        this.choosedDish = choosedDish;
//    }

    public void showDialog(){
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
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_ENGLISH){
                btn.setText(f.getEnglishName());
            } else {
                btn.setText(f.getChineseName());
            }
            frameChoosedFlavor.addView(btn);
            mainActivity.notifyChoosedFoodFlavorChanged(choosedDish);
        }
    }

    class UnchooseFlavorListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            choosedDish.getFlavorList().remove(v.getTag());
            frameChoosedFlavor.removeView(v);
            mainActivity.notifyChoosedFoodFlavorChanged(choosedDish);
        }
    }
}


