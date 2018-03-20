package com.shuishou.digitalmenu.ui;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.uibean.ChoosedDish;


import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/21.
 */

public class QuickSearchDialog {
    private EditText txtSearchCode;
    private TableLayout resultLayout;
    private ArrayList<View> resultCellList = new ArrayList<>(8);
    private AlertDialog dlg;

    private MainActivity mainActivity;
    private ChooseDishListener listener = new ChooseDishListener();
    private ArrayList<Dish> allDishes;

    public QuickSearchDialog(@NonNull MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        initUI();
        initData();
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.quicksearchdialog_layout, null);
        txtSearchCode = (EditText) view.findViewById(R.id.txtSearchCode);
        resultLayout = (TableLayout)view.findViewById(R.id.resultLayout);

        //预设8个查询结果, 避免创建太多对象
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell1));
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell2));
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell3));
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell4));
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell5));
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell6));
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell7));
        resultCellList.add(view.findViewById(R.id.quicksearchresultcell8));
        for(View v : resultCellList){
            ImageButton chooseButton = (ImageButton)v.findViewById(R.id.chooseBtn);
            chooseButton.setOnClickListener(listener);
        }

        txtSearchCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshResult();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setNegativeButton("Close", null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dlg = null;
            }
        });
        builder.setView(view);
        dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 0;
        window.setAttributes(param);
    }

    public void showDialog(){
        dlg.show();
    }

    private void initData(){
        allDishes = new ArrayList<>();
        for(Category1 c1 : mainActivity.getMenu()){
            if (c1.getCategory2s() != null){
                for(Category2 c2 : c1.getCategory2s()){
                    if (c2.getDishes() != null)
                        allDishes.addAll(c2.getDishes());
                }
            }
        }
    }

    private void refreshResult(){
        if (txtSearchCode.getText() == null || txtSearchCode.getText().length() < 2) {
            hideAllResultCell();
        }
        String code = txtSearchCode.getText().toString();
        ArrayList<Dish> results = new ArrayList<>();
        for(Dish d : allDishes){
            if (d.getFirstLanguageName().toLowerCase().contains(code.toLowerCase())
                    || (d.getSecondLanguageName() != null && d.getSecondLanguageName().toLowerCase().contains(code.toLowerCase()))
                    || (d.getAbbreviation() != null && d.getAbbreviation().toLowerCase().contains(code.toLowerCase()))){
                results.add(d);
            }
        }
        hideAllResultCell();
        if (results.size() == 0 || results.size() > 8){
            return;
        }
        for(int i = 0; i< results.size(); i++){
            Dish dish = results.get(i);
            View v = resultCellList.get(i);
            v.setVisibility(View.VISIBLE);
            TextView txtFirstLanguageName = (TextView)v.findViewById(R.id.txtFirstLanguageName);
            TextView txtSecondLanguageName = (TextView)v.findViewById(R.id.txtSecondLanguageName);
            TextView tvChoosedAmount = (TextView) v.findViewById(R.id.tvChoosedAmount);
            ImageButton chooseButton = (ImageButton) v.findViewById(R.id.chooseBtn);
            chooseButton.setTag(dish);

            txtFirstLanguageName.setText(dish.getFirstLanguageName());
            txtSecondLanguageName.setText(dish.getSecondLanguageName());
            for(ChoosedDish cd: mainActivity.getChoosedDishList()){
                if (dish.getId() == cd.getDish().getId()){
                    tvChoosedAmount.setVisibility(View.VISIBLE);
                    tvChoosedAmount.setText(String.valueOf(cd.getAmount()));
                    break;
                }
            }
        }
    }

    private void hideAllResultCell(){
        for(View v : resultCellList){
            v.setVisibility(View.INVISIBLE);
            TextView tvChoosedAmount = (TextView) v.findViewById(R.id.tvChoosedAmount);
            tvChoosedAmount.setText(InstantValue.NULLSTRING);
            tvChoosedAmount.setVisibility(View.INVISIBLE);
        }
    }

    class ChooseDishListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null && v.getTag().getClass().getName().equals(Dish.class.getName())){
                Dish dish = (Dish)v.getTag();
                mainActivity.onDishChoosed(dish);
                TextView tvChoosedAmount = (TextView) ((View)v.getParent()).findViewById(R.id.tvChoosedAmount);
                for(ChoosedDish cd: mainActivity.getChoosedDishList()){
                    if (dish.getId() == cd.getDish().getId()){
                        tvChoosedAmount.setVisibility(View.VISIBLE);
                        tvChoosedAmount.setText(String.valueOf(cd.getAmount()));
                        break;
                    }
                }
            }
        }
    }
}
