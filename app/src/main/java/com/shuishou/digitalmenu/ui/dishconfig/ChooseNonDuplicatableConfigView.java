package com.shuishou.digitalmenu.ui.dishconfig;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.DishConfigGroup;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.ui.components.BorderView;

import java.util.ArrayList;

/**
 * 需要选择0个或者多个配置项, 且不允许重复的情况
 * Created by Administrator on 18/02/2018.
 */

public class ChooseNonDuplicatableConfigView extends BorderView implements DishConfigGroupIFC {
    private final static int ROW_COMPONENT_AMOUNT = 4;//每行显示的控件数目
    private DishConfigGroup group;
    private ArrayList<CheckBox> components = new ArrayList<>();

    public ChooseNonDuplicatableConfigView(MainActivity mainActivity, DishConfigGroup group, DishConfigDialogBuilder.ConfigClickListener listener){
        super(mainActivity);
        this.group = group;
        if (mainActivity.getLanguage() == MainActivity.LANGUAGE_FIRSTLANGUAGE){
            setTitle(group.getFirstLanguageName());
        } else {
            setTitle(group.getSecondLanguageName());
        }
        View v = buildView(group, listener);
        setContentView(v);
    }

    /**
     * 选择0个, 两个或多个, 不可重复的配置项. 每个配置项使用CheckBox
     * @param group
     * @return
     */
    private View buildView(DishConfigGroup group, DishConfigDialogBuilder.ConfigClickListener listener){
        TableLayout view = new TableLayout(mainActivity);
        ArrayList<DishConfig> configs = group.getDishConfigs();
        TableRow tableRow = null;
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.topMargin = 5;
        trlp.rightMargin = 20;
        for (int i = 0; i < configs.size(); i++) {
            DishConfig config = configs.get(i);

            if (i % ROW_COMPONENT_AMOUNT == 0) {
                tableRow = new TableRow(mainActivity);
                view.addView(tableRow);
            }
            if (config.isSoldOut())
                continue;
            CheckBox tb = new CheckBox(mainActivity);
            tb.setTextSize(25);
            tb.setTag(configs.get(i));
            tb.setEllipsize(TextUtils.TruncateAt.END);
            tb.setSingleLine(false);
//            tb.setMaxWidth(250);
            String txt = config.getFirstLanguageName();
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE){
                txt = config.getSecondLanguageName();
            }
            if (config.getPrice() > 0)
                txt += " +$" + config.getPrice();
            else if (config.getPrice() < 0)
                txt += " -$" + config.getPrice();
            tb.setText(txt);
            tb.setOnClickListener(listener);
            tableRow.addView(tb, trlp);
            components.add(tb);
        }
        view.setTag(group);//put group into view for data validation at last
        return view;
    }

    @Override
    public boolean checkData() {
        if (group.getRequiredQuantity() == 0)
            return true;
        else {
            return getChoosedData().size() == group.getRequiredQuantity();
        }
    }

    @Override
    public ArrayList<DishConfig> getChoosedData() {
        ArrayList<DishConfig> configs = new ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).isChecked()){
                configs.add((DishConfig)components.get(i).getTag());
            }
        }
        return configs;
    }

    @Override
    public DishConfigGroup getDishConfigGroup() {
        return group;
    }

    @Override
    public void onConfigComponentClick(DishConfig config){

    }

    @Override
    public void refreshColor() {
        for (CheckBox cb : components) {
            if (cb.isChecked()){
                cb.setBackgroundColor(Color.GREEN);
            } else {
                cb.setBackgroundColor(Color.WHITE);
            }
        }
    }
}
