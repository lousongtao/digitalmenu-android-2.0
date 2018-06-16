package com.shuishou.digitalmenu.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.DishConfigGroup;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;
import com.shuishou.digitalmenu.utils.CommonTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 27/05/2018.
 */

public class RefreshMenuTimer extends Timer {
    public static final int REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_UPDATE = 1;
    public static final int REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_INSERT = 2;
    public static final int REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_DELETE = 3;
    public static final int REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_PICTURE = 4;
    public static final int REFRESHMENUHANDLER_MSGWHAT_REFRESHDISHCONFIG = 5;
    public static final int REFRESHMENUHANDLER_MSGWHAT_REPLACE_PICTURE = 6; //replace picture
    private int refreshMenuInterval = 60 * 1000;

    private Handler refreshMenuHandler;
    private MainActivity mainActivity;
    public RefreshMenuTimer(final MainActivity mainActivity){
        this.mainActivity = mainActivity;
        refreshMenuHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_UPDATE){
                    doRefreshDishUpdate((int)msg.obj);
                } else if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_INSERT){
                    doRefreshDishInsert((int)msg.obj);
                } else if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_DELETE){
                    doRefreshDishDelete((int)msg.obj);
                } else if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REFRESHDISH_PICTURE){
                    doRefreshDishPicture((int)msg.obj);
                } else if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REFRESHDISHCONFIG){
                    doRefreshDishConfig((int)msg.obj);
                } else if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REPLACE_PICTURE){
                    int dishId = (int)msg.obj;
                    Dish dish = mainActivity.getDbOperator().queryDishById(dishId);
                    DishCellComponent fc = mainActivity.getMapDishCellComponents().get(dishId);
                    Drawable d = IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG + dish.getPictureName());
                    if (d != null)
                        fc.setPicture(d);
                }
            }
        };
        schedule(new TimerTask() {
            @Override
            public void run() {
                doRefreshMenuTimerAction();
            }
        }, 1, refreshMenuInterval);
    }

    /**
     * dish属性改变时, 自动刷新dish的component信息
     * @param
     */
    private void doRefreshDishUpdate(int dishId){
        Dish dish = mainActivity.getDbOperator().queryDishById(dishId);
        DishCellComponent dishCell = mainActivity.getMapDishCellComponents().get(dish.getId());
        if (dishCell == null)
            return;//找不到DishCellComponent, 证明此时数据已经不一致, 为保持程序稳定性, 只要跳过这个记录即可, 不需报错
        dishCell.resetDishObject(dish);
        //刷新存在于选择列表中的dish, 如果已经soldout, 告警提示
        if (dish.isSoldOut()){
            for(ChoosedDish cf : mainActivity.getChoosedDishList()){
                if (cf.getDish().getId() == dishId){
                    if (dish.isSoldOut()) {
                        String errormsg = "Dish " + dish.getFirstLanguageName() + " is Sold Out already, please remove it from your selection.";
                        CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "Warning", errormsg);
                    }
                }
            }
        }
    }

    /**
     * 新增加的dish, 需要在其父对象Category2 的 fragment 上增加一个DishCellComponent,
     * 由于dish是新增数据, 在MainActivity中持有的menu树, 并不包含该对象, 所以同时要把dish与其父对象增加绑定关系.
     * @param
     */
    private void doRefreshDishInsert(int dishId){
        Dish dish = mainActivity.getDbOperator().queryDishById(dishId);
        if (dish == null){
            return;
        }
        Category2 category2 = dish.getCategory2();

        if (category2 == null)
            return;
        DishDisplayFragment frag = mainActivity.getMapDishDisplayFragments().get(category2.getId());
        if (frag == null)//如果dish属于某个新增的category2, 但是category2不在同步的范围内, 此时fragment是null, 无法插入控件
            return;
        frag.addDishCell(dish);
    }

    private void doRefreshDishDelete(int dishId){
        //dish在数据库中已经被删除, 需要遍历category2来找到对应的dish
        for(Category1 c1 : mainActivity.getMenu()){
            if (c1.getCategory2s() != null){
                for (Category2 c2 : c1.getCategory2s()){
                    if (c2.getDishes() != null){
                        for (Dish dish : c2.getDishes()){
                            if (dish.getId() == dishId){
                                DishDisplayFragment frag = mainActivity.getMapDishDisplayFragments().get(c2.getId());
                                frag.removeDishCell(dish);
                                c2.getDishes().remove(dish);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 图片加载使用的是异步方式, 所以需要延迟几秒钟刷新UI, 但是延迟不一定能解决问题, 比如图像download失败时就无法处理.
     * @param
     */
    public void doRefreshDishPicture(final int dishId){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshMenuHandler.sendMessage(CommonTool.buildMessage(REFRESHMENUHANDLER_MSGWHAT_REPLACE_PICTURE, dishId));
            }
        }, 10000);
    }

    private void doRefreshDishConfig(int dishConfigId){
        DishConfig dbConfig = (DishConfig) mainActivity.getDbOperator().queryObjectById(dishConfigId, DishConfig.class);
        for (Category1 c1 : mainActivity.getMenu()) {
            if (c1.getCategory2s() != null) {
                for (Category2 c2 : c1.getCategory2s()) {
                    if (c2.getDishes() != null){
                        for (Dish dish : c2.getDishes()){
                            if (dish.getConfigGroups() != null){
                                for (DishConfigGroup group : dish.getConfigGroups()){
                                    if (group.getDishConfigs() != null){
                                        for (DishConfig config : group.getDishConfigs()){
                                            if (config.getId() == dishConfigId){
                                                config.setSoldOut(dbConfig.isSoldOut());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void doRefreshMenuTimerAction(){
        if(InstantValue.URL_TOMCAT == null || InstantValue.URL_TOMCAT.length() == 0)
            return;
        //if local database is null, stop check
        if (mainActivity.getMenu() == null || mainActivity.getMenu().isEmpty())
            return;
        MenuVersion mv = (MenuVersion) mainActivity.getDbOperator().queryObjectById(1, MenuVersion.class);
        int localVersion = 0;
        if (mv != null) {
            localVersion = mv.getVersion();
        }
        mainActivity.getHttpOperator().checkMenuVersion(this, localVersion);
    }

    public Handler getRefreshMenuHandler(){
        return refreshMenuHandler;
    }

    public MainActivity getMainActivity(){
        return mainActivity;
    }
}
