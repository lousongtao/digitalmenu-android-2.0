package com.shuishou.digitalmenu.http;

import android.app.ProgressDialog;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
import com.shuishou.digitalmenu.bean.HttpResult;
import com.shuishou.digitalmenu.bean.Indent;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.bean.MenuVersionInfo;
import com.shuishou.digitalmenu.db.DBOperator;
import com.shuishou.digitalmenu.ui.MainActivity;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.download.DownloadListener;
import com.yanzhenjie.nohttp.download.DownloadQueue;
import com.yanzhenjie.nohttp.download.DownloadRequest;
import com.yanzhenjie.nohttp.error.TimeoutError;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Administrator on 2017/6/9.
 */

public class HttpOperator {

    private String logTag = "HttpOperation";
    //record the picture files need to be download, after all finish, then rebuild the UI
    private Hashtable<String, Boolean> flagFinishLoadDishPictures = new Hashtable<String, Boolean>();

    private MainActivity mainActivity;
    private ArrayList<String> listDishPictures = new ArrayList<>();
    private static final int WHAT_VALUE_QUERYMENU = 1;
    private static final int WHAT_VALUE_CONFIRMCODE = 2;
//    private static final int WHAT_VALUE_MAKEORDER = 3;
    private static final int WHAT_VALUE_QUERYDESK = 4;
    private static final int WHAT_VALUE_QUERYMENUVERSION = 5;
    private static final int WHAT_VALUE_DOWNLOADIMAGE = 10;

