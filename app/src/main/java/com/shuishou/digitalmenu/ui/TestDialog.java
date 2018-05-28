package com.shuishou.digitalmenu.ui;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Administrator on 28/05/2018.
 */

public class TestDialog implements View.OnClickListener {
    private MainActivity mainActivity;
    private EditText txtPassword;
    private EditText txtDishId;
    private Button btnTest1;
    private Button btnTest2;
    private Button btnTest3;
    private Button btnTest4;
    private AlertDialog dlg;
    private EditText txtInfo;
    public TestDialog(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        initUI();
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.testdialog_layout, null);
        txtPassword = (EditText) view.findViewById(R.id.txtPassword);
        txtDishId = (EditText) view.findViewById(R.id.txtDishId);
        btnTest1 = (Button) view.findViewById(R.id.btnTest1);
        btnTest2 = (Button) view.findViewById(R.id.btnTest2);
        btnTest3 = (Button) view.findViewById(R.id.btnTest3);
        btnTest4 = (Button) view.findViewById(R.id.btnTest4);
        txtInfo = (EditText) view.findViewById(R.id.txtInfo);
        btnTest1.setOnClickListener(this);
        btnTest2.setOnClickListener(this);
        btnTest3.setOnClickListener(this);
        btnTest4.setOnClickListener(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
        builder.setNegativeButton("close", null);
        builder.setView(view);
        dlg = builder.create();
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 0;
        window.setAttributes(param);
    }

    public void showDialog(){
        dlg.show();
    }

    private boolean checkData(){
        if (txtPassword.getText() == null || txtPassword.getText().toString().length() == 0){
            Toast.makeText(mainActivity, "no password", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!"qqq".equals(txtPassword.getText().toString())){
            Toast.makeText(mainActivity, "password is wrong", Toast.LENGTH_LONG).show();
            return false;
        }
        if (txtDishId.getText() == null || txtDishId.getText().toString().length() == 0){
            Toast.makeText(mainActivity, "no dish id", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void appendInfo(String s){
        txtInfo.setText(txtInfo.getText() + "\n" + s);
    }
    private void addDishToList(){
        if (!checkData()){
            return;
        }
        Dish dish = getDishById(Integer.parseInt(txtDishId.getText().toString()));
        ChoosedDish choosedDish = new ChoosedDish(dish);
        appendInfo("----------------------------------------------");
        appendInfo("prepare to add a dish into list, current list.size = " +mainActivity.getChoosedDishList().size());
        mainActivity.getChoosedDishList().add(choosedDish);
        appendInfo("add a dish into lish, current list.size = " + mainActivity.getChoosedDishList().size());
        mainActivity.notifyChoosedDishFlavorChanged();
        appendInfo("notify lish data change");
    }

    private void changeTrolleyPrice(){
        if (!checkData())
            return;
        appendInfo("----------------------------------------------");
        int randomi = new Random().nextInt(100);
        mainActivity.getTvChoosedPrice().setText(String.valueOf(randomi));
        appendInfo("generated a random int " + randomi + ", and set to price TextView");
    }

    private Dish getDishById(int id){
        ArrayList<Category1> c1s = mainActivity.getMenu();
        for ( int i = 0; i < c1s.size(); i++) {
            ArrayList<Category2> c2s = c1s.get(i).getCategory2s();
            if (c2s != null) {
                for (int j = 0; j < c2s.size(); j++) {
                    ArrayList<Dish> dishes = c2s.get(j).getDishes();
                    if (dishes != null){
                        for (int k = 0; k < dishes.size(); k++) {
                            if (dishes.get(k).getId() == id)
                                return dishes.get(k);
                        }
                    }
                }
            }
        }
        return null;
    }

    private void attachDishQuantity(){
        if (!checkData())
            return;
        appendInfo("----------------------------------------------");
        int randomi = new Random().nextInt(100);
        DishCellComponent dc = mainActivity.getMapDishCellComponents().get(Integer.parseInt(txtDishId.getText().toString()));
        dc.changeAmount(randomi);
        appendInfo("generated a random int " + randomi + ", already attach on dish cell component");
    }

    private void compareComponent(){
        if (!checkData())
            return;
        appendInfo("----------------------------------------------");
        View trolleyPrice = mainActivity.getTvChoosedPrice();
        View recyclerList = mainActivity.getListViewChoosedDish();
        View parent = mainActivity.getWindow().getDecorView();
        boolean findTrolleyComp = findComponent(parent, trolleyPrice);
        boolean findList = findComponent(parent, recyclerList);
        appendInfo("find trolley text from MainActivity : " + findTrolleyComp);
        appendInfo("find choosed food list from MainActivity : " + findList);
    }

    /**
     * 查找target是不是存在于parent中
     * @param parent
     * @param target
     * @return
     */
    private boolean findComponent(View parent, View target){

        if (parent instanceof ViewGroup){
            ViewGroup vg = (ViewGroup)parent;
            for (int i = 0; i < vg.getChildCount(); i++) {
                if (vg.getChildAt(i) == target)
                    return true;
                if (vg.getChildAt(i) instanceof ViewGroup){
                    boolean findin = findComponent(vg.getChildAt(i), target);
                    if (findin)
                        return true;
                }
            }
        }
        return false;
    }
    @Override
    public void onClick(View v) {
        if (v == btnTest1){
            addDishToList();
        } else if (v == btnTest2){
            changeTrolleyPrice();
        } else if (v == btnTest3){
            attachDishQuantity();
        } else if (v == btnTest4){
            compareComponent();
        }
    }
}
