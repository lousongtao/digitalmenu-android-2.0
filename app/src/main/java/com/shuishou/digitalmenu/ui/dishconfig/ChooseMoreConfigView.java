package com.shuishou.digitalmenu.ui.dishconfig;

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.DishConfigGroup;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.ui.components.BorderView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 需要选择多个配置项, 且可以重复选择的
 * Created by Administrator on 18/02/2018.
 */

public class ChooseMoreConfigView extends BorderView implements DishConfigGroupIFC {
    private final static int ROW_COMPONENT_AMOUNT = 4;//每行显示的控件数目
    public final static String SHOWFIELD = "name";
    public final static String ENTITY = "entity";
    private DishConfigGroup group;
    private ArrayList<HashMap<String, Object>> choosedConfigs = new ArrayList<>();
    private SimpleAdapter adapter;
    private DishConfigDialogBuilder builder;
    public ChooseMoreConfigView(MainActivity mainActivity, DishConfigGroup group, DishConfigDialogBuilder.ConfigClickListener listener, DishConfigDialogBuilder builder){
        super(mainActivity);
        this.group = group;
        this.builder = builder;
        adapter = new SimpleAdapter(mainActivity, choosedConfigs, R.layout.choosedsubitem_layout, new String[]{SHOWFIELD}, new int[]{R.id.tvChoosedSubitem});
        if (mainActivity.getLanguage() == MainActivity.LANGUAGE_FIRSTLANGUAGE){
            setTitle(group.getFirstLanguageName());
        } else {
            setTitle(group.getSecondLanguageName());
        }
        View v = buildView(group, listener);
        setContentView(v);
    }

    /**
     * 必须选择两个或多个, 可重复的配置项; 把选中的结果放入一个List, 允许用户从List中移出已选择项
     * @param group
     * @return
     */
    private View buildView(DishConfigGroup group, final DishConfigDialogBuilder.ConfigClickListener listener){
        View view = View.inflate(mainActivity, R.layout.choosesubitem_layout, null);
        TableLayout configDisplayLayout = (TableLayout)view.findViewById(R.id.subitemdisplay_layout);
        TextView tvInfo = (TextView)view.findViewById(R.id.txtInfoChoosed);
        tvInfo.setText("Delete item by clicking. Need " + group.getRequiredQuantity());
        ArrayList<DishConfig> configs = group.getDishConfigs();

        TableRow tableRow = null;
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.topMargin = 10;
        trlp.leftMargin = 20;
        for (int i = 0; i < configs.size(); i++) {
            DishConfig config = configs.get(i);
            if (i % ROW_COMPONENT_AMOUNT == 0) {
                tableRow = new TableRow(mainActivity);
                configDisplayLayout.addView(tableRow);
            }
            if (config.isSoldOut())
                continue;
            TextView tv = new TextView(mainActivity);
            tv.setTextSize(25);
            tv.setTag(configs.get(i));
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setSingleLine();
            tv.setMaxWidth(250);
            String txt = config.getFirstLanguageName();
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE){
                txt = config.getSecondLanguageName();
            }
            if (config.getPrice() > 0)
                txt += " +$" + config.getPrice();
            else if (config.getPrice() < 0)
                txt += " -$" + config.getPrice();
            tv.setText(txt);

            tableRow.addView(tv, trlp);
            tv.setOnClickListener(listener);
        }


        ListView lvSubitem = (ListView) view.findViewById(R.id.listChoosed);
        lvSubitem.setAdapter(adapter);
        lvSubitem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                choosedConfigs.remove(position);
                adapter.notifyDataSetChanged();
                builder.onChooseChange();
            }
        });
        view.setTag(group);//put group into view for data validation at last
        return view;
    }

    @Override
    public boolean checkData() {
        if (choosedConfigs.size() != group.getRequiredQuantity()){
            String groupName = group.getFirstLanguageName();
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE)
                groupName = group.getSecondLanguageName();
            Toast.makeText(mainActivity, "The choosed amount is not right, you should choose " + group.getRequiredQuantity() + " in group ["+groupName+"]", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<DishConfig> getChoosedData() {
        ArrayList<DishConfig> configs = new ArrayList<>();
        for (int i = 0; i < choosedConfigs.size(); i++) {
            configs.add((DishConfig)choosedConfigs.get(i).get(ENTITY));
        }
        return configs;
    }

    @Override
    public DishConfigGroup getDishConfigGroup() {
        return group;
    }

    /**
     * 对于多选且可重复的, 需要用一个List将所选结果列出
     * @param config
     */
    @Override
    public void onConfigComponentClick(DishConfig config){
        HashMap<String, Object> map = new HashMap<>();
        map.put(ENTITY, config);

        map.put(SHOWFIELD, config.getFirstLanguageName());
        if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE)
            map.put(SHOWFIELD, config.getSecondLanguageName());
        choosedConfigs.add(map);
        adapter.notifyDataSetChanged();
    }
}
