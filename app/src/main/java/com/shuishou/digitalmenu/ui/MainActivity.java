package com.shuishou.digitalmenu.ui;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Desk;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.db.DBOperator;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.io.CustomizedExceptionHandler;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/6/4.
 */

public class MainActivity extends AppCompatActivity{
    public final static byte LANGUAGE_ENGLISH = 1;
    public final static byte LANGUAGE_CHINESE = 2;

    private CategoryTabListView listViewCategorys;
    private RadioButton rbChinese;
    private RadioButton rbEnglish;
    private TextView tvChoosedItems;
    private TextView tvChoosedPrice;
    private TextView tvOrdersLabel;
    private FrameLayout displayFragmentsLayout;

    private ArrayList<Desk> desks;
    private RecyclerChoosedFoodAdapter choosedFoodAdapter;
    private ArrayList<ChoosedDish> choosedFoodList= new ArrayList<>();
    private ArrayList<Category1> category1s = new ArrayList<>(); // = TestData.makeCategory1();

    private HttpOperator httpOperator;
    private DBOperator dbOperator;

    private PostOrderDialog dlgPostOrder;

    private int refreshMenuPeroid = 60 * 1000;

    private Handler refreshMenuHandler;
    private Timer refreshMenuTimer;

    private String logTag = "TestTime-MainActivity";

    private SparseArray<DishDisplayFragment> mapDishDisplayFragments = new SparseArray<>();
    private SparseArray<DishCellComponent> mapFoodCellComponents = new SparseArray<>();

    private ProgressDialog progressDlg;
    private Handler progressDlgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (progressDlg != null)
                    progressDlg.dismiss();
            }
        }
    };
    public static final int TOASTHANDLERWHAT_ERRORMESSAGE = 0;
    private Handler toastHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TOASTHANDLERWHAT_ERRORMESSAGE){
                Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };

    public Handler getToastHandler(){
        return toastHandler;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Sets the default uncaught exception handler. This handler is invoked
        // in case any Thread dies due to an unhandled exception.
        Thread.setDefaultUncaughtExceptionHandler(new CustomizedExceptionHandler(InstantValue.LOCAL_CATALOG_ERRORLOG));
//        instance = this;
        RecyclerView lvChoosedFood = (RecyclerView) findViewById(R.id.list_choosedfood);
        choosedFoodAdapter = new RecyclerChoosedFoodAdapter(this, R.layout.choosedfood_item, choosedFoodList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lvChoosedFood.setLayoutManager(layoutManager);
        lvChoosedFood.setAdapter(choosedFoodAdapter);
        tvChoosedItems = (TextView) findViewById(R.id.tvChoosedFoodItems);
        tvChoosedPrice  = (TextView) findViewById(R.id.tvChoosedFoodPrice);
        rbChinese = (RadioButton) findViewById(R.id.rbChinese);
        rbEnglish = (RadioButton) findViewById(R.id.rbEnglish);
        FrameLayout btnOrder = (FrameLayout) findViewById(R.id.checkoutButton);
        tvOrdersLabel = (TextView) findViewById(R.id.tvChoosedFoodLabel);
        TextView tvRefreshData = (TextView)findViewById(R.id.drawermenu_refreshdata);
        TextView tvServerURL = (TextView)findViewById(R.id.drawermenu_serverurl);
        listViewCategorys = (CategoryTabListView) findViewById(R.id.categorytab_listview);
        displayFragmentsLayout = (FrameLayout) findViewById(R.id.dishdisplayarea_layout);
        ImageButton btnLookfor = (ImageButton)findViewById(R.id.btnLookforDish);
        btnLookfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuickSearchDialog dlg = new QuickSearchDialog(MainActivity.this);
                dlg.showDialog();
            }
        });
        tvRefreshData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshDataDialog dlg = new RefreshDataDialog(MainActivity.this);
                dlg.showDialog();
            }
        });

        tvServerURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveServerURLDialog dlg = new SaveServerURLDialog(MainActivity.this);
                dlg.showDialog();
            }
        });
        rbChinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChangeLanguage(LANGUAGE_CHINESE);
            }
        });

        rbEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChangeLanguage(LANGUAGE_ENGLISH);
            }
        });
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartOrder();
            }
        });

        //init tool class, NoHttp
        NoHttp.initialize(this);
        Logger.setDebug(true);
        Logger.setTag("digitalmenu:nohttp");

        InstantValue.URL_TOMCAT = IOOperator.loadServerURL();
        httpOperator = new HttpOperator(this);
        dbOperator = new DBOperator(this);

        //read local database to memory
        desks = dbOperator.queryDesks();
        category1s = dbOperator.queryAllMenu();


        dlgPostOrder = new PostOrderDialog(this);

        startRefreshMenuTimer();
        buildMenu();
