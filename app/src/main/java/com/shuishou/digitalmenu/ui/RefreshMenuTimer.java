package com.shuishou.digitalmenu.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.DishConfigGroup;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.bean.MenuVersionInfo;
import com.shuishou.digitalmenu.db.DBOperator;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;
import com.shuishou.digitalmenu.utils.CommonTool;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 27/05/2018.
 */

public class RefreshMenuTimer extends Timer {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RefreshMenuTimer.class.getSimpleName());
    private static final int REFRESHMENUHANDLER_MSGWHAT_REPLACE_PICTURE = 100; //replace picture

    private Handler refreshMenuHandler;
    private MainActivity mainActivity;

    public RefreshMenuTimer(final MainActivity mainActivity){
        this.mainActivity = mainActivity;
        refreshMenuHandler = new RefreshMenuHander();
        schedule(new TimerTask() {
            @Override
            public void run() {
                doRefreshMenuTimerAction();
            }
        }, 1, InstantValue.REFRESHMENUINTERVAL * 1000);
    }

    /**
     * query category1 from local database and insert it into the menu by its sequence
     * @param id
     */
    private void doRefreshCategory1Insert(int id){
        Category1 c1 = (Category1) mainActivity.getDbOperator().queryObjectById(id, Category1.class);
        for (int i = 0; i < mainActivity.getMenu().size(); i++) {
            if (mainActivity.getMenu().get(i).getSequence() > c1.getSequence()){
                mainActivity.getMenu().add(i, c1);
                break;
            }
        }
        mainActivity.getCategoryTabAdapter().notifyDataSetChanged();
    }

    /**
     * query the category1, replace the properties value, if the sequence changed, no need take care, the list component can do sort for display.
     * @param id
     */
    private void doRefreshCategory1Update(int id){
        Category1 c1 = (Category1) mainActivity.getDbOperator().queryObjectById(id, Category1.class);
        for (int i = 0; i < mainActivity.getMenu().size(); i++) {
            if (mainActivity.getMenu().get(i).getId() == id){
                mainActivity.getMenu().get(i).copyFrom(c1);
                break;
            }
        }
        mainActivity.getCategoryTabAdapter().notifyDataSetChanged();
    }

    private void doRefreshCategory1Delete(int id){
        for (int i = 0; i < mainActivity.getMenu().size(); i++) {
            if (mainActivity.getMenu().get(i).getId() == id){
                mainActivity.getMenu().remove(i);
                break;
            }
        }
        mainActivity.getCategoryTabAdapter().notifyDataSetChanged();
    }

    private void doRefreshCategory2Insert(int id){
        Category2 c2 = (Category2) mainActivity.getDbOperator().queryObjectById(id, Category2.class);
        int DISPLAY_DISH_COLUMN_NUMBER = 3; //菜单界面每行显示的数目/列数
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = (int) (displayMetrics.widthPixels / displayMetrics.density);
        int leftMargin = (screenWidth - 180 -260 - 3 * InstantValue.DISPLAY_DISH_WIDTH) / 4;
        if (leftMargin < 0){
            DISPLAY_DISH_COLUMN_NUMBER = 2; //for small screen, show 2 columns
        }
        if (leftMargin < 7)
            leftMargin = 7;
        DishDisplayFragment frag = new DishDisplayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DishDisplayFragment.BUNDLE_COLUMNS, DISPLAY_DISH_COLUMN_NUMBER);
        bundle.putInt(DishDisplayFragment.BUNDLE_LEFTMARGIN, (int)(leftMargin * displayMetrics.density));
        bundle.putInt(DishDisplayFragment.BUNDLE_TOPMARGIN, 15);
        bundle.putSerializable(DishDisplayFragment.BUNDLE_CATEGORY2, c2);
        frag.setArguments(bundle);
        frag.init(mainActivity);
        mainActivity.getMapDishDisplayFragments().put(c2.getId(), frag);
        mainActivity.getCategoryTabAdapter().notifyDataSetChanged();
    }

    /**
     * load Category2 data, and copy the properties value to the origin object in menu list
     * @param id
     */
    private void doRefreshCategory2Update(int id){
        ArrayList<Category1> menu = mainActivity.getMenu();
        if (menu == null)
            return;
        Category2 c2 = (Category2) mainActivity.getDbOperator().queryObjectById(id, Category2.class);
        for (int i = 0; i < menu.size(); i++) {
            Category1 c1 = menu.get(i);
            if (c1.getCategory2s() != null){
                for (int j = 0; j < c1.getCategory2s().size(); j++) {
                    Category2 c2j = c1.getCategory2s().get(j);
                    if (c2j.getId() == id){
                        c2j.copyFrom(c2);
                        break;
                    }
                }
            }
        }
        mainActivity.getCategoryTabAdapter().notifyDataSetChanged();
    }

    private void doRefreshCategory2Delete(int id){
        ArrayList<Category1> menu = mainActivity.getMenu();
        if (menu == null)
            return;
        for (int i = 0; i < menu.size(); i++) {
            Category1 c1 = menu.get(i);
            if (c1.getCategory2s() != null){
                for (int j = 0; j < c1.getCategory2s().size(); j++) {
                    Category2 c2 = c1.getCategory2s().get(j);
                    if (c2.getId() == id){
                        c1.getCategory2s().remove(j);
                        break;
                    }
                }
            }
        }
        mainActivity.getMapDishDisplayFragments().remove(id);
        mainActivity.getCategoryTabAdapter().notifyDataSetChanged();
    }

