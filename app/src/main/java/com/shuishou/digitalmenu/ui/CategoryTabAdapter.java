package com.shuishou.digitalmenu.ui;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.shuishou.digitalmenu.bean.Category1;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/12/25.
 */

public class CategoryTabAdapter extends ArrayAdapter<Category1> {
    private int resourceId;
//    private ArrayList<Category1> category1List;
//    private ChangeLanguageTextView tvName;
    private MainActivity mainActivity;

    public CategoryTabAdapter(MainActivity mainActivity, int resource, ArrayList<Category1> objects){
        super(mainActivity, resource, objects);
        this.mainActivity = mainActivity;
        resourceId = resource;
//        category1List = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Category1 c1 = getItem(position);
        CategoryTabLayoutItem view;
        if (convertView == null){
            view = (CategoryTabLayoutItem)LayoutInflater.from(mainActivity).inflate(resourceId, null);
            view.init(mainActivity, c1);
        } else {
            view = (CategoryTabLayoutItem)convertView;
            /**
             * here need to check the language before show, because the convertView is the object move out the screen and reused it,
             * so the language is different if user changed language before it move into the screen.
             */
            view.showByLanguage();
        }
        return view;
    }
}
