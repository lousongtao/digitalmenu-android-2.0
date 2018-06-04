package com.shuishou.digitalmenu.http;

import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Desk;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.bean.HttpResult;
import com.shuishou.digitalmenu.bean.Indent;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.bean.MenuVersionInfo;
import com.shuishou.digitalmenu.bean.UserData;
import com.shuishou.digitalmenu.db.DBOperator;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.utils.CommonTool;
import com.yanzhenjie.nohttp.FileBinary;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.download.DownloadListener;
import com.yanzhenjie.nohttp.download.DownloadQueue;
import com.yanzhenjie.nohttp.download.DownloadRequest;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/6/9.
 */

public class HttpOperator {

    private String logTag = "HttpOperation";

    public static final String DISHCHANGE_INSERT = "DISHCHANGE_INSERT";
    public static final String DISHCHANGE_UPDATE = "DISHCHANGE_UPDATE";
    public static final String DISHCHANGE_DELETE = "DISHCHANGE_DELETE";
    public static final String DISHCHANGE_PICTURE = "DISHCHANGE_PICTURE";
    public static final String DISHCONFIGCHANGE = "DISHCONFIGCHANGE";

    private MainActivity mainActivity;
//    private ArrayList<String> listDishPictures = new ArrayList<>();
    private static final int WHAT_VALUE_QUERYMENU = 1;
    private static final int WHAT_VALUE_QUERYFLAVOR = 2;
    private static final int WHAT_VALUE_QUERYDESK = 4;
    private static final int WHAT_VALUE_QUERYMENUVERSION = 5;
    private static final int WHAT_VALUE_QUERYCONFIGSMAP = 6;
    private static final int WHAT_VALUE_QUERYWAITER = 7;

    private Gson gson = new Gson();

    private OnResponseListener responseListener =  new OnResponseListener<JSONObject>() {
        @Override
        public void onStart(int what) {
        }

        @Override
        public void onSucceed(int what, Response<JSONObject> response) {
            switch (what){
                case WHAT_VALUE_QUERYMENU :
                    doResponseQueryMenu(response);
                    break;
                case WHAT_VALUE_QUERYCONFIGSMAP:
                    doResponseQueryConfigsMap(response);
                    break;
                case WHAT_VALUE_QUERYDESK :
                    doResponseQueryDesk(response);
                    break;
                case WHAT_VALUE_QUERYMENUVERSION:
                    doResponseQueryMenuVersion(response);
                    break;
                case WHAT_VALUE_QUERYFLAVOR:
                    doResponseQueryFlavor(response);
                    break;
                case WHAT_VALUE_QUERYWAITER:
                    doResponseQueryWaiter(response);
                    break;
                default:
            }
        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            Log.e("Http failed", "what = "+ what + "\nresponse = "+ response.get());
            MainActivity.LOG.error("Response Listener On Faid. what = "+ what + "\nresponse = "+ response.get());
            String msg = InstantValue.NULLSTRING;
            switch (what){
                case WHAT_VALUE_QUERYMENU :
                    msg = "Failed to load Menu data. Please restart app!";
                    break;
                case WHAT_VALUE_QUERYCONFIGSMAP:
                    msg = "Failed to load ConfigsMap. Please restart app!";
//                    mainActivity.popRestartDialog("cannot load confirm code, please try to restart this app");
                    break;
                case WHAT_VALUE_QUERYDESK :
                    msg = "Failed to load Desk data. Please restart app!";
                    break;
                case WHAT_VALUE_QUERYFLAVOR:
                    msg = "Failed to load Flavor data. Please restart app!";
            }
            CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "WRONG", msg);
        }