//    private void doRefreshDishConfigGroupUpdate(int id){
//        DishConfigGroup group = findDishConfigGroup(mainActivity.getMenu(), id);
//        DishConfigGroup dbGroup = (DishConfigGroup) mainActivity.getDbOperator().queryObjectById(id, DishConfigGroup.class);
//        if (group != null){
//            group.copyFrom(dbGroup);
//        }
//    }

//    private void doRefreshDishConfigGroupDelete(int id){
//        //nothing to do
//    }
//
//    private void doRefreshDishConfigGroupInsert(int id){
//        //nothing to do
//    }

//    private void doRefreshDishConfig(int dishConfigId){
//        DishConfig dbConfig = (DishConfig) mainActivity.getDbOperator().queryObjectById(dishConfigId, DishConfig.class);
//        for (Category1 c1 : mainActivity.getMenu()) {
//            if (c1.getCategory2s() != null) {
//                for (Category2 c2 : c1.getCategory2s()) {
//                    if (c2.getDishes() != null){
//                        for (Dish dish : c2.getDishes()){
//                            if (dish.getConfigGroups() != null){
//                                for (DishConfigGroup group : dish.getConfigGroups()){
//                                    if (group.getDishConfigs() != null){
//                                        for (DishConfig config : group.getDishConfigs()){
//                                            if (config.getId() == dishConfigId){
//                                                config.setSoldOut(dbConfig.isSoldOut());
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

//    private void doRefreshDishConfigInsert(int id){
//        DishConfig config = (DishConfig) mainActivity.getDbOperator().queryObjectById(id, DishConfig.class);
//        DishConfigGroup group = findDishConfigGroup(mainActivity.getMenu(), config.getGroup().getId());
//        if (group != null){
//            if (group.getDishConfigs() == null){
//                group.setDishConfigs(new ArrayList<DishConfig>());
//            }
//            group.getDishConfigs().add(config);
//        }
//    }
//
//    private void doRefreshDishConfigDelete(int id){
//        DishConfig config = (DishConfig) mainActivity.getDbOperator().queryObjectById(id, DishConfig.class);
//        DishConfigGroup group = findDishConfigGroup(mainActivity.getMenu(), config.getGroup().getId());
//        if (group != null){
//            for (int i = 0; i < group.getDishConfigs().size(); i++) {
//                if (group.getDishConfigs().get(i).getId() == id){
//                    group.getDishConfigs().remove(i);
//                    break;
//                }
//            }
//        }
//    }

//    private void doRefreshConfigGroupMoveInDish(int id){
//        Dish dish = findDish(mainActivity.getMenu(), id);
//        Dish dbDish = (Dish) mainActivity.getDbOperator().queryObjectById(id, Dish.class);
//        if (dish != null) {
//            dish.setConfigGroups(dbDish.getConfigGroups());
//        }
//    }

