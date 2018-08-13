package com.shuishou.digitalmenu.uibean;

import android.graphics.drawable.Drawable;

import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishConfig;
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
    private ArrayList<DishConfig> dishConfigList = new ArrayList<>();
    private ArrayList<Flavor> flavorList = new ArrayList<>();

    public ChoosedDish() {
    }

    public ChoosedDish(Dish dish) {
        this.dish = dish;
    }

    public String getFirstLanguageName() {
        return dish.getFirstLanguageName();
    }

    public String getSecondLanguageName() {
        return dish.getSecondLanguageName();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return dish.getPrice() + getAdjustPrice();
    }

    public double getAdjustPrice(){
        if (dishConfigList == null || dishConfigList.isEmpty())
            return 0;
        double ap = 0;
        for (int i = 0; i < dishConfigList.size(); i++) {
            ap += dishConfigList.get(i).getPrice();
        }
        return ap;
    }

    public Dish getDish() {
        return dish;
    }

    public ArrayList<DishConfig> getDishConfigList() {
        return dishConfigList;
    }

    public void setDishConfigList(ArrayList<DishConfig> dishConfigList) {
        this.dishConfigList = dishConfigList;
    }

    public void addDishConfig(DishConfig dishConfig) {
        this.dishConfigList.add(dishConfig);
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