    private Gson gson = new Gson();
//    private StoreOrderData storeOrderData;
//    private class StoreOrderData{
//        String orders;
//        int deskid;
//        String confirmCode;
//        StoreOrderData(String _confirmCode, String _orders, int _deskid){
//            orders = _orders;
//            deskid = _deskid;
//            confirmCode = _confirmCode;
//        }
//    }

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
//                case WHAT_VALUE_CONFIRMCODE:
//                    doResponseConfirmCode4RefreshData(response);
//                    break;
//                case WHAT_VALUE_MAKEORDER :
//                    doResponseMakeOrder(response);
//                    break;
                case WHAT_VALUE_QUERYDESK :
                    doResponseQueryDesk(response);
                    break;
                case WHAT_VALUE_QUERYMENUVERSION:
                    doResponseQueryMenuVersion(response);
                    break;
                default:
            }
        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            Log.e("Http failed", "what = "+ what + "\nresponse = "+ response.get());
            String msg = "";
            switch (what){
                case WHAT_VALUE_QUERYMENU :
                    msg = "Failed to load Menu data. Please restart app!";
                    break;
//                case WHAT_VALUE_CONFIRMCODE:
//                    msg = "The input code is wrong.";
//                    break;
//                case WHAT_VALUE_MAKEORDER :
//                    msg = "Failed to make order. Please try again!";
//                    break;
                case WHAT_VALUE_QUERYDESK :
                    msg = "Failed to load Desk data. Please restart app!";
                    break;
            }
            new AlertDialog.Builder(mainActivity)
                    .setIcon(R.drawable.error)
                    .setTitle("WRONG")
                    .setMessage(msg)
                    .setNegativeButton("OK", null)
                    .create().show();
        }

        @Override
        public void onFinish(int what) {
        }
    };

    private DownloadListener downloadDishListener = new DownloadListener() {
        @Override
        public void onDownloadError(int what, Exception exception) {
            new AlertDialog.Builder(mainActivity)
                    .setIcon(R.drawable.error)
                    .setTitle("WRONG")
                    .setMessage("Failed to load dish image. Please restart app!")
                    .setNegativeButton("OK", null)
                    .create().show();
        }

        @Override
        public void onStart(int what, boolean isResume, long rangeSize, Headers responseHeaders, long allCount) {
        }

        @Override
        public void onProgress(int what, int progress, long fileCount, long speed) {

        }

        @Override
        public void onFinish(int what, String filePath) {
            flagFinishLoadDishPictures.put(filePath, true);
            if (!flagFinishLoadDishPictures.containsValue(false)){
                mainActivity.buildMenu();
            }
        }

        @Override
        public void onCancel(int what) {

        }
    };

    private RequestQueue requestQueue = NoHttp.newRequestQueue();

    public HttpOperator(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    private void doResponseQueryMenu(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryMenu: " + response.getException().getMessage() );
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
//            mainActivity.buildMenu();
        }
    }

    private void doResponseQueryDesk(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryDesk: " + response.getException().getMessage() );
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
            new AlertDialog.Builder(mainActivity)
                    .setIcon(R.drawable.error)
                    .setTitle("WRONG")
                    .setMessage("Failed to load Desk data. Please restart app!")
                    .setNegativeButton("OK", null)
                    .create().show();
        }
    }

    private void doResponseQueryMenuVersion(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseQueryMenuVersion: " + response.getException().getMessage() );
            sendErrorMessageToToast("Http:doResponseQueryMenuVersion: " + response.getException().getMessage());
            return;
        }
        HttpResult<Integer> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<Integer>>(){}.getType());
        if (result.success){
            mainActivity.getDbOperator().saveObjectByCascade(new MenuVersion(1, result.data));
        } else {
            new AlertDialog.Builder(mainActivity)
                    .setIcon(R.drawable.error)
                    .setTitle("WRONG")
                    .setMessage("Failed to load Menu version data. Please redo synchronization action!")
                    .setNegativeButton("OK", null)
                    .create().show();
        }
    }

    //load desk
    public void loadDeskData(){
        Request<JSONObject> deskRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/getdesks");
        requestQueue.add(WHAT_VALUE_QUERYDESK, deskRequest, responseListener);
    }

    //load desk
    public void loadMenuData(){
        Request<JSONObject> menuRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/querymenu");
        requestQueue.add(WHAT_VALUE_QUERYMENU, menuRequest, responseListener);
    }

    //load menu version
    public void loadMenuVersionData(){
        Request<JSONObject> mvRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/menu/getlastmenuversion", RequestMethod.POST);
        requestQueue.add(WHAT_VALUE_QUERYMENUVERSION, mvRequest, responseListener);
    }

    public String checkConfirmCodeSync(String code){
        Request<JSONObject> codeRequest = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/checkconfirmcode", RequestMethod.POST);
        codeRequest.add("code", code);
        Response<JSONObject> response = NoHttp.startRequestSync(codeRequest);
        if (response.getException() != null){
            return response.getException().getMessage();
        }
        if (response.get() == null) {
            Log.e(logTag, "Error occur while synchronize check confirm code. response.get() is null.");
            return "Error occur while synchronize check confirm code. response.get() is null";
        }
        HttpResult<Boolean> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<Boolean>>(){}.getType());
        if (result.data)
            return InstantValue.RESULT_SUCCESS;
        else
            return "Confirm code is wrong!";
    }

    /**
     * check the desk if available for making order.
     * @param deskName
     * @return "AVAILABLE": order is available; "OCCUPIED": there is an order already on this desk; other result for exception;
     */
    public String checkDeskStatus(String deskName){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/indent/queryindent", RequestMethod.GET);
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

    private void loadDishPictureFromServer(){
        flagFinishLoadDishPictures.clear();
        listDishPictures.clear();
        for (Category1 c1: mainActivity.getMenu()) {
            for(Category2 c2 : c1.getCategory2s()){
                for(Dish dish : c2.getDishes()){
                    listDishPictures.add(dish.getPictureName());

                }
            }
        }
        DownloadQueue queue = NoHttp.newDownloadQueue();
        for (String filename : listDishPictures) {
            flagFinishLoadDishPictures.put(InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG + filename, false);
            flagFinishLoadDishPictures.put(InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL + filename, false);
            String urlbig = InstantValue.URL_TOMCAT + "/../"+ InstantValue.SERVER_CATALOG_DISH_PICTURE_BIG+"/"+ filename;
            String urlsmall = InstantValue.URL_TOMCAT + "/../"+ InstantValue.SERVER_CATALOG_DISH_PICTURE_SMALL+"/"+ filename;
            DownloadRequest requestbig = NoHttp.createDownloadRequest(urlbig, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG, filename, true, true);
            DownloadRequest requestsmall = NoHttp.createDownloadRequest(urlsmall, RequestMethod.GET, InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL, filename, true, true);
            queue.add(WHAT_VALUE_DOWNLOADIMAGE, requestbig, downloadDishListener);
            queue.add(WHAT_VALUE_DOWNLOADIMAGE, requestsmall, downloadDishListener);

        }
    }

//    private void doResponseMakeOrder(Response<JSONObject> response){
//        if (response.getException() != null){
//            Log.e(logTag, "doResponseQueryMakeOrder: " + response.getException().getMessage() );
//            return;
//        }
//        HttpResult<Integer> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<Integer>>(){}.getType());
//        if (result.success){
//            mainActivity.onFinishMakeOrder(result.data);
//        } else {
//            mainActivity.popupWarnDialog(R.drawable.error, "WRONG", "Something wrong happened while making order! \n\nError message : " + result.result);
//        }
//    }


//    private void doResponseConfirmCode4RefreshData(Response<JSONObject> response){
//        JSONObject result = response.get();
//        try {
//            boolean b = Boolean.valueOf(result.get("success").toString());
//            if (b){
//                mainActivity.onRefreshData();
//            } else {
//                mainActivity.popupWarnDialog(R.drawable.error, "WRONG", "Confirmation Code is wrong!");
//            }
//        } catch (JSONException e) {
//            Log.e("HttpOperator", "JSON exception in checkConfirmCode");
//            e.printStackTrace();
//        }
//    }

    /**
     * first check the CONFIRM CODE, if it is right, make order
     * @param orders
     * @param deskid
     */
    public HttpResult<Integer> makeOrder(String code, String orders, int deskid, int customerAmount){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/indent/makeindent", RequestMethod.POST);
        request.add("confirmCode", code);
        request.add("indents", orders);
        request.add("deskid", deskid);
        request.add("customerAmount", customerAmount);
        Response<JSONObject> response = NoHttp.startRequestSync(request);

        if (response.getException() != null){
            HttpResult<Integer> result = new HttpResult<>();
            result.result = response.getException().getMessage();
            return result;
        }
        if (response.get() == null) {
            Log.e(logTag, "Error occur while make order. response.get() is null.");
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
            Log.e(logTag, "chechMenuVersion: There are Exception to checkmenuversion" );//TODO:
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
            sendErrorMessageToToast("get false from server while Check Menu Version");
        }
        return null;
    }

    private void sendErrorMessageToToast(String sMsg){
        Message message = new Message();
        message.what = MainActivity.TOASTHANDLERWHAT_ERRORMESSAGE;
        message.obj = sMsg;
        mainActivity.getToastHandler().sendMessage(message);
    }

}
