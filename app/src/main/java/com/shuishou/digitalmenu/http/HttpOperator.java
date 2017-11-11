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
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.bean.HttpResult;
import com.shuishou.digitalmenu.bean.Indent;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.bean.MenuVersionInfo;
import com.shuishou.digitalmenu.db.DBOperator;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.shuishou.digitalmenu.utils.CommonTool;
import com.yanzhenjie.nohttp.FileBinary;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
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


    private MainActivity mainActivity;
//    private ArrayList<String> listDishPictures = new ArrayList<>();
    private static final int WHAT_VALUE_QUERYMENU = 1;
    private static final int WHAT_VALUE_QUERYFLAVOR = 2;
    private static final int WHAT_VALUE_QUERYDESK = 4;
    private static final int WHAT_VALUE_QUERYMENUVERSION = 5;
    private static final int WHAT_VALUE_QUERYCONFIGSMAP = 6;

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

    private void doResponseQueryConfigsMap(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryConfigsMap: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseQueryConfigsMap: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseQueryConfigsMap: " + response.getException().getMessage());
            return;
        }
        HttpResult<HashMap<String, String>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<HashMap<String, String>>>(){}.getType());
        if (result.success){
            mainActivity.setConfigsMap(result.data);
        } else {
            Log.e(logTag, "doResponseQueryConfigsMap: get FALSE for query confirm code");
            MainActivity.LOG.error("doResponseQueryConfigsMap: get FALSE for query confirm code");
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
     * @param localVersion
     * @return if same return null, otherwise return the List of Changed DishId
     */
    public ArrayList<Integer> chechMenuVersion(int localVersion){
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
            //collect all change into a set to remove the duplicate dishid
            Set<Integer> dishIdSet = new HashSet<>();
            int newVersion = 0;
            for (int i = 0; i < result.data.size(); i++) {
                dishIdSet.add(result.data.get(i).dishId);
                if (result.data.get(i).id > newVersion)
                    newVersion = result.data.get(i).id;
            }
            //reload info about dishes in dishIdSet
            ArrayList<Integer> dishIdList = new ArrayList<Integer>();
            dishIdList.addAll(dishIdSet);
            for (int i = 0; i < dishIdList.size(); i++) {
                Request<JSONObject> reqDish = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/querydishbyid", RequestMethod.POST);
                reqDish.add("dishId", dishIdList.get(i));
                Response<JSONObject> respDish = NoHttp.startRequestSync(reqDish);
                if (respDish.getException() != null){
                    Log.e(logTag, "get Exception while call menu/querydishbyid for dishid = "+ dishIdList.get(i)+", Exception is "+ respDish.getException());
                    MainActivity.LOG.error("get Exception while call menu/querydishbyid for dishid = "+ dishIdList.get(i)+", Exception is "+ respDish.getException());
                    sendErrorMessageToToast("get Exception while call menu/querydishbyid for dishid = "+ dishIdList.get(i)+", Exception is "+ respDish.getException());
                }
                HttpResult<ArrayList<Dish>> resultDish = gson.fromJson(respDish.get().toString(), new TypeToken<HttpResult<ArrayList<Dish>>>(){}.getType());
                if (resultDish.success){
                    //TODO: only do SOLDOUT property at first stage
                    Dish dish = resultDish.data.get(0);
                    Dish dbDish = dbOpr.queryDishById(dish.getId());
                    dbDish.setSoldOut(dish.isSoldOut());
                    dbOpr.updateObject(dbDish);
                }
            }
            //update menu version.
            dbOpr.deleteAllData(MenuVersion.class);
            MenuVersion mv = new MenuVersion(1, newVersion);
            dbOpr.saveObjectByCascade(mv);

            return dishIdList;
        } else {
            Log.e(logTag, "get false from server while Check Menu Version");
            MainActivity.LOG.error("get false from server while Check Menu Version");
            sendErrorMessageToToast("get false from server while Check Menu Version");
        }
        return null;
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
//        for(File file : files){
//            Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/uploaderrorlog", RequestMethod.POST);
//            FileBinary bin1 = new FileBinary(file);
//            request.add("logfile", bin1);
//            request.add("machineCode", machineCode);
//            key++;
//            listener.addFiletoList(key, file.getAbsolutePath());
//            requestQueue.add(key, request, listener);
//        }
//        listener.setTotalFileAmount(key);
    }

    private void loadDishPictureFromServer(){
        DownloadDishImageListener listener = new DownloadDishImageListener(mainActivity);
        DownloadQueue queue = NoHttp.newDownloadQueue();
        int key = 0;// the key of filelist;
        String temps1 = "/../";
        String temps2 = "/";
        for (Category1 c1: mainActivity.getMenu()) {
            for(Category2 c2 : c1.getCategory2s()){
                for(Dish dish : c2.getDishes()){
                    String filename = dish.getPictureName();
                    if (filename != null){
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
                }
            }
        }
        listener.setTotalFileAmount(key);
    }


}
