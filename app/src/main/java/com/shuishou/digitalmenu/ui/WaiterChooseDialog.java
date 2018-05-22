package com.shuishou.digitalmenu.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.UserData;

/**
 * Created by Administrator on 22/05/2018.
 */

public class WaiterChooseDialog {
    private AlertDialog dlg;
    private MainActivity mainActivity;

    private ListView lvWaiter;
    private static WaiterChooseDialog instance;

    public WaiterChooseDialog(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        initUI();
    }
    public static WaiterChooseDialog getInstance(MainActivity mainActivity){
        if (instance == null){
            instance = new WaiterChooseDialog(mainActivity);
        }
        return instance;
    }

    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.waiterlist_layout, null);
        lvWaiter = (ListView) view.findViewById(R.id.lvWaiter);
        String[] names = new String[mainActivity.getWaiters().size()];
        for (int i = 0; i < mainActivity.getWaiters().size(); i++) {
            names[i] = mainActivity.getWaiters().get(i).getUserName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_list_item_1, names);
        lvWaiter.setAdapter(adapter);
        lvWaiter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData user = mainActivity.getWaiters().get(position);
                mainActivity.getPostOrderDialog().setWaiter(user);
                dlg.dismiss();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
        builder.setView(view);
        dlg = builder.create();
    }

    public void showDialog(){
        dlg.show();
    }
}
