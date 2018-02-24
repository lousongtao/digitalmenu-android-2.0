package com.shuishou.digitalmenu.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.ui.components.ChangeLanguageTextView;
import com.shuishou.digitalmenu.ui.components.DishNameTextView;

/**
 * Created by Administrator on 2016/12/31.
 */

class DishCellComponent{
    private TextView tvChoosedAmount;
    private static FrameLayout.LayoutParams choosedAmountParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final FrameLayout chooseButtonLayout;
    private final ImageView imgDishPicture;
    private final View foodCellView;
    private Dish dish;
    private ImageView ivSoldOut;
    private TextView tvOldPrice;
    private LinearLayout foodcellPriceLayout;
    private TextView foodPriceText;
    private MainActivity mainActivity;
    public DishCellComponent(final MainActivity mainActivity, Dish _dish){
        this.mainActivity = mainActivity;
        this.dish = _dish;
        foodCellView = LayoutInflater.from(mainActivity).inflate(R.layout.dishcell_layout, null);
        DishNameTextView foodNameText = (DishNameTextView) foodCellView.findViewById(R.id.foodNameText);
        chooseButtonLayout = (FrameLayout) foodCellView.findViewById(R.id.chooseButtonLayout);
        foodPriceText = (TextView) foodCellView.findViewById(R.id.foodPriceText);
        foodcellPriceLayout = (LinearLayout) foodCellView.findViewById(R.id.foodcellprice_layout);
        imgDishPicture = (ImageView)foodCellView.findViewById(R.id.layDishPicture);
        imgDishPicture.setTag(dish);
        imgDishPicture.setOnClickListener(ClickDishPictureListener.getInstance(mainActivity));
        ImageButton chooseButton = (ImageButton) foodCellView.findViewById(R.id.chooseBtn);
        chooseButton.setTag(dish);
        chooseButton.setOnClickListener(ChooseDishListener.getInstance(mainActivity));

        foodNameText.setTxtFirstLanguageName(dish.getFirstLanguageName());
        foodNameText.setTxtSecondLanguageName(dish.getSecondLanguageName());
        foodPriceText.setText(InstantValue.DOLLARSPACE + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, dish.getPrice()));
        foodNameText.show(mainActivity.getLanguage());

        if (dish.isPromotion()){
            setInPromotionVisibility(true);
        }
        if (dish.getHotLevel() > 0){
            ImageView img = new ImageView(mainActivity);
            if (dish.getHotLevel() == 1)
                img.setBackgroundResource(R.drawable.chili1);
            else if (dish.getHotLevel() == 2)
                img.setBackgroundResource(R.drawable.chili2);
            else if (dish.getHotLevel() == 3)
                img.setBackgroundResource(R.drawable.chili3);
            foodcellPriceLayout.addView(img);
        }
        if (dish.isSoldOut()){
            setSoldOutVisibility(dish.isSoldOut());
        }
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish){
        this.dish = dish;
    }

    public View getDishCellView() {
        return foodCellView;
    }

    public void setInPromotionVisibility(boolean b){
        dish.setPromotion(b);
        foodPriceText.setText(InstantValue.DOLLARSPACE + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, dish.getPrice()));
        if (b){
            if (tvOldPrice == null){
                tvOldPrice = new TextView(mainActivity);
                foodcellPriceLayout.addView(tvOldPrice);
            }
            tvOldPrice.setVisibility(View.VISIBLE);
            tvOldPrice.setText("was"+ InstantValue.DOLLAR + dish.getOriginPrice());
            tvOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            foodPriceText.setTextColor(mainActivity.getResources().getColor(R.color.red));
        } else {
            tvOldPrice.setVisibility(View.INVISIBLE);
            foodPriceText.setTextColor(mainActivity.getResources().getColor(R.color.color_OrangeTheme_ChoosedFoodFont));
        }
    }
    public void setSoldOutVisibility(boolean isSoldOut){
        dish.setSoldOut(isSoldOut);
        if (ivSoldOut == null){
            //create this image just when needed
            ivSoldOut = new ImageView(mainActivity);
            ivSoldOut.setImageResource(R.drawable.soldout);
            chooseButtonLayout.addView(ivSoldOut, 1);
        }
        ivSoldOut.setVisibility(dish.isSoldOut() ? View.VISIBLE : View.INVISIBLE);
    }

    public void setPicture(Drawable d){
        imgDishPicture.setImageDrawable(d);
    }

    public void changeAmount(int amount){
        if (tvChoosedAmount == null){
            tvChoosedAmount = new TextView(mainActivity);
            tvChoosedAmount.setTextSize(12);
            tvChoosedAmount.setTextColor(Color.parseColor("#FFFFFF"));
            tvChoosedAmount.setBackgroundResource(R.drawable.small_red_circle);
            chooseButtonLayout.addView(tvChoosedAmount, choosedAmountParam);
        }
        tvChoosedAmount.setText(String.valueOf(amount));
        if (amount == 0){
            chooseButtonLayout.removeView(tvChoosedAmount);
            tvChoosedAmount = null;
        }
    }

}