//        ViewServer.get(this).addWindow(this);
    }

    /**
     * For reduce the time of switch different fragments, build all fragments at the start time and store
     * them in a SparseArray. While need to display one fragment, just get it from the list.
     * one category2 = one fragment
     */
    private void initialFoodCellComponents(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int leftMargin = (screenWidth - 180 -260 - 3 * InstantValue.DISPLAY_DISH_WIDTH) / 4;
        if (leftMargin < 7)
            leftMargin = 7;
        if (category1s != null){
            TableRow.LayoutParams trlp = new TableRow.LayoutParams();
            trlp.topMargin = 7;
            trlp.leftMargin = leftMargin;
            trlp.width = InstantValue.DISPLAY_DISH_WIDTH;
            trlp.height = InstantValue.DISPLAY_DISH_HEIGHT;
            Bundle bundle = new Bundle();
            ActionBar.LayoutParams ablp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            for (Category1 c1 : category1s){
                if (c1.getCategory2s() != null){
                    for (Category2 c2 : c1.getCategory2s()){
                        DishDisplayFragment frag = new DishDisplayFragment();
                        bundle.putSerializable("category2", c2);
                        frag.setArguments(bundle);

                        View view = View.inflate(this, R.layout.dishdisplay_layout, null);
                        TableLayout contentLayout = (TableLayout) view.findViewById(R.id.dishdisplay_content);
                        ScrollView sv = new ScrollView(this);
                        sv.setLayoutParams(ablp);

                        ArrayList<Dish> dishes = c2.getDishes();
                        if (dishes != null){
                            TableLayout tl = new TableLayout(this);
                            TableRow tr = null;

                            for(int i = 0; i< dishes.size(); i++){
                                Dish dish = dishes.get(i);
                                if (i % InstantValue.DISPLAY_DISH_COLUMN_NUMBER == 0){
                                    tr = new TableRow(this);
                                    tl.addView(tr);
                                }
                                DishCellComponent fc = new DishCellComponent(this, dish);
                                tr.addView(fc.getFoodCellView(), trlp);
                                //这里要把fc先加入进tablerow才可以设置background,否则fc会被background的size撑大
                                if (dish.getPictureName() != null) {
                                    Drawable d = IOOperator.getDishImageDrawable(this.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG + dish.getPictureName());
                                    fc.setPicture(d);
                                }
                                mapFoodCellComponents.put(dish.getId(), fc);
                            }
                            sv.addView(tl);
                        }

                        contentLayout.addView(sv);

                        frag.setView(view);
                        mapDishDisplayFragments.put(c2.getId(), frag);
                    }
                }
            }
        }
    }

    /**
     * set a timer to load the server menu, just now, only focus on the SOLDOUT status.
     */
    private void startRefreshMenuTimer(){
        refreshMenuHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1){
                    ArrayList<Integer> dishIdList = (ArrayList<Integer>) msg.obj;
                    //loop to find Dish Object depending on the id, reload the data from database
                    for(Integer dishId : dishIdList){
                        Dish dish = dbOperator.queryDishById(dishId);
                        //remind clients if the sold out dish are selected
                        for(ChoosedDish cf : choosedFoodList){
                            if (cf.getDish().getId() == dishId){
                                if (dish.isSoldOut()) {
                                    String errormsg = "Dish " + dish.getEnglishName() + " is Sold Out already, please remove it from your selection.";
                                    if (getLanguage() == LANGUAGE_CHINESE)
                                        errormsg = "您选择的 " + dish.getChineseName() + " 已经售完, 请从列表中将其去除.";
                                    popupWarnDialog(R.drawable.error, "Warning", errormsg);
                                }
                            }
                        }
                    }
                }
            }
        };
        //start timer
        refreshMenuTimer = new Timer();
        refreshMenuTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(InstantValue.URL_TOMCAT == null || InstantValue.URL_TOMCAT.length() == 0)
                    return;
                MenuVersion mv = (MenuVersion) dbOperator.queryObjectById(1, MenuVersion.class);
                int localVersion = 0;
                if (mv != null) {
                    localVersion = mv.getVersion();
                }

                ArrayList<Integer> dishIdList = httpOperator.chechMenuVersion(localVersion);
                if (dishIdList != null && !dishIdList.isEmpty()){
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = dishIdList;
                    refreshMenuHandler.sendMessage(msg);
                }
            }
        }, 1, refreshMenuPeroid
        );
    }

    public PostOrderDialog getPostOrderDialog(){
        return dlgPostOrder;
    }

    private void onStartOrder(){
        if (choosedFoodList == null || choosedFoodList.isEmpty())
            return;
        dlgPostOrder.clearup();
        dlgPostOrder.showDialog(httpOperator, choosedFoodList);
    }

    public void onFinishMakeOrder(String title, String message){
        //clear data
        choosedFoodList.clear();
        choosedFoodAdapter.notifyDataSetChanged();
//        adapter.clear();
        tvChoosedItems.setText("0");
        tvChoosedPrice.setText("$0");
        dlgPostOrder.dismiss();
        int fcCount = mapFoodCellComponents.size();
        for (int i = 0; i< fcCount; i++){
            DishCellComponent fc = mapFoodCellComponents.valueAt(i);
            fc.changeAmount(0);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(R.drawable.info);
        builder.setNegativeButton("OK", null);
        builder.create().show();
    }

    public void buildMenu(){
        initialFoodCellComponents();

        CategoryTabAdapter categoryTabAdapter = new CategoryTabAdapter(MainActivity.this, R.layout.categorytab_listitem_layout, category1s);
        listViewCategorys.setAdapter(categoryTabAdapter);
        listViewCategorys.post(new Runnable() {
            @Override
            public void run() {
                listViewCategorys.chooseItemByPosition(0);
            }
        });

        onChangeLanguage(getLanguage());
        Message msg = new Message();
        msg.what = 0;
        progressDlgHandler.sendMessage(msg);
    }

    public void startProgressDialog(String title, String message){
        progressDlg = ProgressDialog.show(this, title, message);
    }
    public DBOperator getDbOperator(){
        return dbOperator;
    }

    public HttpOperator getHttpOperator(){
        return httpOperator;
    }

    public void persistMenu(){
        dbOperator.clearMenu();
        dbOperator.saveObjectsByCascade(category1s);
    }

    public void persistDesk(){
        dbOperator.clearDesk();
        dbOperator.saveObjectsByCascade(desks);
    }

    /**
     * execute while click the dish add button.
     *
     * For common dishes, it can be added into choosed list directly, but for some special dishes,
     * such as the hot pot soup, we need the customer choose a favor before adding into choosed list,
     * this is a compulsive operation/requirements.
     *
     * Thus we check the dish's property to decide if we need to pop a dialog to obligate user do something,
     * just now, we just do special for hot pot soup. In future, maybe there will be same problem with other dishes
     *
     * 1. the dish does not need to do special, then add it into choosed list directly
     *
     * 2. the dish needs some special, popup a dialog and do the operation of adding into lish while user click
     * the dialog's CONFIRM button
     *
     * Created by Jerry on 2017/9/29.
     */
    public void onDishChoosed(Dish dish) {
        if (dish.isSoldOut()){
            Toast.makeText(MainActivity.this, "This dish is sold out now.", Toast.LENGTH_LONG).show();
            return;
        }
        if (dish.getCategory2().getEnglishName().toLowerCase().equals("soup")
                && dish.getCategory2().getCategory1().getEnglishName().toLowerCase().equals("hot pot")){
            ChooseHotpotSoupDialog dlg = new ChooseHotpotSoupDialog(this, dish);
            dlg.showDialog();
        } else {
            addDishInChoosedList(dish, null);
        }

    }

    public void addDishInChoosedList(Dish dish, String requirements) {
        ChoosedDish choosedFood = null;
        //first check if the dish is exist in the list already
        for (ChoosedDish cf : choosedFoodList) {
            if (cf.getDish().getId() == dish.getId()) {
                choosedFood = cf;
                break;
            }
        }
        if (choosedFood != null) {
            choosedFood.setAmount(choosedFood.getAmount() + 1);
        } else {
            choosedFood = new ChoosedDish(dish);
            choosedFoodList.add(choosedFood);
        }
        if (requirements != null) {
            choosedFood.setAdditionalRequirements(requirements);
        }
        choosedFoodAdapter.notifyDataSetChanged();
        //show choosed icon
        DishCellComponent fc = mapFoodCellComponents.get(dish.getId());
        fc.changeAmount(choosedFood.getAmount());
        calculateFoodPrice();
    }

    private void calculateFoodPrice(){
        double totalPrice = 0.0;
        for(ChoosedDish cf : choosedFoodList){
            totalPrice += cf.getAmount() * cf.getPrice();
        }
        double gst = totalPrice / 11;
        tvChoosedItems.setText(String.valueOf(choosedFoodList.size()));
        tvChoosedPrice.setText(InstantValue.DOLLAR + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, totalPrice));
    }

