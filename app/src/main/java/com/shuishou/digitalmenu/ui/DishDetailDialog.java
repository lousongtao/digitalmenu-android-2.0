package com.shuishou.digitalmenu.ui;

import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/4.
 */

public class DishDetailDialog {
    private static DishDetailDialog instance;
    private ImageButton chooseButton;
    private ImageView imgHotLevel;
    private ImageView imgDishPicture;
    private TextView txtName;
    private TextView txtPrice;
    private TextView txtAmount;
    private Dish dish;
    private int choosedAmount;
    private MainActivity mainActivity;
    private AlertDialog dlg;
    private DishDetailDialog(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        initUI();
    }

    public static DishDetailDialog getInstance(MainActivity mainActivity){
        if (instance == null)
            instance = new DishDetailDialog(mainActivity);
        return instance;
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.dishdetail_layout, null);
        chooseButton = (ImageButton) view.findViewById(R.id.btnChoose_dishdetail);
        imgHotLevel = (ImageView) view.findViewById(R.id.imgHotLevel);
        imgDishPicture = (ImageView) view.findViewById(R.id.imgPicture);
        txtName = (TextView) view.findViewById(R.id.foodNameText);
        txtPrice = (TextView) view.findViewById(R.id.foodPriceText);
        txtAmount = (TextView) view.findViewById(R.id.txtAmount);
        txtAmount.setVisibility(View.INVISIBLE);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.onDishChoosed(dish);
                choosedAmount++;
                txtAmount.setVisibility(View.VISIBLE);
                txtAmount.setText(String.valueOf(choosedAmount));
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

        builder.setView(view);
        dlg = builder.create();
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 0;
        window.setAttributes(param);
    }

    public void showDialog(byte language, Dish dish, int choosedAmount){
        this.dish = dish;
        this.choosedAmount = choosedAmount;
        if (choosedAmount > 0){
            txtAmount.setText(String.valueOf(choosedAmount));
            txtAmount.setVisibility(View.VISIBLE);
        } else {
            txtAmount.setVisibility(View.INVISIBLE);
        }
        txtPrice.setText(InstantValue.DOLLARSPACE + dish.getPrice());
        switch (dish.getHotLevel()){
            case 0:
                imgHotLevel.setVisibility(View.INVISIBLE);
                break;
            case 1:
                imgHotLevel.setVisibility(View.VISIBLE);
                imgHotLevel.setImageResource(R.drawable.chili1);
                break;
            case 2:
                imgHotLevel.setVisibility(View.VISIBLE);
                imgHotLevel.setImageResource(R.drawable.chili2);
                break;
            case 3:
                imgHotLevel.setVisibility(View.VISIBLE);
                imgHotLevel.setImageResource(R.drawable.chili3);
                break;
            default:
        }
        imgDishPicture.setImageDrawable(IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_ORIGIN + dish.getPictureName()));
        if (language == MainActivity.LANGUAGE_CHINESE){
            txtName.setText(dish.getChineseName());
        } else {
            txtName.setText(dish.getEnglishName());
        }
        chooseButton.setTag(dish);
        dlg.show();
    }
}


