package com.shuishou.digitalmenu.ui;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/7/21.
 */

public class ChooseDishSubitemDialog {
    private AlertDialog dlg;
    private CheckBox cb1;
    private CheckBox cb2;
    private CheckBox cb3;
    private CheckBox cb4;
    private CheckBox cb5;
    private CheckBox cb6;
    private CheckBox cb7;
    private MainActivity mainActivity;

    private Dish dish;
    private int requireamount = 0;
    private ArrayList<HashMap<String, String>> choosedSubitem = new ArrayList<>();
    private SimpleAdapter adapter;
    private final static String SHOWFIELD = "name";
    private final static String TAG_SUBITEM = "SUBITEM";
    private final static String TAG_REMOVE = "REMOVE";
    private final static int ROWAMOUNT = 3;
    public ChooseDishSubitemDialog(@NonNull MainActivity mainActivity, Dish dish) {
        this.mainActivity = mainActivity;
        this.dish = dish;
        initUI();
    }

    private void initUI(){
        requireamount = dish.getSubitemAmount();
        SubitemClickListener clickListener = new SubitemClickListener();
        View view = View.inflate(mainActivity, R.layout.choosesubitem_layout, null);
        TableLayout subitemDisplayLayout = (TableLayout)view.findViewById(R.id.subitemdisplay_layout);
        ArrayList<DishChooseSubitem> subitems = dish.getChooseSubItems();

        TableRow tableRow = null;
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.topMargin = 10;
        trlp.leftMargin = 20;
        for (int i = 0; i < subitems.size(); i++) {
            if (i % ROWAMOUNT == 0) {
                tableRow = new TableRow(mainActivity);
                subitemDisplayLayout.addView(tableRow);
            }
            TextView tv = new TextView(mainActivity);
            tv.setTextSize(25);
            tv.setTag(TAG_SUBITEM);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setSingleLine();
            tv.setMaxWidth(250);
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_CHINESE){
                tv.setText(subitems.get(i).getChineseName());
            } else {
                tv.setText(subitems.get(i).getEnglishName());
            }
            tableRow.addView(tv, trlp);
            tv.setOnClickListener(clickListener);
        }
        adapter = new SimpleAdapter(mainActivity, choosedSubitem, R.layout.choosedsubitem_layout, new String[]{SHOWFIELD}, new int[]{R.id.tvChoosedSubitem});
        ListView lvSubitem = (ListView) view.findViewById(R.id.listChoosed);
        lvSubitem.setAdapter(adapter);
        lvSubitem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                choosedSubitem.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        TextView tvInfo = (TextView)view.findViewById(R.id.txtInfoChoosed);

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        if (mainActivity.getLanguage() == MainActivity.LANGUAGE_CHINESE){
            tvInfo.setText("点选列表项可删除");
            builder.setTitle("选择");
            builder.setMessage("您选择的是 "+ dish.getChineseName() +" , 请选择 " + requireamount + " 项.");
        } else {
            tvInfo.setText("Delete item by clicking");
            builder.setTitle("Flavor");
            builder.setMessage("Please choose "+ requireamount + " flavor for " + dish.getEnglishName());
        }
        //here cannot use listener on the positive button because the dialog will dismiss no matter
        //the input value is valiable or not. I wish the dialog keep while input info is wrong.
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", null);
        builder.setView(view);
        dlg = builder.create();

        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //add listener for YES button
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onConfirm();
                    }
                });
            }
        });
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 50;
        window.setAttributes(param);
    }

    public void showDialog(){
        dlg.show();
    }

    private void onConfirm(){
        if (choosedSubitem.size() != requireamount){
            Toast.makeText(mainActivity, "The choosed amount is not right, you should choose " + requireamount, Toast.LENGTH_LONG).show();
            return;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < requireamount; i++) {
            sb.append(choosedSubitem.get(i).get(SHOWFIELD));
        }
        mainActivity.addDishInChoosedList(dish, sb.toString());
        dlg.dismiss();
    }

    public void dismiss(){
        dlg.dismiss();
    }

    class SubitemClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if (TAG_SUBITEM.equals(v.getTag())){
                HashMap<String, String> map = new HashMap<>();
                map.put(SHOWFIELD, ((TextView)v).getText().toString());
                choosedSubitem.add(map);
                adapter.notifyDataSetChanged();
            } else if (TAG_REMOVE.equals(v.getTag())){

            }
        }
    }
}
