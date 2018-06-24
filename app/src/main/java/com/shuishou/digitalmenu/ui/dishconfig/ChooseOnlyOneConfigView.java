package com.shuishou.digitalmenu.ui.dishconfig;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.DishConfigGroup;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.ui.components.BorderView;

import java.util.ArrayList;

/**
 * Created by Administrator on 18/02/2018.
 */

public class ChooseOnlyOneConfigView extends BorderView implements DishConfigGroupIFC{
    private final static int ROW_COMPONENT_AMOUNT = 4;//每行显示的控件数目
    private DishConfigGroup group;
    private ArrayList<RadioButton> components = new ArrayList<>();
    public ChooseOnlyOneConfigView(MainActivity mainActivity, DishConfigGroup group, DishConfigDialogBuilder.ConfigClickListener listener){
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
     * 必须选择一个配置项, 使用RadioButton,
     * android 的RadioGroup继承自LinearLayout, 在控件多的时候不会自动换行, 如果采用TableLayout控制界面布局, 就需要自己实现单选切换功能. 否则需要定制自己的RadioGroup.
     * 权衡之下, 自己实现单选切换比较容易, 故这里返回的View是个TableLayout
     * @param group
     * @return
     */
    private View buildView(DishConfigGroup group, DishConfigDialogBuilder.ConfigClickListener listener){
        TableLayout view = new TableLayout(mainActivity);
//        view.setOrientation(LinearLayout.HORIZONTAL);
        ArrayList<DishConfig> configs = group.getDishConfigs();
        if (configs == null)
            return view;
        TableRow tableRow = null;
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.topMargin = 5;
        trlp.rightMargin = 20;
        boolean checkFirst = false;//对第一个控件进行选中, 由于soldout参数的存在, 不可以使用i=0作为第一个控件的判断方法
        for (int i = 0; i < configs.size(); i++) {
            DishConfig config = configs.get(i);
            if (i % ROW_COMPONENT_AMOUNT == 0) {
                tableRow = new TableRow(mainActivity);
                view.addView(tableRow);
            }
            if (config.isSoldOut())
                continue;
            RadioButton rb = new RadioButton(mainActivity);
            rb.setTextSize(25);
            rb.setTag(config);
            rb.setEllipsize(TextUtils.TruncateAt.END);
            rb.setSingleLine();
//            rb.setMaxWidth(250);
            String txt = config.getFirstLanguageName();
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE){
                txt = config.getSecondLanguageName();
            }
            if (config.getPrice() > 0)
                txt += " +$" + config.getPrice();
            else if (config.getPrice() < 0)
                txt += " -$" + config.getPrice();
            rb.setText(txt);
            rb.setOnClickListener(listener);
            tableRow.addView(rb, trlp);
            components.add(rb);
            //always set first item as selected, 要把RadioButton先加入RadioGroup之后才能设置checked, 否则不会跟其他的button切换状态
            if (!checkFirst) {
                rb.setChecked(true);
                rb.setBackgroundColor(Color.GREEN);
                checkFirst = true;
            }
        }
        view.setTag(group);//put group into view for data validation at last
        return view;
    }

    @Override
    public boolean checkData() {
        ArrayList<DishConfig> cs = getChoosedData();
        if (cs.size() != 1){
            String groupName = group.getFirstLanguageName();
            if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE)
                groupName = group.getSecondLanguageName();
            Toast.makeText(mainActivity, "Should choose ONE item in group [" + groupName + "]", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
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

    /**
     * 根据选中的config对象,找到对应radioButton, 将其设置为选中状态, 同时将其他的设置为非选中
     * @param config
     */
    @Override
    public void onConfigComponentClick(DishConfig config){
        for (RadioButton rb : components){
            rb.setChecked(rb.getTag() == config);
        }
    }

    @Override
    public void refreshColor() {
        for (RadioButton rb : components){
            if (rb.isChecked())
                rb.setBackgroundColor(Color.GREEN);
            else
                rb.setBackgroundColor(Color.WHITE);
        }
    }
}
