package com.shuishou.digitalmenu.ui;

import android.view.View;

import com.shuishou.digitalmenu.R;


/**
 * respond the three buttons for choosed food: plus, minus and flavor
 * Created by Administrator on 2017/11/4.
 */

public class ChoosedFoodClickListener implements View.OnClickListener{

    public static final String IMAGEBUTTON_TAG_KEY_ACTION_PLUS = "plus";
    public static final String IMAGEBUTTON_TAG_KEY_ACTION_MINUS = "minus";
    public static final String IMAGEBUTTON_TAG_KEY_ACTION_FLAVOR = "flavor";

    private static ChoosedFoodClickListener instance;
    private MainActivity mainActivity;
    private ChoosedFoodClickListener(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public static ChoosedFoodClickListener getInstance(MainActivity mainActivity){
        if (instance == null)
            instance = new ChoosedFoodClickListener(mainActivity);
        return instance;
    }

    @Override
    public void onClick(View v) {
        if (IMAGEBUTTON_TAG_KEY_ACTION_PLUS.equals(v.getTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_ACTION))){
            mainActivity.plusDish((int)v.getTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION));
        } else if (IMAGEBUTTON_TAG_KEY_ACTION_MINUS.equals(v.getTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_ACTION))){
            mainActivity.minusDish((int)v.getTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION));
        } else if (IMAGEBUTTON_TAG_KEY_ACTION_FLAVOR.equals(v.getTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_ACTION))){
            mainActivity.flavorDish((int)v.getTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION));
        }
    }

    public static void rebuildInstance(MainActivity mainActivity){
        instance = new ChoosedFoodClickListener(mainActivity);
    }
}
