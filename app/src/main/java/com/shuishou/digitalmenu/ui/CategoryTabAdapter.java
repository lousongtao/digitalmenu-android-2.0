package com.shuishou.digitalmenu.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedFood;

import java.util.List;

/**
 * Created by Administrator on 2016/12/25.
 */

public class CategoryTabAdapter extends ArrayAdapter<Category1> {
    private int resourceId;
    private List<Category1> category1List;
    private ChangeLanguageTextView tvName;

    public CategoryTabAdapter(Context context, int resource, List<Category1> objects){
        super(context, resource, objects);
        resourceId = resource;
        category1List = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Category1 c1 = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        CategoryTabLayoutItem viewItem = (CategoryTabLayoutItem) view;
        viewItem.init(getContext(), c1);
        return view;
    }

}
