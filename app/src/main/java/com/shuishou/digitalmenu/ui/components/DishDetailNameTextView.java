package com.shuishou.digitalmenu.ui.components;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 在dish的detail界面显示dishname, 根据名称的不同长度, 使用不同大小的字体.
 * Created by Administrator on 2017/1/24.
 */

public class DishDetailNameTextView extends ChangeLanguageTextView {
    private int bigfont = 32;
    private int middlefont = 20;
    private int smallfont = 15;
    public DishDetailNameTextView(Context context){
        super(context);
    }

    public DishDetailNameTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show(byte language){
        super.show(language);
        if (getText().length() < 20) {
            setTextSize(bigfont);
            setMaxLines(1);
        }else if (getText().length() < 80) {
            setTextSize(middlefont);
            setMaxLines(2);
        }else {
            setTextSize(smallfont);
            setMaxLines(2);
        }
    }
}