        @Override
        public void onFinish(int what) {
        }
    };

    private RequestQueue requestQueue = NoHttp.newRequestQueue();

    public HttpOperator(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    private void doResponseQueryMenu(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryMenu: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseQueryMenu: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseQueryMenu: " + response.getException().getMessage());
            return;
        }
        HttpResult<ArrayList<Category1>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<Category1>>>(){}.getType());
        if (result.success){
            ArrayList<Category1> c1s = result.data;
            sortAllMenu(c1s);
            mainActivity.setMenu(c1s);
            mainActivity.persistMenu();
            loadDishPictureFromServer();
        }else {
            Log.e(logTag, "doResponseQueryMenu: get FALSE for query confirm code");
            MainActivity.LOG.error("doResponseQueryMenu: get FALSE for query confirm code");
        }
    }

    private void doResponseQueryWaiter(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryWaiter: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseQueryWaiter: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseQueryWaiter: " + response.getException().getMessage());
            return;
        }
        HttpResult<ArrayList<UserData>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<UserData>>>(){}.getType());
        if (result.success){
            mainActivity.setWaiters(result.data);
        } else {
            Log.e(logTag, "doResponseQueryWaiter: get FALSE ");
            MainActivity.LOG.error("doResponseQueryWaiter: get FALSE ");
        }
    }

    /**
     * app启动时, 要加载config, 尤其要保证能加载到confirmcode. 如果没有confirmcode, 系统依然可以运行, 但是用户点菜后将无法下单, 必须重新restart,
     * 然后要求客户重新点菜, 此举会导致用户的不愉快, 所以这里要保证confirmcode取到再成功运行, 否则提示重启;
     */
    private void doResponseQueryConfigsMap(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryConfigsMap: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseQueryConfigsMap: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseQueryConfigsMap: " + response.getException().getMessage());
//            mainActivity.popRestartDialog("cannot load confirm code, please try to restart this app");
            return;
        }
        HttpResult<HashMap<String, String>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<HashMap<String, String>>>(){}.getType());
        if (result.success){
            mainActivity.setConfigsMap(result.data);
        } else {
            Log.e(logTag, "doResponseQueryConfigsMap: get FALSE for query confirm code");
            MainActivity.LOG.error("doResponseQueryConfigsMap: get FALSE for query confirm code");
//            mainActivity.popRestartDialog("cannot load confirm code, please try to restart this app");
        }
    }

    private void doResponseQueryFlavor(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryFlavor: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseQueryFlavor: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseQueryFlavor: " + response.getException().getMessage());
            return;
        }
        HttpResult<ArrayList<Flavor>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<Flavor>>>(){}.getType());
        if (result.success){
            ArrayList<Flavor> flavors = result.data;
            mainActivity.setFlavors(result.data);
            mainActivity.persistFlavor();
        } else {
            Log.e(logTag, "doResponseQueryFlavor: get FALSE for query flavor");
            MainActivity.LOG.error("doResponseQueryFlavor: get FALSE for query flavor");
            CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "WRONG", "Failed to load flavor data. Please restart app!");
        }
    }

    private void doResponseQueryDesk(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryDesk: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseQueryDesk: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseQueryDesk: " + response.getException().getMessage());
            return;
        }
        HttpResult<ArrayList<Desk>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<Desk>>>(){}.getType());
        if (result.success){
            ArrayList<Desk> desks = result.data;
            Collections.sort(desks, new Comparator<Desk>() {
                @Override
                public int compare(Desk o1, Desk o2) {
                    return o1.getId() - o2.getId();
                }
            });
            mainActivity.setDesk(result.data);
            mainActivity.persistDesk();
            mainActivity.getPostOrderDialog().initDeskData(result.data);
        } else {
            Log.e(logTag, "doResponseQueryDesk: get FALSE for query desk");
            MainActivity.LOG.error("doResponseQueryDesk: get FALSE for query desk");
            CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "WRONG", "Failed to load Desk data. Please restart app!");
        }
    }

    private void doResponseQueryMenuVersion(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryMenuVersion: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseQueryMenuVersion: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseQueryMenuVersion: " + response.getException().getMessage());
            return;
        }
        HttpResult<Integer> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<Integer>>(){}.getType());
        if (result.success){
            mainActivity.getDbOperator().saveObjectByCascade(new MenuVersion(1, result.data));
        } else {
            Log.e(logTag, "doResponseQueryMenuVersion: get FALSE for query menu version");
            MainActivity.LOG.error("doResponseQueryMenuVersion: get FALSE for query menu version");
            CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "WRONG", "Failed to load Menu version data. Please redo synchronization action!");
        }
    }

    //load desk
    public void loadDeskData(){
        mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA,
                "start loading table data ..."));
        Request<JSONObject> deskRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/getdesks");
        requestQueue.add(WHAT_VALUE_QUERYDESK, deskRequest, responseListener);
    }

    //load flavor
    public void loadFlavorData(){
        mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA,
                "start loading flavor data ..."));
        Request<JSONObject> deskRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/queryflavor");
        requestQueue.add(WHAT_VALUE_QUERYFLAVOR, deskRequest, responseListener);
    }

    //load waiters
    public void loadWaiterData(){
        mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA,
                "start loading waiter data ..."));
        Request<JSONObject> deskRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/account/queryaccount");
        requestQueue.add(WHAT_VALUE_QUERYWAITER, deskRequest, responseListener);
    }

    //load menu
    public void loadMenuData(){
        mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA,
                "start loading menu data ..."));
        Request<JSONObject> menuRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/querymenu");
        requestQueue.add(WHAT_VALUE_QUERYMENU, menuRequest, responseListener);
    }

    //load menu version
    public void loadMenuVersionData(){
        Request<JSONObject> mvRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/getlastmenuversion", RequestMethod.POST);
        requestQueue.add(WHAT_VALUE_QUERYMENUVERSION, mvRequest, responseListener);
    }

    public void queryConfigsMap(){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/queryconfigmap", RequestMethod.GET);
        requestQueue.add(WHAT_VALUE_QUERYCONFIGSMAP, request, responseListener);
    }

    /**
     * check the desk if available for making order.
     * @param deskName
     * @return "AVAILABLE": order is available; "OCCUPIED": there is an order already on this desk; other result for exception;
     */
    public String checkDeskStatus(String deskName){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/indent/queryindent", RequestMethod.POST);
        request.add("deskname", deskName);
        request.add("status", "Unpaid");
        Response<JSONObject> response = NoHttp.startRequestSync(request);
        if (response.getException() != null){
            return response.getException().getMessage();
        }
        if (response.get() == null) {
            Log.e(logTag, "Error occur while check desk available for making order. response.get() is null.");
            return "Error occur while check desk available for making order. response.get() is null";
        }
        HttpResult<ArrayList<Indent>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<Indent>>>(){}.getType());
        if (result.data == null || result.data.isEmpty()){
            return InstantValue.CHECKDESK4MAKEORDER_AVAILABLE;
        } else {
            return InstantValue.CHECKDESK4MAKEORDER_OCCUPIED;
        }
    }
    //sort by sequence
    private void sortAllMenu(ArrayList<Category1> c1s){
        if (c1s != null){
            Collections.sort(c1s, new Comparator<Category1>() {
                @Override
                public int compare(Category1 o1, Category1 o2) {
                    return o1.getSequence() - o2.getSequence();
                }
            });
            for (Category1 c1 : c1s) {
                if (c1.getCategory2s() != null){
                    Collections.sort(c1.getCategory2s(), new Comparator<Category2>() {
                        @Override
                        public int compare(Category2 o1, Category2 o2) {
                            return o1.getSequence() - o2.getSequence();
                        }
                    });
                    for (Category2 c2 : c1.getCategory2s()) {
                        if(c2.getDishes() != null){
                            Collections.sort(c2.getDishes(), new Comparator<Dish>() {
                                @Override
                                public int compare(Dish o1, Dish o2) {
                                    return o1.getSequence() - o2.getSequence();
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private void onFailedLoadMenu(){
        //TODO: require restart app
    }

    /**
     * first check the CONFIRM CODE, if it is right, make order
     * @param orders
     * @param deskid
     */
    public HttpResult<Integer> makeOrder(String code, String orders, int deskid, int customerAmount, String comments){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/indent/makeindent", RequestMethod.POST);
        request.add("confirmCode", code);
        request.add("indents", orders);
        request.add("deskid", deskid);
        request.add("customerAmount", customerAmount);
        request.add("comments", comments);
        Response<JSONObject> response = NoHttp.startRequestSync(request);

        if (response.getException() != null){
            HttpResult<Integer> result = new HttpResult<>();
            result.result = response.getException().getMessage();
            return result;
        }
        if (response.get() == null) {
            Log.e(logTag, "Error occur while make order. response.get() is null.");
            MainActivity.LOG.error("Error occur while make order. response.get() is null.");
            HttpResult<Integer> result = new HttpResult<>();
            result.result = "Error occur while make order. response.get() is null";
            return result;
        }
        HttpResult<Integer> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<Integer>>(){}.getType());
        return result;
    }

    public HttpResult<Integer> addDishToOrder(int deskid, String orders){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/indent/adddishtoindent", RequestMethod.POST);
        request.add("indents", orders);
        request.add("deskid", deskid);

        Response<JSONObject> response = NoHttp.startRequestSync(request);
        if (response.getException() != null){
            HttpResult<Integer> result = new HttpResult<>();
            result.result = response.getException().getMessage();
            return result;
        }
        if (response.get() == null) {
            Log.e(logTag, "Error occur while add dish to order. response.get() is null.");
            MainActivity.LOG.error("Error occur while add dish to order. response.get() is null.");
            HttpResult<Integer> result = new HttpResult<>();
            result.result = "Error occur while add dish to order. response.get() is null";
            return result;

        }
        HttpResult<Integer> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<Integer>>(){}.getType());
        return result;
    }

    /**
     * check the menu version difference between client and server
     * 目前的同步数据包括, dish的基本属性修改, 含soldout/promotion; dish的增加; dish的删除; DishConfig的soldout
     * 实现逻辑:
     * 1. DishConfig分为一组, 同步服务端数据, 比较soldout的值, 然后更新本地数据库中对应的DishConfig对象
     * 2. dish分为 '增加', '删除', '修改' 三个组;
     *    同步各组中的数据;
     *    在MainActivity中, 对不同组的dish进行相应的操作.
     *    这里有逻辑缺陷, 如果一个dish先做了修改, 又做了删除, 在同步时, '修改'组中存在该dishid, 但是服务端是无法返回该dish的数据.
     *    同理, 其他的操作也有可能导致不一致性. 所以我们这里只简化考虑, 使用非空判断避免空指针异常, 不进行复杂的逻辑检查.
     * @param localVersion
     * @return if no menu change, return null; otherwise return a map including changed dishes and dishconfigs
     */
    public HashMap<String, ArrayList<Integer>> checkMenuVersion(int localVersion){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/checkmenuversion", RequestMethod.POST);
        request.add("versionId", localVersion);
        Response<JSONObject> response = NoHttp.startRequestSync(request);
        if (response.getException() != null){
            Log.e(logTag, "chechMenuVersion: There are Exception to checkmenuversion\n"+ response.getException().getMessage() );//TODO:
            MainActivity.LOG.error("chechMenuVersion: There are Exception to checkmenuversion\n"+ response.getException().getMessage() );
            sendErrorMessageToToast("Http:chechMenuVersion: " + response.getException().getMessage());
            return null;
        }
        HttpResult<ArrayList<MenuVersionInfo>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<MenuVersionInfo>>>(){}.getType());
        if (result.success){
            if (result.data == null)
                return null;
            DBOperator dbOpr = mainActivity.getDbOperator();
            //collect all change into a set to avoid the duplicate dishid
            Set<Integer> dishIdSet_update = new HashSet<>();
            Set<Integer> dishIdSet_insert = new HashSet<>();
            Set<Integer> dishIdSet_delete = new HashSet<>();
            Set<Integer> dishIdSet_changePicture = new HashSet<>();
            Set<Integer> dishConfigIdSet = new HashSet<>();
            int maxVersion = 0;//get the biggest version number in this update
            for (int i = 0; i < result.data.size(); i++) {
                MenuVersionInfo mvi = result.data.get(i);
                if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHCONFIGSOLDOUT){
                    dishConfigIdSet.add(mvi.objectId);
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHSOLDOUT
                        || mvi.type == InstantValue.MENUCHANGE_TYPE_DISHUPDATE
                        || mvi.type == InstantValue.MENUCHANGE_TYPE_CHANGEPROMOTION
                        || mvi.type == InstantValue.MENUCHANGE_TYPE_DISHPICTURE) {
                    dishIdSet_update.add(mvi.objectId);
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHADD){
                    dishIdSet_insert.add(mvi.objectId);
                } else if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHDELETE){
                    dishIdSet_delete.add(mvi.objectId);
                }
                if (mvi.type == InstantValue.MENUCHANGE_TYPE_DISHPICTURE){
                    dishIdSet_changePicture.add(mvi.objectId);
                }
                if (mvi.id > maxVersion)
                    maxVersion = mvi.id;
            }
            //reload info about dishes in dishIdSet
            ArrayList<Integer> dishIdListUpdate = new ArrayList<>(dishIdSet_update);
            ArrayList<Integer> dishIdListDelete = new ArrayList<>(dishIdSet_delete);
            ArrayList<Integer> dishIdListInsert = new ArrayList<>(dishIdSet_insert);
            ArrayList<Integer> dishConfigIdList = new ArrayList<>(dishConfigIdSet);
            ArrayList<Integer> dishIdListPicture = new ArrayList<>(dishIdSet_changePicture);
            boolean bSyncDishesUpdated = synchronizeDishesUpdate(dishIdListUpdate);
            boolean bSyncDishesInsert = synchronizeDishesInsert(dishIdListInsert);
            boolean bSyncDishesDelete = synchronizeDishesDelete(dishIdListDelete);
            boolean bSyncDishesPicture = synchronizeDishesPicture(dishIdListPicture);//图片替换要在最后处理, 因为dish上记录的picturename有可能会变掉
            boolean bSyncDishConfigs = synchronizeDishConfig(dishConfigIdList);
            //only persist the maxVersion while all the sync functions done successfully
            if (bSyncDishesUpdated & bSyncDishesInsert & bSyncDishesDelete & bSyncDishesPicture & bSyncDishConfigs){
                dbOpr.deleteAllData(MenuVersion.class);
                MenuVersion mv = new MenuVersion(1, maxVersion);
                dbOpr.saveObjectByCascade(mv);
                HashMap<String, ArrayList<Integer>> map = new HashMap<>();
                map.put(DISHCHANGE_INSERT, dishIdListInsert);
                map.put(DISHCHANGE_UPDATE, dishIdListUpdate);
                map.put(DISHCHANGE_DELETE, dishIdListDelete);
                map.put(DISHCHANGE_PICTURE, dishIdListPicture);
                map.put(DISHCONFIGCHANGE, dishConfigIdList);
                return map;
            }
        } else {
            Log.e(logTag, "get false from server while Check Menu Version");
            MainActivity.LOG.error("get false from server while Check Menu Version");
            sendErrorMessageToToast("get false from server while Check Menu Version");
        }
        return null;
    }

    /**
     * this function is used for those dishes which properties changed, including change promption,
     * change price, change name, change soldout, change sequence, etc
     * load dishes data from server by the id list; then cover the local db data using the server data
     * @param dishIdList
     * @return false while exception occur.
     */
    private boolean synchronizeDishesUpdate(ArrayList<Integer> dishIdList){
        if (dishIdList.isEmpty())
            return true;
        ArrayList<Dish> dishes = queryDishById(dishIdList);
        if (dishes == null){
            sendErrorMessageToToast("find unrecognized dishes by id in  "+ dishIdList +", please refresh data on this device.");
            return false;
        }
        DBOperator dbOpr = mainActivity.getDbOperator();
        for (int i = 0; i < dishes.size(); i++) {
            Dish dish = dishes.get(i);
            Dish dbDish = dbOpr.queryDishById(dish.getId());
            if (dbDish == null){
                sendErrorMessageToToast("find unrecognized dish '"+dish.getFirstLanguageName()+"', please refresh data on this device.");
                return false;
            }
            dbDish.copyFrom(dish);
            dbOpr.updateObject(dbDish);
        }
        return true;
    }

    /**
     * 对于新增的dish, 同步到本地时需要查询全部的menu树. 因为服务端的dish对parent设置了gsonIgnore, 所以无法得到
     * 父对象数据. 只能拿全部的树结构向下查找对应id的dish
     * @param dishIdList
     * @return 如果dishIdList == null , 返回true; 否则, 在成功保存dish数据后, 返回true
     */
    private boolean synchronizeDishesInsert(ArrayList<Integer> dishIdList){
        if (dishIdList.isEmpty())
            return true;

        Request<JSONObject> reqMenu = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/querymenu", RequestMethod.GET);
        Response<JSONObject> respDish = NoHttp.startRequestSync(reqMenu);
        if (respDish.getException() != null){
            Log.e(logTag, "get Exception while call menu/querymenu for refresh added dish by id in "+ dishIdList+", Exception is "+ respDish.getException());
            MainActivity.LOG.error("get Exception while call menu/querymenu for refresh added dish by id in "+ dishIdList+", Exception is "+ respDish.getException());
            sendErrorMessageToToast("get Exception while call menu/querymenu for refresh added dish by id in "+ dishIdList+", Exception is "+ respDish.getException());
            return false;
        }
        HttpResult<ArrayList<Category1>> result = gson.fromJson(respDish.get().toString(), new TypeToken<HttpResult<ArrayList<Category1>>>(){}.getType());
        if (!result.success){
            Log.e(logTag, "get Exception while call menu/querymenu for refresh added dish by id in "+ dishIdList+", Exception is "+ respDish.getException());
            MainActivity.LOG.error("get Exception while call menu/querymenu for refresh added dish by id in "+ dishIdList+", Exception is "+ respDish.getException());
            sendErrorMessageToToast("get Exception while call menu/querymenu for refresh added dish by id in "+ dishIdList+", Exception is "+ respDish.getException());
            return false;
        }
        DBOperator dbOpr = mainActivity.getDbOperator();
        ArrayList<Category1> category1s = result.data;
        for (int id : dishIdList) {
            boolean finddish = false;
            for ( int i = 0; i < category1s.size() && !finddish; i++) {
                ArrayList<Category2> c2s = category1s.get(i).getCategory2s();
                if (c2s != null){
                    for (int j = 0; j < c2s.size() && !finddish; j++) {
                        ArrayList<Dish> dishes = c2s.get(j).getDishes();
                        if (dishes != null){
                            for (Dish dish : dishes){
                                if (dish.getId()==id){
                                    //此时要把dish对象跟mainActivity中持有的menu树绑定, 而不是绑定当前的category2对象, 否则DB在操作时,会导致数据库中的category2与category1解除关系
                                    finddish = true;
                                    Category2 c2 = findCategory2(mainActivity.getMenu(), c2s.get(j).getId());
                                    dish.setCategory2(c2);
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
     * 对于删除的dish, 从本地删除
     * @param dishIdList
     * @return 如果dishIDList== null, 返回true, 否则, 在成功删除本地dish后, 返回true
     */
    private boolean synchronizeDishesDelete(ArrayList<Integer> dishIdList){
        if (dishIdList.isEmpty())
            return true;
        DBOperator dbOpr = mainActivity.getDbOperator();
        for (int i = 0; i < dishIdList.size(); i++) {
            Dish dbDish = dbOpr.queryDishById(dishIdList.get(i));
            if (dbDish != null){
                dbDish.setConfigGroups(null);
                dbOpr.deleteObject(dbDish);
            }
        }
        return true;
    }

    /**
     * 下载图片有改动的dish, 此方法为异步下载, 所以前端无法做到同步刷新, 只有在下次启动APP的时候会体现新图片
     * @param dishIdList
     * @return
     */
    private boolean synchronizeDishesPicture(ArrayList<Integer> dishIdList){
        if (dishIdList == null || dishIdList.isEmpty())
            return true;
        DownloadListener listener = new DownloadListener(){
            public void onDownloadError(int what, Exception exception) {}
            public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {}
            public void onProgress(int what, int progress, long fileCount, long speed) {}
            public void onFinish(int what, String filePath) {}
            public void onCancel(int what) {}
        };
        DownloadQueue queue = NoHttp.newDownloadQueue();
        DBOperator dbOpr = mainActivity.getDbOperator();
        String temps1 = "/../";
        String temps2 = "/";
        for (int i = 0; i < dishIdList.size(); i++) {
            Dish dbDish = dbOpr.queryDishById(dishIdList.get(i));
            if (dbDish != null){
                String filename = dbDish.getPictureName();
                String urlbig = InstantValue.URL_TOMCAT + temps1 + InstantValue.SERVER_CATALOG_DISH_PICTURE_BIG+ temps2 + filename;
                DownloadRequest requestbig = NoHttp.createDownloadRequest(urlbig, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG, filename, true, true);
                queue.add(0, requestbig, listener);

                String urlsmall = InstantValue.URL_TOMCAT + temps1 + InstantValue.SERVER_CATALOG_DISH_PICTURE_SMALL+ temps2 + filename;
                DownloadRequest requestsmall = NoHttp.createDownloadRequest(urlsmall, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL, filename, true, true);
                queue.add(0, requestsmall, listener);

                String urlorigin = InstantValue.URL_TOMCAT + temps1 + InstantValue.SERVER_CATALOG_DISH_PICTURE_ORIGIN + temps2 + filename;
                DownloadRequest requestorigin = NoHttp.createDownloadRequest(urlorigin, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_ORIGIN, filename, true, true);
                queue.add(0, requestorigin, listener);
            }
        }
        return true;
    }

    private ArrayList<Dish> queryDishById(ArrayList<Integer> dishIdList){
        ArrayList<Dish> dishes = null;
        if (dishIdList == null || dishIdList.isEmpty()){
            return dishes;
        }
        String sIds = dishIdList.toString().replace("[","").replace("]","").replace(" ","");
        Request<JSONObject> reqDish = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/querydishbyidlist", RequestMethod.POST);
        reqDish.add("dishIdList", sIds);
        Response<JSONObject> respDish = NoHttp.startRequestSync(reqDish);
        if (respDish.getException() != null){
            Log.e(logTag, "get Exception while call menu/querydishbyidlist for dishidlist = "+ dishIdList+", Exception is "+ respDish.getException());
            MainActivity.LOG.error("get Exception while call menu/querydishbyidlist for dishidlist = "+ dishIdList+", Exception is "+ respDish.getException());
            sendErrorMessageToToast("get Exception while call menu/querydishbyidlist for dishidlist = "+ dishIdList+", Exception is "+ respDish.getException());
            return null;
        }
        HttpResult<ArrayList<Dish>> result = gson.fromJson(respDish.get().toString(), new TypeToken<HttpResult<ArrayList<Dish>>>(){}.getType());
        if (!result.success){
            Log.e(logTag, "get false value while call menu/querydishbyidlist for dishidlist = "+ dishIdList+", Exception is "+ respDish.getException());
            MainActivity.LOG.error("get false value while call menu/querydishbyidlist for dishidlist = "+ dishIdList+", Exception is "+ respDish.getException());
            sendErrorMessageToToast("get false value while call menu/querydishbyidlist for dishidlist = "+ dishIdList+", Exception is "+ respDish.getException());
            return null;
        }
        return result.data;
    }



    /**
     * load dishes data from server by the id list;
     * compare the SOLDOUT and PROMOTION value with the local data, if different, modify local data
     * @param dishConfigIdList
     * @return false while exception occur.
     */
    private boolean synchronizeDishConfig(ArrayList<Integer> dishConfigIdList){
        if (dishConfigIdList.isEmpty())
            return true;
        String sIds = dishConfigIdList.toString().replace("[","").replace("]","").replace(" ","");
        Request<JSONObject> reqDish = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/querydishconfigbyidlist", RequestMethod.POST);
        reqDish.add("dishConfigIdList", sIds);
        Response<JSONObject> respDish = NoHttp.startRequestSync(reqDish);
        if (respDish.getException() != null){
            Log.e(logTag, "get Exception while call menu/querydishconfigbyidlist for dishConfigIdList = "+ dishConfigIdList+", Exception is "+ respDish.getException());
            MainActivity.LOG.error("get Exception while call menu/querydishconfigbyidlist for dishConfigIdList = "+ dishConfigIdList+", Exception is "+ respDish.getException());
            sendErrorMessageToToast("get Exception while call menu/querydishconfigbyidlist for dishConfigIdList = "+ dishConfigIdList+", Exception is "+ respDish.getException());
            return false;
        }
        HttpResult<ArrayList<DishConfig>> result = gson.fromJson(respDish.get().toString(), new TypeToken<HttpResult<ArrayList<DishConfig>>>(){}.getType());
        if (!result.success){
            Log.e(logTag, "get false value while call menu/querydishconfigbyidlist for dishConfigIdList = "+ dishConfigIdList+", Exception is "+ respDish.getException());
            MainActivity.LOG.error("get false value while call menu/querydishconfigbyidlist for dishConfigIdList = "+ dishConfigIdList+", Exception is "+ respDish.getException());
            sendErrorMessageToToast("get false value while call menu/querydishconfigbyidlist for dishConfigIdList = "+ dishConfigIdList+", Exception is "+ respDish.getException());
            return false;
        }
        ArrayList<DishConfig> dishConfigs = result.data;
        DBOperator dbOpr = mainActivity.getDbOperator();
        for (int i = 0; i < dishConfigs.size(); i++) {
            DishConfig dishConfig = dishConfigs.get(i);
            DishConfig dbDishConfig = (DishConfig) dbOpr.queryObjectById(dishConfig.getId(), DishConfig.class);
            if (dbDishConfig == null){
                sendErrorMessageToToast("find unrecognized dishConfig '"+dishConfig.getFirstLanguageName()+"', please refresh data on this device.");
                return false;
            }
            if (dishConfig.isSoldOut() != dbDishConfig.isSoldOut()) {
                dbDishConfig.setSoldOut(dishConfig.isSoldOut());
                dbOpr.updateObject(dbDishConfig);
            }
        }
        return true;
    }

    private void sendErrorMessageToToast(String sMsg){
        mainActivity.getToastHandler().sendMessage(CommonTool.buildMessage(MainActivity.TOASTHANDLERWHAT_ERRORMESSAGE,sMsg));
    }

    public void uploadErrorLog(File file, String machineCode){
        int key = 0;// the key of filelist;
        UploadErrorLogListener listener = new UploadErrorLogListener(mainActivity);
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/uploaderrorlog", RequestMethod.POST);
        FileBinary bin1 = new FileBinary(file);
        request.add("logfile", bin1);
        request.add("machineCode", machineCode);
        listener.addFiletoList(key, file.getAbsolutePath());
        requestQueue.add(key, request, listener);
    }

    private void loadDishPictureFromServer(ArrayList<String> filenames){
        DownloadDishImageListener listener = new DownloadDishImageListener(mainActivity);
        DownloadQueue queue = NoHttp.newDownloadQueue();
        int key = 0;// the key of filelist;
        String temps1 = "/../";
        String temps2 = "/";
        for (int i = 0; i < filenames.size(); i++) {
            String filename = filenames.get(i);
            key++;
            listener.addFiletoList(key, InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG + filename);
            String urlbig = InstantValue.URL_TOMCAT + temps1 + InstantValue.SERVER_CATALOG_DISH_PICTURE_BIG+ temps2 + filename;
            DownloadRequest requestbig = NoHttp.createDownloadRequest(urlbig, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG, filename, true, true);
            queue.add(key, requestbig, listener);

            key++;
            listener.addFiletoList(key, InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL + filename);
            String urlsmall = InstantValue.URL_TOMCAT + temps1 + InstantValue.SERVER_CATALOG_DISH_PICTURE_SMALL+ temps2 + filename;
            DownloadRequest requestsmall = NoHttp.createDownloadRequest(urlsmall, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL, filename, true, true);
            queue.add(key, requestsmall, listener);

            key++;
            listener.addFiletoList(key, InstantValue.LOCAL_CATALOG_DISH_PICTURE_ORIGIN + filename);
            String urlorigin = InstantValue.URL_TOMCAT + temps1 + InstantValue.SERVER_CATALOG_DISH_PICTURE_ORIGIN + temps2 + filename;
            DownloadRequest requestorigin = NoHttp.createDownloadRequest(urlorigin, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_ORIGIN, filename, true, true);
            queue.add(key, requestorigin, listener);
        }
        listener.setTotalFileAmount(key);
        if (key == 0){
            //如果所有的dish都没有图片, 给handle一个message来停止等待框
            mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH, "start to rebuild menu"));
            mainActivity.popRestartDialog("Data refresh finish successfully. Please restart the app.");
        }
    }
    private void loadDishPictureFromServer(){
        ArrayList<String> filenames = new ArrayList<>();
        for (Category1 c1: mainActivity.getMenu()) {
            for(Category2 c2 : c1.getCategory2s()){
                for(Dish dish : c2.getDishes()){
                    String filename = dish.getPictureName();
                    if (filename != null){
                        filenames.add(filename);
                    }
                }
            }
        }
        loadDishPictureFromServer(filenames);
    }

    /**
     * 下载logo文件, 该方法只管下载到本地, 不做额外操作, 所以使用DownloadListener即可, 所有需要实现的方法都是空方法
     */
    public void loadLogoPictureFromServer(){
        DownloadListener listener = new DownloadListener(){
            public void onDownloadError(int what, Exception exception) {}
            public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {}
            public void onProgress(int what, int progress, long fileCount, long speed) {}
            public void onFinish(int what, String filePath) {}
            public void onCancel(int what) {}
        };
        DownloadQueue queue = NoHttp.newDownloadQueue();

        String filename = "rightup.jpg";
        DownloadRequest request = NoHttp.createDownloadRequest(InstantValue.URL_TOMCAT + "/../logofile/" + filename, RequestMethod.GET, InstantValue.LOGO_PATH, filename, true, true);
        queue.add(0, request, listener);

        filename = "rightbottom.jpg";
        request = NoHttp.createDownloadRequest(InstantValue.URL_TOMCAT + "/../logofile/" + filename, RequestMethod.GET, InstantValue.LOGO_PATH, filename, true, true);
        queue.add(0, request, listener);

        filename = "chooseddishbg.jpg";
        request = NoHttp.createDownloadRequest(InstantValue.URL_TOMCAT + "/../logofile/" + filename, RequestMethod.GET, InstantValue.LOGO_PATH, filename, true, true);
        queue.add(0, request, listener);
    }
}
