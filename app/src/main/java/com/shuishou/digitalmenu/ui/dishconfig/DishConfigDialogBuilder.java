package com.shuishou.digitalmenu.ui.dishconfig;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.DishConfigGroup;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.ui.components.BorderView;
import com.shuishou.digitalmenu.utils.CommonTool;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Administrator on 17/02/2018.
 */

public class DishConfigDialogBuilder {
    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DishConfigDialogBuilder.class.getSimpleName());
    private MainActivity mainActivity;
    private AlertDialog dialog;
    private ArrayList<DishConfigGroupIFC> groupViewList = new ArrayList<>();
    private TextView txtInfo;
    public DishConfigDialogBuilder(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void showConfigDialog(final Dish dish){
        ArrayList<DishConfigGroup> groups = dish.getConfigGroups();
        Collections.sort(groups, new Comparator<DishConfigGroup>() {
            @Override
            public int compare(DishConfigGroup o1, DishConfigGroup o2) {
                return o1.getSequence() - o2.getSequence();
            }
        });
        ConfigClickListener listener = new ConfigClickListener();
        txtInfo = new TextView(mainActivity);
        txtInfo.setTextSize(30);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 0, 0 ,0);
        LinearLayout contentview = new LinearLayout(mainActivity);
        contentview.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < groups.size(); i++) {
            DishConfigGroup group = groups.get(i);
            if (group.getRequiredQuantity() == 1){
                BorderView bv = new ChooseOnlyOneConfigView(mainActivity, group, listener);
                contentview.addView(bv);
                groupViewList.add((DishConfigGroupIFC) bv);
            } else {
                if (group.isAllowDuplicate()){
                    BorderView bv = new ChooseDuplicatableConfigView(mainActivity, group, listener, this);
                    contentview.addView(bv);
                    groupViewList.add((DishConfigGroupIFC) bv);
                } else {
                    BorderView bv = new ChooseNonDuplicatableConfigView(mainActivity, group, listener);
                    contentview.addView(bv);
                    groupViewList.add((DishConfigGroupIFC) bv);
                }
            }
        }
        ScrollView scrollView = new ScrollView(mainActivity);
        scrollView.addView(contentview);
        LinearLayout view = new LinearLayout(mainActivity);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setLayoutParams(layoutParams);
        view.addView(txtInfo, layoutParams);
        view.addView(scrollView);
        dialog = new AlertDialog.Builder(mainActivity)
                .setIcon(R.drawable.config)
                .setTitle("Choose Favorites")
                //.setMessage("")//must set an empty String, otherwise the dialog cannot change Message info; while there are too many components in dialog, the message are can be pushed out side, so using a textview instead of the message component
                .setNegativeButton("Cancel", null)
                .setPositiveButton("  OK  ", null)
                .setView(view)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                //add listener for YES button
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        onFinish(dish);
                    }
                });
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " lousongtao test : Cancel to choose favorites");
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 50;
        param.width = WindowManager.LayoutParams.MATCH_PARENT;
        param.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(param);
        onChooseChange();//initial the choosed info
    }

    private void onFinish(Dish dish){
        ArrayList<DishConfig> choosedConfigs = new ArrayList<>();
        for (int i = 0; i < groupViewList.size(); i++) {
            DishConfigGroupIFC cview = groupViewList.get(i);
            if (!cview.checkData())
                return;
            choosedConfigs.addAll(cview.getChoosedData());
        }
        LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " lousongtao test : do choose favorites");
        mainActivity.addDishInChoosedList(dish, choosedConfigs);
        dialog.dismiss();
    }

    public void onChooseChange(){
        double price = 0;
        String msg = "";
        for (int i = 0; i < groupViewList.size(); i++) {
            DishConfigGroupIFC cview = groupViewList.get(i);

            ArrayList<DishConfig> choosedConfigs = cview.getChoosedData();
            if (!choosedConfigs.isEmpty()){
                DishConfigGroup group = cview.getDishConfigGroup();
                String groupName = group.getFirstLanguageName();
                if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE)
                    groupName = group.getSecondLanguageName();
                if (msg.length() == 0)
                    msg += groupName + "=";
                else
                    msg += "; " + groupName + "=";

                for (int j = 0; j < choosedConfigs.size(); j++){
                    DishConfig config = choosedConfigs.get(j);

                    price += config.getPrice();
                    String configName = config.getFirstLanguageName();
                    if (mainActivity.getLanguage() == MainActivity.LANGUAGE_SECONDLANGUAGE)
                        configName = config.getSecondLanguageName();
                    if (j > 0)
                        msg += "/";
                    msg += configName;
                }
            }
        }
        String pricepm = "+";
        if (price < 0)
            pricepm = "-";
        txtInfo.setText("Price : " + pricepm +"$"+ CommonTool.transferDouble2Scale(Math.abs(price)) + ", " + msg);
    }

    class ConfigClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof DishConfig){
                //首先响应该Config控件所属View的操作. 先用v逐级查找父节点, 直到找到DishConfigIFC类型的对象为止
                DishConfigGroupIFC cview = getParentView(v);
                if (cview != null){
                    cview.onConfigComponentClick((DishConfig)v.getTag());
                }

                onChooseChange();
            }
        }

        private DishConfigGroupIFC getParentView(View v){
            if (!(v.getParent() instanceof View))
                return null;
            if (v.getParent() instanceof DishConfigGroupIFC)
                return (DishConfigGroupIFC) v.getParent();
            return getParentView((View)v.getParent());
        }
    }
}
