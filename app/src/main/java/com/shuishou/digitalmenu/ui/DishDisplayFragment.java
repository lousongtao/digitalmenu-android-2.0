package com.shuishou.digitalmenu.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.io.IOOperator;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/9/12.
 */

public class DishDisplayFragment extends Fragment {
    private Category2 category2;
    private TableLayout contentLayout;
    private View view;
    private String logTag = "TestTime-DishFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        category2 = (Category2) args.get("category2");
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

}