//    public static MainActivity getInstance(){
//        return instance;
//    }

    public void plusDish(int position) {
        choosedFoodList.get(position).setAmount(choosedFoodList.get(position).getAmount() + 1);
        //show choosed icon
        DishCellComponent fc = mapFoodCellComponents.get(choosedFoodList.get(position).getDish().getId());
        fc.changeAmount(choosedFoodList.get(position).getAmount());
        calculateFoodPrice();
        choosedFoodAdapter.notifyItemChanged(position);
    }

    public void minusDish(int position) {
        if (position >= choosedFoodList.size()){
            return; //点击太快可以导致同时出发多次事件, 前面的时间把列表清空后, 后面的就出发OutofBounds异常
        }
        Dish dish = choosedFoodList.get(position).getDish();
        int oldAmount = choosedFoodList.get(position).getAmount();
        //change small icon in FoodCellComponent
        DishCellComponent fc = mapFoodCellComponents.get(dish.getId());
        fc.changeAmount(oldAmount - 1);

        choosedFoodList.get(position).setAmount( oldAmount - 1);
        if (oldAmount == 1) {
            choosedFoodList.remove(position);
            choosedFoodAdapter.notifyItemRemoved(position);
            choosedFoodAdapter.notifyItemRangeChanged(position, choosedFoodList.size());
        }else {
            choosedFoodAdapter.notifyItemChanged(position);
        }
        calculateFoodPrice();
    }

    public void addRequirements(int position) {
        ChoosedDish cf = choosedFoodList.get(position);
        AddOrderRequirementsDialog dlg = new AddOrderRequirementsDialog(this);
        dlg.showDialog(cf);
    }

    public ArrayList<ChoosedDish> getChoosedFoodList() {
        return choosedFoodList;
    }
