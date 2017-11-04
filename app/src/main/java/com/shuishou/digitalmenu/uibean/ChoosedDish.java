package com.shuishou.digitalmenu.uibean;

import android.graphics.drawable.Drawable;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/12/25.
 */

public class ChoosedDish {
    private Dish dish;
    private int imageId;
    private Drawable image;
//    private String additionalRequirements = InstantValue.NULLSTRING;
    private int amount = 1; //default value
    private ArrayList<DishChooseSubitem> dishSubitemList = new ArrayList<>();
    private ArrayList<Flavor> flavorList = new ArrayList<>();

    public ChoosedDish() {
    }

    public ChoosedDish(Dish dish) {
        this.dish = dish;
    }

    public String getName_cn() {
        return dish.getChineseName();
    }

    public String getName_en() {
        return dish.getEnglishName();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return dish.getPrice();
    }

    public Dish getDish() {
        return dish;
    }

//    public String getAdditionalRequirements() {
//        return additionalRequirements;
//    }
//
//    public void setAdditionalRequirements(String additionalRequirements) {
//        this.additionalRequirements = additionalRequirements;
//    }

    public ArrayList<DishChooseSubitem> getDishSubitemList() {
        return dishSubitemList;
    }

    public void setDishSubitemList(ArrayList<DishChooseSubitem> dishSubitemList) {
        this.dishSubitemList = dishSubitemList;
    }

    public void addDishSubitemList(DishChooseSubitem dishSubitem) {
        this.dishSubitemList.add(dishSubitem);
    }

    public ArrayList<Flavor> getFlavorList() {
        return flavorList;
    }

    public void setFlavorList(ArrayList<Flavor> flavorList) {
        this.flavorList = flavorList;
    }

    public void addFlavorList(Flavor flavor) {
        this.flavorList.add(flavor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChoosedDish that = (ChoosedDish) o;

        return dish.getId() == that.getDish().getId();
    }

    @Override
    public int hashCode() {
        return getDish().getId();
    }
}
