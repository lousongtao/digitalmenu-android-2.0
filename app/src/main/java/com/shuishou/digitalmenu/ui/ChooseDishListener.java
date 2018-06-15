package com.shuishou.digitalmenu.ui;

import android.view.View;

import com.shuishou.digitalmenu.bean.Dish;

/**
 * click the dish choose button to add it into choosed list
 * Created by Administrator on 2017/9/22.
 */

public class ChooseDishListener implements View.OnClickListener {
    private static ChooseDishListener instance;
    private MainActivity mainActivity;
    private ChooseDishListener(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public static ChooseDishListener getInstance(MainActivity mainActivity){
        if(instance == null){
            instance = new ChooseDishListener(mainActivity);
        }
        return instance;
    }

    public static ChooseDishListener getInstance(){
        return instance;
    }

    public static void rebuildInstance(MainActivity mainActivity){
        instance = new ChooseDishListener(mainActivity);
    }

    public static void release(){
        instance = null;
    }
    @Override
    public void onClick(View v) {
        if (v.getTag() != null && v.getTag().getClass().getName().equals(Dish.class.getName())){
            mainActivity.onDishChoosed((Dish)v.getTag());
        }
    }
}