//    private void doRefreshConfigGroupMoveOutDish(int id){
//        Dish dish = findDish(mainActivity.getMenu(), id);
//        Dish dbDish = (Dish) mainActivity.getDbOperator().queryObjectById(id, Dish.class);
//        if (dish != null) {
//            dish.setConfigGroups(dbDish.getConfigGroups());
//        }
//    }
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
        Dish dish = findDish(mainActivity.getMenu(), dishId);
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
    private void doRefreshDishPicture(final int dishId){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshMenuHandler.sendMessage(CommonTool.buildMessage(REFRESHMENUHANDLER_MSGWHAT_REPLACE_PICTURE, dishId));
            }
        }, 10000);
    }

    private void logError(String log){
        LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " " + log);
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
        HttpOperator ho = mainActivity.getHttpOperator();
        ArrayList<MenuVersionInfo> mvs = ho.compareServerMenuVersion(localVersion);
        if (mvs != null && !mvs.isEmpty()){
            for (int i = 0; i < mvs.size(); i++) {
                MenuVersionInfo mvi = mvs.get(i);
//                if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGSOLDOUT){
//                    if (ho.synchronizeDishConfig(mvi.objectId)){
//                        persistMenuVersion(mvi.id);
//                        getRefreshMenuHandler().sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHCONFIGSOLDOUT, mvi.objectId));
//                    } else {
//                        break;
//                    }
//                } else
                if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHSOLDOUT
                        || mvi.type == InstantValue.MENUCHANGE_TYPE_DISHUPDATE
                        || mvi.type == InstantValue.MENUCHANGE_TYPE_CHANGEPROMOTION) {
                    if (synchronizeDishesUpdate(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        getRefreshMenuHandler().sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHUPDATE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishesUpdate type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHADD){
                    if (synchronizeDishInsert(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHADD, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDisheInsert type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHDELETE){
                    if (synchronizeDishesDelete(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHDELETE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishesDelete type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHPICTURE){
                    //同步图片需要先同步基本属性, 因为图片的文件名可能更改了
                    if (synchronizeDishesUpdate(mvi.objectId) && ho.synchronizeDishesPicture(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        doRefreshDishPicture(mvi.objectId);
                    } else {
                        logError("error occur while doing synchronizeDishesUpdate type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_CATEGORY1ADD){
                    if (ho.synchronizeCategory1Insert(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_CATEGORY1ADD, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeCategory1Insert type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_CATEGORY1UPDATE){
                    if (synchronizeCategory1Update(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_CATEGORY1UPDATE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeCategory1Update type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_CATEGORY1DELETE){
                    if (synchronizeCategory1Delete(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_CATEGORY1DELETE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeCategory1Delete type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_CATEGORY2ADD){
                    if (synchronizeCategory2Insert(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_CATEGORY2ADD, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeCategory2Insert type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_CATEGORY2UPDATE){
                    if (synchronizeCategory2Update(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_CATEGORY2UPDATE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeCategory2Update type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_CATEGORY2DELETE){
                    if (synchronizeCategory2Delete(mvi.objectId)){
                        persistMenuVersion(mvi.id);
                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_CATEGORY2DELETE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeCategory2Delete type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPADD){
                    if (ho.synchronizeDishConfigGroupInsert(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPADD, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishConfigGroupInsert type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPUPDATE){
                    if (synchronizeDisheConfigGroupUpdate(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPUPDATE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDisheConfigGroupUpdate type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPDELETE){
                    if (synchronizeDishConfigGroupDelete(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPDELETE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishConfigGroupDelete type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGADD){
                    //同步 DishConfig时, 要本地数据库中的数据更新, 同时将内存中的menu树对象进行替换, 不需要给handler发消息
                    if (synchronizeDishConfigInsert(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHCONFIGADD, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishConfigInsert type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGUPDATE
                            || mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGSOLDOUT){
                    //同步 DishConfig时, 要将本地数据库中的数据更新, 同时将内存中的menu树对象进行替换, 不需要给handler发消息
                    if (synchronizeDisheConfigUpdate(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHCONFIGUPDATE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDisheConfigUpdate type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGDELETE){
                    //同步 DishConfig时, 要将本地数据库中的数据更新, 同时将内存中的menu树对象进行替换, 不需要给handler发消息
                    if (synchronizeDishConfigDelete(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHCONFIGDELETE, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishConfigDelete type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHMOVEINCONFIGGROUP){
                    if (synchronizeDishMoveInDishConfigGroup(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHMOVEINCONFIGGROUP, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishMoveInDishConfigGroup type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHMOVEOUTCONFIGGROUP){
                    if (synchronizeDishMoveOutDishConfigGroup(mvi.objectId)){
                        persistMenuVersion(mvi.id);
//                        refreshMenuHandler.sendMessage(CommonTool.buildMessage(InstantValue.MENUCHANGE_TYPE_DISHMOVEOUTCONFIGGROUP, mvi.objectId));
                    } else {
                        logError("error occur while doing synchronizeDishMoveOutDishConfigGroup type : " + mvi.type + ", id : " + mvi.id + ", objectId : "
                                + mvi.objectId + ", current sync record : "+ i + ", total list size : " + mvs.size());
                        break;
                    }
                } else {
                    logError("find unrecognized type : " + mvi.type + ", id : " + mvi.id + ", objectId : "+ mvi.objectId);
                }
            }
        }
    }
    

    private void persistMenuVersion(int version){
        DBOperator dbOpr = mainActivity.getDbOperator();
        dbOpr.deleteAllData(MenuVersion.class);
        MenuVersion mv = new MenuVersion(1, version);
        dbOpr.saveObjectByCascade(mv);
    }

    private boolean synchronizeCategory1Update(int id){
        Category1 c1 = mainActivity.getHttpOperator().queryCategory1ById(id);
        if (c1 == null){
            sendErrorMessageToToast("cannot load Category1 by id = "+ id +" from server, please refresh data on this device.");
            return false;
        }
        DBOperator dbOpr = mainActivity.getDbOperator();
        Category1 dbc1 = (Category1)dbOpr.queryObjectById(id, Category1.class);
        if (dbc1 == null){
            sendErrorMessageToToast("find unrecognized Category1 '"+ c1.getFirstLanguageName()+"', please refresh data on this device.");
            return false;
        }
        dbc1.copyFrom(c1);
        dbOpr.updateObject(dbc1);
        return true;
    }

    private boolean synchronizeCategory2Update(int id){
        Category2 c2 = mainActivity.getHttpOperator().queryCategory2ById(id);
        if (c2 == null){
            sendErrorMessageToToast("cannot load Category2 by id = "+ id +" from server, please refresh data on this device.");
            return false;
        }
        DBOperator dbOpr = mainActivity.getDbOperator();
        Category2 dbc2 = (Category2)dbOpr.queryObjectById(id, Category2.class);
        if (dbc2 == null){
            sendErrorMessageToToast("find unrecognized Category2 '" + c2.getFirstLanguageName()+"', please refresh data on this device.");
            return false;
        }
        dbc2.copyFrom(c2);
        dbOpr.updateObject(dbc2);
        return true;
    }

    /**
     * this function is used for those dishes which properties changed, including change promption,
     * change price, change name, change soldout, change sequence, etc
     * load dishes data from server by the id list; then cover the local db data using the server data
     * @param
     * @return false while exception occur.
     */
    private boolean synchronizeDishesUpdate(int dishId){
        Dish dish = mainActivity.getHttpOperator().queryDishById(dishId);
        if (dish == null){
            sendErrorMessageToToast("cannot load dishes by id = "+ dishId +" from server, please refresh data on this device.");
            return false;
        }
        DBOperator dbOpr = mainActivity.getDbOperator();
        Dish dbDish = dbOpr.queryDishById(dish.getId());
        if (dbDish == null){
            sendErrorMessageToToast("find unrecognized dish '"+dish.getFirstLanguageName()+"', please refresh data on this device.");
            return false;
        }
        dbDish.copyFrom(dish);
        dbOpr.updateObject(dbDish);
        return true;
    }
    
    /**
     * 从服务端获得最新的数据, 并保存到本地数据库, 并替换内存中的menu对象
     * @param id
     * @return
     */
    private boolean synchronizeDisheConfigGroupUpdate(int id){
        DishConfigGroup group = mainActivity.getHttpOperator().queryDishConfigGroupById(id);
        if (group == null){
            sendErrorMessageToToast("cannot load DishConfigGroup by id = "+ id +" from server, please refresh data on this device.");
            return false;
        }
        DBOperator dbOpr = mainActivity.getDbOperator();
        DishConfigGroup dbGroup = (DishConfigGroup) dbOpr.queryObjectById(id, DishConfigGroup.class);
        if (dbGroup == null){
            sendErrorMessageToToast("find unrecognized DishConfigGroup '" + group.getUniqueName() + "', please refresh data on this device.");
            return false;
        }
        dbGroup.copyFrom(group);
        dbOpr.updateObject(dbGroup);
        //更新本地内存menu对象
        for ( int i = 0; i < mainActivity.getMenu().size(); i++) {
            Category1 c1 = mainActivity.getMenu().get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null){
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null){
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            Dish dish = c2.getDishes().get(k);
                            if (dish.getConfigGroups() != null){
                                for (int l = 0; l < dish.getConfigGroups().size(); l++) {
                                    DishConfigGroup dgroup = dish.getConfigGroups().get(l);
                                    if (dgroup.getId() == id){
                                        dish.getConfigGroups().set(l, group);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean synchronizeDisheConfigUpdate(int id){
        DishConfig dc = mainActivity.getHttpOperator().queryDishConfigById(id);
        if (dc == null){
            sendErrorMessageToToast("cannot load DishConfig by id = "+ id +" from server, please refresh data on this device.");
            return false;
        }
        DBOperator dbOpr = mainActivity.getDbOperator();
        DishConfig dbConfig = (DishConfig) dbOpr.queryObjectById(id, DishConfig.class);
        if (dbConfig == null){
            sendErrorMessageToToast("find unrecognized dishconfig '"+ dc.getFirstLanguageName()+"', please refresh data on this device.");
            return false;
        }
        dbConfig.copyFrom(dc);
        dbOpr.updateObject(dbConfig);
        //更新本地内存menu对象
        for ( int i = 0; i < mainActivity.getMenu().size(); i++) {
            Category1 c1 = mainActivity.getMenu().get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null){
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null){
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            Dish dish = c2.getDishes().get(k);
                            if (dish.getConfigGroups() != null){
                                for (int l = 0; l < dish.getConfigGroups().size(); l++) {
                                    DishConfigGroup dgroup = dish.getConfigGroups().get(l);
                                    if (dgroup.getDishConfigs() != null){
                                        for (int m = 0; m < dgroup.getDishConfigs().size(); m++) {
                                            DishConfig config = dgroup.getDishConfigs().get(m);
                                            if (config.getId() == id){
                                                config.copyFrom(dc);
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
        return true;
    }

    /**
     * 对于删除的Category1, 从本地删除
     */
    private boolean synchronizeCategory1Delete(int id){
        DBOperator dbOpr = mainActivity.getDbOperator();
        Category1 c1 = (Category1)dbOpr.queryObjectById(id, Category1.class);
        if (c1 != null){
            c1.setCategory2s(null);
            dbOpr.deleteObject(c1);
        }
        return true;
    }

    /**
     * 对于删除的Category2, 从本地删除
     */
    private boolean synchronizeCategory2Delete(int id){
        DBOperator dbOpr = mainActivity.getDbOperator();
        Category2 c2 = (Category2) dbOpr.queryObjectById(id, Category2.class);
        if (c2 != null){
            c2.setCategory1(null);//需要解除父关联, 否则删除后, 上级目录会同时删除
            c2.setDishes(null);
            dbOpr.deleteObject(c2);
        }
        return true;
    }

    /**
     * 对于删除的dish, 从本地删除
     * @param dishId
     * @return 如果dishIDList== null, 返回true, 否则, 在成功删除本地dish后, 返回true
     */
    private boolean synchronizeDishesDelete(int dishId){
        DBOperator dbOpr = mainActivity.getDbOperator();
        Dish dbDish = dbOpr.queryDishById(dishId);
        if (dbDish != null){
            dbDish.setCategory2(null);//需要解除父关联, 否则删除后, 上级目录会同时删除
            dbDish.setConfigGroups(null);
            dbOpr.deleteObject(dbDish);
        }
        return true;
    }

    private boolean synchronizeDishConfigGroupDelete(int id){
        DBOperator dbOpr = mainActivity.getDbOperator();
        Category2 c2 = (Category2) dbOpr.queryObjectById(id, Category2.class);
        if (c2 != null){
            c2.setCategory1(null);//需要解除父关联, 否则删除后, 上级目录会同时删除
            c2.setDishes(null);
            dbOpr.deleteObject(c2);
        }
        return true;
    }

    private boolean synchronizeDishConfigDelete(int id){
        DBOperator dbOpr = mainActivity.getDbOperator();
        DishConfig dc = (DishConfig) dbOpr.queryObjectById(id, DishConfig.class);
        if (dc != null){
            dc.setGroup(null);
            dbOpr.deleteObject(dc);
        }
        //更新本地内存menu对象
        for ( int i = 0; i < mainActivity.getMenu().size(); i++) {
            Category1 c1 = mainActivity.getMenu().get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null){
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null){
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            Dish dish = c2.getDishes().get(k);
                            if (dish.getConfigGroups() != null){
                                for (int l = 0; l < dish.getConfigGroups().size(); l++) {
                                    DishConfigGroup group = dish.getConfigGroups().get(l);
                                    if (group.getDishConfigs() != null){
                                        for (int m = 0; m < group.getDishConfigs().size(); m++) {
                                            if (group.getDishConfigs().get(m).getId() == id){
                                                group.getDishConfigs().remove(m);
                                                break;
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
        return true;
    }

    /**
     * 比较服务端dish跟本地dish的DishConfigGroup的差异
     * @param id dish.id
     * @return
     */
    private boolean synchronizeDishMoveInDishConfigGroup(int id){
        Dish serverDish = mainActivity.getHttpOperator().queryDishById(id);
        if (serverDish == null) {
            return false;
        }
        if (serverDish.getConfigGroups() == null || serverDish.getConfigGroups().isEmpty()){
            logError("doing synchronizeDishMoveInDishConfigGroup, but the dish data from server without any DishConfigGroup, dish.id : " + id);
            return false;
        }
        Dish localDish = findDish(mainActivity.getMenu(), id);
        if (localDish == null){
            logError("doing synchronizeDishMoveInDishConfigGroup, but the local dish cannot be found, dish.id : " + id);
            return false;
        }
        if (localDish.getConfigGroups() == null){
            localDish.setConfigGroups(new ArrayList<DishConfigGroup>());
        }
        localDish.copyFrom(serverDish);
        for ( int i = 0; i < serverDish.getConfigGroups().size(); i++) {
            DishConfigGroup group = serverDish.getConfigGroups().get(i);
            boolean find = false;
            for (int j = 0; j < localDish.getConfigGroups().size(); j++) {
                DishConfigGroup localGroup = localDish.getConfigGroups().get(j);
                if (localGroup.getId() == group.getId()){
                    find = true;
                    break;
                }
            }
            if (!find){
                localDish.getConfigGroups().add(group);
                mainActivity.getDbOperator().saveObjectByCascade(localDish);
            }

        }
        return true;
    }

    /**
     * 比较服务端dish跟本地dish的DishConfigGroup的差异
     * @param id dish.id
     * @return
     */
    private boolean synchronizeDishMoveOutDishConfigGroup(int id){
        Dish serverDish = mainActivity.getHttpOperator().queryDishById(id);
        if (serverDish == null)
            return false;

        Dish localDish = findDish(mainActivity.getMenu(), id);
        if (localDish == null){
            logError("doing synchronizeDishMoveOutDishConfigGroup, but the local dish cannot be found, dish.id : " + id);
            return false;
        }

        if (localDish.getConfigGroups() == null || localDish.getConfigGroups().isEmpty()){
            logError("doing synchronizeDishMoveOutDishConfigGroup, but the local dish does not contain any DishConfigGroup. dish.id : " + id);
            return false;
        }
        for ( int i = localDish.getConfigGroups().size() - 1; i >= 0; i--) {
            DishConfigGroup localGroup = localDish.getConfigGroups().get(i);
            boolean removeThis = true;
            if (serverDish.getConfigGroups() != null){
                for (int j = 0; j < serverDish.getConfigGroups().size(); j++) {
                    DishConfigGroup serverGroup = serverDish.getConfigGroups().get(j);
                    if (localGroup.getId() == serverGroup.getId()){
                        removeThis = false;
                        break;
                    }
                }
            }

            if (removeThis){
                localDish.getConfigGroups().remove(localGroup);
                mainActivity.getDbOperator().saveObjectByCascade(localDish);
                break;//每删除一个DishConfigGroup就退出循环
            }

        }
        return true;
    }

    /**
     * 对于新增的DishConfigGroup, 同步到本地时需要查询全部的menu树. 因为服务端对parent设置了gsonIgnore, 所以无法得到
     * 父对象数据. 只能拿全部的树结构向下查找对应id的DishConfigGroup.
     * 修改本地数据库后, 需要更新本地内存中的menu对象树
     * @param
     * @return 在成功保存数据后, 返回true
     */
    private boolean synchronizeDishConfigInsert(int id) {
        DBOperator dbOpr = mainActivity.getDbOperator();
        ArrayList<Category1> serverMenu = mainActivity.getHttpOperator().queryAllMenu();
        if (serverMenu == null)
            return false;
        boolean find = false;
        DishConfigGroup newGroup = null;
        for ( int i = 0; i < serverMenu.size(); i++) {
            Category1 c1 = serverMenu.get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null){
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null){
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            Dish dish = c2.getDishes().get(k);
                            if (dish.getConfigGroups() != null){
                                for (int l = 0; l < dish.getConfigGroups().size(); l++) {
                                    DishConfigGroup group = dish.getConfigGroups().get(l);
                                    if (group.getDishConfigs() != null){
                                        for (int m = 0; !find && m < group.getDishConfigs().size(); m++) {
                                            DishConfig dc = group.getDishConfigs().get(m);
                                            if (dc.getId() == id){
                                                find = true;

                                                DishConfigGroup localGroup = findDishConfigGroup(mainActivity.getMenu(), group.getId());
                                                if (localGroup == null){
                                                    logError("doing synchronizeDishConfigInsert, but cannot find the parent DishConfigGroup on this device by id : " + group.getId());
                                                    return false;
                                                }
                                                if (localGroup.getDishConfigs() == null){
                                                    localGroup.setDishConfigs(new ArrayList<DishConfig>());
                                                }
                                                localGroup.getDishConfigs().add(dc);
                                                dc.setGroup(localGroup);
                                                dbOpr.saveObjectByCascade(localGroup);
                                                newGroup = localGroup;
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
        //更新本地内存menu对象
        for ( int i = 0; i < mainActivity.getMenu().size(); i++) {
            Category1 c1 = mainActivity.getMenu().get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null){
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null){
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            Dish dish = c2.getDishes().get(k);
                            if (dish.getConfigGroups() != null){
                                for (int l = 0; l < dish.getConfigGroups().size(); l++) {
                                    DishConfigGroup group = dish.getConfigGroups().get(l);
                                    if (newGroup != null && group.getId() == newGroup.getId()){
                                        dish.getConfigGroups().set(l, newGroup);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 对于新增的Category2, 同步到本地时需要查询全部的menu树. 因为服务端对parent设置了gsonIgnore, 所以无法得到
     * 父对象数据. 只能拿全部的树结构向下查找对应id的Category2
     * @param
     * @return 在成功保存数据后, 返回true
     */
    private boolean synchronizeCategory2Insert(int id) {
        DBOperator dbOpr = mainActivity.getDbOperator();
        ArrayList<Category1> category1s = mainActivity.getHttpOperator().queryAllMenu();
        if (category1s == null)
            return false;
        for ( int i = 0; i < category1s.size(); i++) {
            Category1 c1 = category1s.get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null){
                for (int j = 0; j < c2s.size(); j++) {
                    if (c2s.get(j).getId() == id){
                        //此时要把对象跟mainActivity中持有的menu树绑定, 而不是绑定当前的category1对象, 否则DB在操作时,会导致数据库中的category2与category1解除关系
                        for (int k = 0; k < mainActivity.getMenu().size(); k++) {
                            Category1 localc1 = mainActivity.getMenu().get(k);
                            if (localc1.getId() == c1.getId()){
                                if (localc1.getCategory2s() == null){
                                    localc1.setCategory2s(new ArrayList<Category2>());
                                }
                                localc1.getCategory2s().add(c2s.get(j));
                                c2s.get(j).setCategory1(localc1);
                                dbOpr.saveObjectByCascade(c2s.get(j));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private DishConfigGroup findDishConfigGroup(ArrayList<Category1> category1s, int id){
        for ( int i = 0; i < category1s.size(); i++) {
            Category1 c1 = category1s.get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null) {
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null) {
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            Dish dish = c2.getDishes().get(k);
                            if (dish.getConfigGroups() != null) {
                                for (int l = 0; l < dish.getConfigGroups().size(); l++) {
                                    DishConfigGroup group = dish.getConfigGroups().get(l);
                                    if (group.getId() == id) {
                                        return group;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private DishConfig findDishConfig(ArrayList<Category1> category1s, int id){
        for ( int i = 0; i < category1s.size(); i++) {
            Category1 c1 = category1s.get(i);
            ArrayList<Category2> c2s = c1.getCategory2s();
            if (c2s != null) {
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null) {
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            Dish dish = c2.getDishes().get(k);
                            if (dish.getConfigGroups() != null) {
                                for (int l = 0; l < dish.getConfigGroups().size(); l++) {
                                    DishConfigGroup group = dish.getConfigGroups().get(l);
                                    if (group.getDishConfigs() != null){
                                        for (int m = 0; m < group.getDishConfigs().size(); m++) {
                                            if (group.getDishConfigs().get(m).getId() == id){
                                                return group.getDishConfigs().get(m);
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
        return null;
    }

    /**
     * 对于新增的dish, 同步到本地时需要查询全部的menu树. 因为服务端的dish对parent设置了gsonIgnore, 所以无法得到
     * 父对象数据. 只能拿全部的树结构向下查找对应id的dish
     * @param
     * @return 在成功保存dish数据后, 返回true
     */
    private boolean synchronizeDishInsert(int id){
        DBOperator dbOpr = mainActivity.getDbOperator();
        ArrayList<Category1> serverMenu = mainActivity.getHttpOperator().queryAllMenu();
        if (serverMenu == null)
            return false;
        boolean finddish = false;
        for ( int i = 0; i < serverMenu.size() && !finddish; i++) {
            ArrayList<Category2> c2s = serverMenu.get(i).getCategory2s();
            if (c2s != null){
                for (int j = 0; j < c2s.size() && !finddish; j++) {
                    ArrayList<Dish> dishes = c2s.get(j).getDishes();
                    if (dishes != null){
                        for (int k = 0; k < dishes.size(); k++) {
                            Dish dish = dishes.get(k);
                            if (dish.getId()==id){
                                //此时要把dish对象跟mainActivity中持有的menu树绑定, 而不是绑定当前的category2对象, 否则DB在操作时,会导致数据库中的category2与category1解除关系
                                finddish = true;
                                Category2 c2 = findCategory2(mainActivity.getMenu(), c2s.get(j).getId());
                                if (c2 != null) {
                                    dish.setCategory2(c2);
                                    if (c2.getDishes() == null){
                                        c2.setDishes(new ArrayList<Dish>());
                                    }
                                    c2.getDishes().add(dish);
                                    dbOpr.saveObjectByCascade(dish);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 在菜单树中查找id对应的category2对象
     * @param c1s 对象树
     * @param id category2的id
     * @return 如果找不到, 返回null
     */
    private Category2 findCategory2(ArrayList<Category1> c1s, int id){
        for ( int i = 0; i < c1s.size(); i++) {
            ArrayList<Category2> c2s = c1s.get(i).getCategory2s();
            if (c2s != null) {
                for (int j = 0; j < c2s.size(); j++) {
                    if (c2s.get(j).getId() == id){
                        return c2s.get(j);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 在菜单树中查找id对应的dish对象
     * @param c1s 对象树
     * @param id dish的id
     * @return 如果找不到, 返回null
     */
    private Dish findDish(ArrayList<Category1> c1s, int id){
        for ( int i = 0; i < c1s.size(); i++) {
            ArrayList<Category2> c2s = c1s.get(i).getCategory2s();
            if (c2s != null) {
                for (int j = 0; j < c2s.size(); j++) {
                    Category2 c2 = c2s.get(j);
                    if (c2.getDishes() != null){
                        for (int k = 0; k < c2.getDishes().size(); k++) {
                            if (c2.getDishes().get(k).getId() == id){
                                return c2.getDishes().get(k);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void sendErrorMessageToToast(String sMsg){
        mainActivity.getToastHandler().sendMessage(CommonTool.buildMessage(MainActivity.TOASTHANDLERWHAT_ERRORMESSAGE,sMsg));
    }

    private Handler getRefreshMenuHandler(){
        return refreshMenuHandler;
    }

    public MainActivity getMainActivity(){
        return mainActivity;
    }

    @SuppressLint("HandlerLeak")
    class RefreshMenuHander extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHUPDATE){
                doRefreshDishUpdate((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHADD){
                doRefreshDishInsert((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHDELETE){
                doRefreshDishDelete((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHPICTURE){
                doRefreshDishPicture((int)msg.obj);
            } else if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REPLACE_PICTURE){
                int dishId = (int)msg.obj;
                Dish dish = mainActivity.getDbOperator().queryDishById(dishId);
                DishCellComponent fc = mainActivity.getMapDishCellComponents().get(dishId);
                Drawable d = IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG + dish.getPictureName());
                if (fc != null && d != null)
                    fc.setPicture(d);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_CATEGORY1ADD){
                doRefreshCategory1Insert((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_CATEGORY1UPDATE){
                doRefreshCategory1Update((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_CATEGORY1DELETE){
                doRefreshCategory1Delete((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_CATEGORY2ADD){
                doRefreshCategory2Insert((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_CATEGORY2UPDATE){
                doRefreshCategory2Update((int)msg.obj);
            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_CATEGORY2DELETE){
                doRefreshCategory2Delete((int)msg.obj);
            }
//            else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPADD){
//                doRefreshDishConfigGroupInsert((int)msg.obj);
//            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPUPDATE){
//                doRefreshDishConfigGroupUpdate((int)msg.obj);
//            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHCONFIGGROUPDELETE){
//                doRefreshDishConfigGroupDelete((int)msg.obj);
//            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHCONFIGSOLDOUT
//                || msg.what == InstantValue.MENUCHANGE_TYPE_DISHCONFIGUPDATE){
//                doRefreshDishConfig((int)msg.obj);
//            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHCONFIGADD){
//                doRefreshDishConfigInsert((int)msg.obj);
//            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHCONFIGDELETE){
//                doRefreshDishConfigDelete((int)msg.obj);
//            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHMOVEINCONFIGGROUP){
//                doRefreshConfigGroupMoveInDish((int)msg.obj);
//            } else if (msg.what == InstantValue.MENUCHANGE_TYPE_DISHMOVEOUTCONFIGGROUP){
//                doRefreshConfigGroupMoveOutDish((int)msg.obj);
//            }
        }
    }
}
