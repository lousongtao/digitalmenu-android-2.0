package com.shuishou.digitalmenu.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.io.IOOperator;

import java.util.ArrayList;

/**
 *
 * Created by Administrator on 2017/9/12.
 */

public class DishDisplayFragment extends Fragment {
    private Category2 category2;
    private int columnNumber; //每行显示的列数
    private int topMargin;
    private int leftMargin;
    private View view;
    private TableLayout contentLayout;
    private String logTag = "TestTime-DishFragment";
    public static final String BUNDLE_CATEGORY2 = "category2";
    public static final String BUNDLE_COLUMNS = "columns";
    public static final String BUNDLE_LEFTMARGIN = "leftmargin";
    public static final String BUNDLE_TOPMARGIN = "topmargin";
    private MainActivity mainActivity;

    public DishDisplayFragment(){

    }

    public void init(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        ScrollView sv = new ScrollView(mainActivity);
        ActionBar.LayoutParams ablp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sv.setLayoutParams(ablp);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.topMargin = topMargin;
        trlp.leftMargin = leftMargin;
        ArrayList<Dish> dishes = category2.getDishes();
        contentLayout = new TableLayout(mainActivity);
        sv.addView(contentLayout);
        if (dishes != null) {
            TableRow tr = null;
            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                if (i % columnNumber == 0) {
                    tr = new TableRow(mainActivity);
                    contentLayout.addView(tr);
                }
                DishCellComponent fc = new DishCellComponent(mainActivity, dish);
                tr.addView(fc.getDishCellView(), trlp);
                //这里要把fc先加入进tablerow才可以设置background,否则fc会被background的size撑大
                if (dish.getPictureName() != null) {
                    Drawable d = IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG + dish.getPictureName());
                    fc.setPicture(d);
                }
                mainActivity.getMapDishCellComponents().put(dish.getId(), fc);
            }
            

        }
        view = sv;
    }

    /**
     * 通过定时刷新, 发现有dish新增的操作, 使用该方法在内部插入一个DishCellComponent.
     * 不考虑Dish的sequence, 把DishCellComponent插入最后即可.
     * @param dish
     */
    public void addDishCell(Dish dish){
        DishCellComponent fc = new DishCellComponent(mainActivity, dish);
        mainActivity.getMapDishCellComponents().put(dish.getId(), fc);
        //得到最后一行, 并判断是否为空行, 若非空, 另起一行加入
        TableRow tr = null;
        if(contentLayout.getChildCount() > 0) {
            tr = (TableRow) contentLayout.getChildAt(contentLayout.getChildCount() - 1);
        }
        if (tr == null || tr.getChildCount() == columnNumber){
            tr = new TableRow(mainActivity);
            contentLayout.addView(tr);
        }
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.topMargin = topMargin;
        trlp.leftMargin = leftMargin;
        tr.addView(fc.getDishCellView(), trlp);
    }

    public void removeDishCell(Dish dish){
        for (int i = 0; i < contentLayout.getChildCount(); i++){
            TableRow tr = (TableRow) contentLayout.getChildAt(i);
            for (int j = 0; j < tr.getChildCount(); j++) {
                View v = tr.getChildAt(j);
                if (v.getTag() instanceof Dish
                        && ((Dish)v.getTag()).getId() == dish.getId()){
                    tr.removeView(v);
                    return;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        category2 = (Category2) args.get(BUNDLE_CATEGORY2);
        columnNumber = args.getInt(BUNDLE_COLUMNS);
        topMargin = args.getInt(BUNDLE_TOPMARGIN);
        leftMargin = args.getInt(BUNDLE_LEFTMARGIN);
    }

    public TableLayout getContentLayout() {
        return contentLayout;
    }

    public void setContentLayout(TableLayout contentLayout) {
        this.contentLayout = contentLayout;
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getMyView(){
        return view;
    }
}
