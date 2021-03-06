package com.shuishou.digitalmenu.ui.components;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 在DishCellComponent中使用, 根据dishname的长度, 使用不同大小的字体
 * Created by Administrator on 2017/1/24.
 */

public class DishNameTextView extends ChangeLanguageTextView {
    public DishNameTextView(Context context){
        super(context);
    }

    public DishNameTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show(byte language){
        super.show(language);
        if (getText().length() < 15) {
            int bigfont = 20;
            setTextSize(bigfont);
            setMaxLines(1);
        }else if (getText().length() < 25) {
            int middlefont = 15;
            setTextSize(middlefont);
            setMaxLines(1);
        }else {
            int smallfont = 13;
            setTextSize(smallfont);
            setMaxLines(2);
        }
    }
}