//    @Override
//    public void deleteDish(int position) {
//        choosedFoodList.remove(position);
//        calculateFoodPrice();
//        choosedFoodAdapter.notifyItemChanged(position);
//    }

    /**
     * 1. clear local database
     * 2. clear local dish pictures
     * 3. load data from server, including desk, menu, menuversion, dish picture files
     * 4. after loading finish, redraw the UI
     */
    public void onRefreshData(){
        //clear all data and picture files
        IOOperator.deleteDishPicture(InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG);
        IOOperator.deleteDishPicture(InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL);
        dbOperator.deleteAllData(Desk.class);
        dbOperator.deleteAllData(MenuVersion.class);
        dbOperator.deleteAllData(Dish.class);
        dbOperator.deleteAllData(Category2.class);
        dbOperator.deleteAllData(Category1.class);
        // synchronize and persist
        httpOperator.loadDeskData();
        httpOperator.loadMenuVersionData();
        httpOperator.loadMenuData();
    }

    public SparseArray<DishDisplayFragment> getMapDishDisplayFragments() {
        return mapDishDisplayFragments;
    }

    public void setMapDishDisplayFragments(SparseArray<DishDisplayFragment> mapDishDisplayFragments) {
        this.mapDishDisplayFragments = mapDishDisplayFragments;
    }

    public SparseArray<DishCellComponent> getMapFoodCellComponents() {
        return mapFoodCellComponents;
    }

    public void setMapFoodCellComponents(SparseArray<DishCellComponent> mapFoodCellComponents) {
        this.mapFoodCellComponents = mapFoodCellComponents;
    }

    /**
     * while change language, there 2 types components
     * 1. common components, need to change by code
     * 2. ChangeLanguageTextView. for this type, look for them from the root window
     * @param language
     */
    private void onChangeLanguage(byte language){
        if (language == LANGUAGE_ENGLISH){
            tvOrdersLabel.setText(R.string.choosed_food_label_en);
        } else if (language == LANGUAGE_CHINESE){
            tvOrdersLabel.setText(R.string.choosed_food_label_cn);
        }
        //find all ChangeLanguageTextView and invoke its change language text
        ArrayList<ChangeLanguageTextView> tvs = lookforAllChangeLanguageTextView(this.getWindow().getDecorView());
        int fragCount = mapDishDisplayFragments.size();
        for (int i = 0; i< fragCount ; i++){
            DishDisplayFragment frag = mapDishDisplayFragments.valueAt(i);
            tvs.addAll(lookforAllChangeLanguageTextView(frag.getMyView()));
        }

        for(ChangeLanguageTextView tv : tvs){
            tv.show(getLanguage());
        }
    }

    private ArrayList<ChangeLanguageTextView> lookforAllChangeLanguageTextView(View view){
        ArrayList<ChangeLanguageTextView> list = new ArrayList<ChangeLanguageTextView>();
        if (view instanceof ViewGroup){
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0; i< vg.getChildCount(); i++){
                View child = vg.getChildAt(i);
                if (child.getClass().equals(ChangeLanguageTextView.class)){
                    list.add((ChangeLanguageTextView)child);
                }
                list.addAll(lookforAllChangeLanguageTextView(child));
            }
        }
        return list;
    }

    public byte getLanguage(){
        if (rbChinese.isChecked())
            return LANGUAGE_CHINESE;
        else return LANGUAGE_ENGLISH;
    }

    public void setMenu(ArrayList<Category1> category1s){
        this.category1s = category1s;
    }

    public ArrayList<Category1> getMenu(){
        return this.category1s;
    }
    public void setDesk(ArrayList<Desk> desks){
        this.desks = desks;
    }

    public ArrayList<Desk> getDesks() {
        return desks;
    }

    public void popupToast(String msg, int shortlong){
        Toast.makeText(this, msg, shortlong).show();
    }

    public void popupWarnDialog(int iconId, String title, String msg){
        new AlertDialog.Builder(this)
                .setIcon(iconId)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton("OK", null)
                .create().show();
    }

    //屏蔽实体按键BACK
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //屏蔽recent task 按键, some pad devices are different with the virtual device, such as Sumsung Tab E
    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext() .getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        ActivityManager activityManager = (ActivityManager) getApplicationContext() .getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    /**
     * stop for Sumsung's Recent Task button
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshMenuTimer = null;
//        ViewServer.get(this).removeWindow(this);
    }
}
