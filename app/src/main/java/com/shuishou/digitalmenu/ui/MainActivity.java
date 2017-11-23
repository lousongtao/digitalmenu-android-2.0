package com.shuishou.digitalmenu.ui;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.shuishou.digitalmenu.bean.DishChoosePopinfo;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.db.DBOperator;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;
import com.shuishou.digitalmenu.utils.CommonTool;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MainActivity.class.getSimpleName());
    public final static byte LANGUAGE_FIRSTLANGUAGE = 1;
    public final static byte LANGUAGE_SECONDLANGUAGE = 2;
    private String TAG_UPLOADERRORLOG = "uploaderrorlog";
    private String TAG_EXITSYSTEM = "exitsystem";
    private String TAG_LOOKFOR = "lookfor";
    private String TAG_REFRESHDATA = "refreshdata";
    private String TAG_SERVERURL = "serverurl";
    private String TAG_RBFIRSTLANGUAGE = "rbFirstLanguage";
    private String TAG_RBSECONDLANGUAGE = "rbSecondLanguage";
    private String TAG_BTNORDER = "btnorder";
    private CategoryTabListView listViewCategorys;
    private RadioButton rbFirstLanguage;
    private RadioButton rbSecondLanguage;
    private TextView tvChoosedItems;
    private TextView tvChoosedPrice;
    private TextView tvOrdersLabel;

    private ArrayList<Desk> desks;
    private ArrayList<Flavor> flavors;
    private RecyclerChoosedDishAdapter choosedDishAdapter;
    private ArrayList<ChoosedDish> choosedDishList= new ArrayList<>();
    private ArrayList<Category1> category1s = new ArrayList<>(); // = TestData.makeCategory1();
    private HashMap<String, String> configsMap;
    private HttpOperator httpOperator;
    private DBOperator dbOperator;

    private PostOrderDialog dlgPostOrder;
    private ChooseFlavorDialog dlgChooseFlavor;
    private DishDetailDialog dlgDishDetail;

    public static final int REFRESHMENUHANDLER_MSGWHAT_REFRESHMENU = 1;
    private Handler refreshMenuHandler;
    private Timer refreshMenuTimer;

    private String logTag = "TestTime-MainActivity";

    private SparseArray<DishDisplayFragment> mapDishDisplayFragments = new SparseArray<>();
    private SparseArray<DishCellComponent> mapDishCellComponents = new SparseArray<>();

    public static final int PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA = 3;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH = 2;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_SHOWPROGRESS = 1;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG = 0;
    private ProgressDialog progressDlg;
    private Handler progressDlgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG) {
                if (progressDlg != null)
                    progressDlg.dismiss();
            } else if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_SHOWPROGRESS){
                if (progressDlg != null){
                    progressDlg.setMessage(msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING);
                }
            } else if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH){
                if (progressDlg != null){
                    progressDlg.setMessage(msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING);
                }
            } else if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA){
                if (progressDlg != null){
                    progressDlg.setMessage(msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING);
                }
            }
        }
    };
    public static final int TOASTHANDLERWHAT_ERRORMESSAGE = 0;
    private Handler toastHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TOASTHANDLERWHAT_ERRORMESSAGE){
                Toast.makeText(MainActivity.this,msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING, Toast.LENGTH_LONG).show();
            }
        }
    };

    public Handler getToastHandler(){
        return toastHandler;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: test
//        String s = null;
//        if (s.equals("")){
//
//        }

        setContentView(R.layout.activity_main);
        RecyclerView lvChoosedDish = (RecyclerView) findViewById(R.id.list_choosedfood);
        choosedDishAdapter = new RecyclerChoosedDishAdapter(this, R.layout.choosedfood_item, choosedDishList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lvChoosedDish.setLayoutManager(layoutManager);
        lvChoosedDish.setAdapter(choosedDishAdapter);
        tvChoosedItems = (TextView) findViewById(R.id.tvChoosedFoodItems);
        tvChoosedPrice  = (TextView) findViewById(R.id.tvChoosedFoodPrice);
        rbFirstLanguage = (RadioButton) findViewById(R.id.rbFirstLanguage);
        rbSecondLanguage = (RadioButton) findViewById(R.id.rbSecondLanguage);
        FrameLayout btnOrder = (FrameLayout) findViewById(R.id.checkoutButton);
        tvOrdersLabel = (TextView) findViewById(R.id.tvChoosedFoodLabel);
        TextView tvRefreshData = (TextView)findViewById(R.id.drawermenu_refreshdata);
        TextView tvServerURL = (TextView)findViewById(R.id.drawermenu_serverurl);
        TextView tvUploadErrorLog = (TextView)findViewById(R.id.drawermenu_uploaderrorlog);
        TextView tvExit = (TextView)findViewById(R.id.drawermenu_exit);
        listViewCategorys = (CategoryTabListView) findViewById(R.id.categorytab_listview);
//        displayFragmentsLayout = (FrameLayout) findViewById(R.id.dishdisplayarea_layout);
        ImageButton btnLookfor = (ImageButton)findViewById(R.id.btnLookforDish);

        tvUploadErrorLog.setTag(TAG_UPLOADERRORLOG);
        tvExit.setTag(TAG_EXITSYSTEM);
        btnLookfor.setTag(TAG_LOOKFOR);
        tvRefreshData.setTag(TAG_REFRESHDATA);
        tvServerURL.setTag(TAG_SERVERURL);
        rbFirstLanguage.setTag(TAG_RBFIRSTLANGUAGE);
        rbSecondLanguage.setTag(TAG_RBSECONDLANGUAGE);
        btnOrder.setTag(TAG_BTNORDER);
        tvUploadErrorLog.setOnClickListener(this);
        tvExit.setOnClickListener(this);
        btnLookfor.setOnClickListener(this);
        tvRefreshData.setOnClickListener(this);
        tvServerURL.setOnClickListener(this);
        rbFirstLanguage.setOnClickListener(this);
        rbSecondLanguage.setOnClickListener(this);
        btnOrder.setOnClickListener(this);

        //init tool class, NoHttp
        NoHttp.initialize(this);
        Logger.setDebug(true);
        Logger.setTag("digitalmenu:nohttp");

        InstantValue.URL_TOMCAT = IOOperator.loadServerURL(InstantValue.FILE_SERVERURL);
        httpOperator = new HttpOperator(this);
        dbOperator = new DBOperator(this);

        if (InstantValue.URL_TOMCAT != null && InstantValue.URL_TOMCAT.length() > 0)
            httpOperator.queryConfigsMap();

        //read local database to memory
        desks = dbOperator.queryDesks();
        flavors = dbOperator.queryFlavors();

        dlgPostOrder = PostOrderDialog.getInstance(this);
        dlgChooseFlavor = ChooseFlavorDialog.getInstance(this);
        dlgDishDetail = DishDetailDialog.getInstance(this);

        startRefreshMenuTimer();
        buildMenu();
    }

    /**
     * For reduce the time of switch different fragments, build all fragments at the start time and store
     * them in a SparseArray. While need to display one fragment, just get it from the list.
     * one category2 = one fragment
     */
    private void initialDishCellComponents(){
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
                                tr.addView(fc.getDishCellView(), trlp);
                                //这里要把fc先加入进tablerow才可以设置background,否则fc会被background的size撑大
                                if (dish.getPictureName() != null) {
                                    Drawable d = IOOperator.getDishImageDrawable(this.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG + dish.getPictureName());
                                    fc.setPicture(d);
                                }
                                mapDishCellComponents.put(dish.getId(), fc);
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
                if (msg.what == REFRESHMENUHANDLER_MSGWHAT_REFRESHMENU){
                    ArrayList<Integer> dishIdList = (ArrayList<Integer>) msg.obj;
                    //loop to find Dish Object depending on the id, reload the data from database
                    for(Integer dishId : dishIdList){
                        Dish dish = dbOperator.queryDishById(dishId);
                        mapDishCellComponents.get(dish.getId()).setSoldOutVisibility(dish.isSoldOut());
                        //remind clients if the sold out dish are selected
                        for(ChoosedDish cf : choosedDishList){
                            if (cf.getDish().getId() == dishId){
                                if (dish.isSoldOut()) {
                                    String errormsg = "Dish " + dish.getFirstLanguageName() + " is Sold Out already, please remove it from your selection.";
                                    /**
                                     * this is just for Chinese restaurant use, this is not good code
                                     */
                                    if ((rbFirstLanguage.isChecked() && "中文".equals(rbFirstLanguage.getText()))
                                        || (rbSecondLanguage.isChecked() && "中文".equals(rbSecondLanguage.getText())))
                                        errormsg = "您选择的 " + dish.getFirstLanguageName() + " 已经售完, 请从列表中将其去除.";
                                    CommonTool.popupWarnDialog(MainActivity.this, R.drawable.error, "Warning", errormsg);
                                }
                            }
                        }
                    }
                }
            }
        };
        //start timer
        refreshMenuTimer = new Timer();
        int refreshMenuPeroid = 60 * 1000;
        refreshMenuTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(InstantValue.URL_TOMCAT == null || InstantValue.URL_TOMCAT.length() == 0)
                    return;
                //if local database is null, stop check
                if (category1s == null || category1s.isEmpty())
                    return;
                MenuVersion mv = (MenuVersion) dbOperator.queryObjectById(1, MenuVersion.class);
                int localVersion = 0;
                if (mv != null) {
                    localVersion = mv.getVersion();
                }

                ArrayList<Integer> dishIdList = httpOperator.chechMenuVersion(localVersion);
                if (dishIdList != null && !dishIdList.isEmpty()){
                    refreshMenuHandler.sendMessage(CommonTool.buildMessage(REFRESHMENUHANDLER_MSGWHAT_REFRESHMENU, dishIdList));
                }
            }
        }, 1, refreshMenuPeroid
        );
    }

    public PostOrderDialog getPostOrderDialog(){
        return dlgPostOrder;
    }

    private void onStartOrder(){
        if (choosedDishList == null || choosedDishList.isEmpty())
            return;
        dlgPostOrder.clearup();
        dlgPostOrder.showDialog(httpOperator, choosedDishList);
    }

    public void onFinishMakeOrder(String title, String message){
        //clear data
        choosedDishList.clear();
        choosedDishAdapter.notifyDataSetChanged();
//        adapter.clear();
        tvChoosedItems.setText("0");
        tvChoosedPrice.setText("$0");
        dlgPostOrder.dismiss();
        int fcCount = mapDishCellComponents.size();
        for (int i = 0; i< fcCount; i++){
            DishCellComponent fc = mapDishCellComponents.valueAt(i);
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
        category1s = dbOperator.queryAllMenu();
        initialDishCellComponents();

        CategoryTabAdapter categoryTabAdapter = new CategoryTabAdapter(MainActivity.this, R.layout.categorytab_listitem_layout, category1s);
        listViewCategorys.setAdapter(categoryTabAdapter);
        listViewCategorys.post(new Runnable() {
            @Override
            public void run() {
                listViewCategorys.chooseItemByPosition(0);
            }
        });

        onChangeLanguage();
        progressDlgHandler.sendMessage(CommonTool.buildMessage(PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG));
    }

    public Handler getProgressDlgHandler(){
        return progressDlgHandler;
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

    public void persistFlavor(){
        dbOperator.clearFlavor();
        dbOperator.saveObjectsByCascade(flavors);
    }

    /**
     * execute while click the dish add button.
     *
     * For common dishes, it can be added into choosed list directly, but for some special dishes,
     * such as the hot pot soup, we need the customer choose a favor before adding into choosed list,
     * this is a compulsive operation/requirements.
     *
     * Thus we check the dish's CHOOSEMODE property to decide if we need to do other operation before adding into list
     * the different conditions are:
     *
     * 1. CHOOSEMODE == InstantValue.DISH_CHOOSEMODE_DEFAULT, the dish does not need to do special, then add it into choosed list directly
     *
     * 2. CHOOSEMODE == InstantValue.DISH_CHOOSEMODE_SUBITEM, the dish needs to choose some items before adding,
     *      popup a dialog and to list the opinions to comstomer, if the customer gives up to choose subitems, then cancel the adding operation;
     *      ONLY adding into list after customer click the dialog's CONFIRM button(do some validatation if needed)
     *
     * 3. CHOOSEMODE == InstantValue.DISH_CHOOSEMODE_POPINFOCHOOSE, popup a message to tell some information before adding the list
     *
     * 4. CHOOSEMODE == InstantValue.DISH_CHOOSEMODE_POPINFOQUIT, popup a message, and then quit the operation(no adding this dish to the list)
     *
     * Created by Jerry on 2017/9/29.
     */
    public void onDishChoosed(final Dish dish) {
        if (dish.isSoldOut()){
            Toast.makeText(MainActivity.this, "This dish is sold out now.", Toast.LENGTH_LONG).show();
            return;
        }
        if (dish.getChooseMode() == InstantValue.DISH_CHOOSEMODE_DEFAULT){
            addDishInChoosedList(dish);
        } else if (dish.getChooseMode() == InstantValue.DISH_CHOOSEMODE_SUBITEM){
            ChooseDishSubitemDialog dlg = new ChooseDishSubitemDialog(this, dish);
            dlg.showDialog();
        } else if (dish.getChooseMode() == InstantValue.DISH_CHOOSEMODE_POPINFOCHOOSE){
            String msg = (getLanguage() == LANGUAGE_FIRSTLANGUAGE) ? dish.getChoosePopInfo().getFirstLanguageName() : dish.getChoosePopInfo().getSecondLanguageName();
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.info)
                    .setTitle("Infomation")
                    .setMessage(msg)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addDishInChoosedList(dish);
                        }
                    })
                    .create().show();
        } else if (dish.getChooseMode() == InstantValue.DISH_CHOOSEMODE_POPINFOQUIT){
            String msg = (getLanguage() == LANGUAGE_SECONDLANGUAGE) ? dish.getChoosePopInfo().getFirstLanguageName() : dish.getChoosePopInfo().getSecondLanguageName();
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.info)
                    .setTitle("Infomation")
                    .setMessage(msg)
                    .setNegativeButton("OK", null)
                    .create().show();
        }
    }

    /**
     * add dish into choosed list
     * 1. dish.automerge = true(default value),
     *      add this dish to the choosed list. If the dish already exists, merge them into one item, just make amount plus one;
     *      show PLUS & MINUS buttons in the choosed list, customer can add/reduce the dish's amount;
     *      recalculate all choosed list price;
     *      refresh the dish's choosed amount and show it using a small icon.
     *
     * 2. dish.automerge = false;
     *      add a new item into list no matter whether the same dish exist or not. the amount always keep ONE;
     *      show PLUS & MINUS buttons in the choosed list, customer can add/reduce the dish's amount;
     *      recalculate all choosed list price;
     *      refresh the dish's choosed amount and show it using a small icon
     *
     * otherwise, add a new item into list no matter whether the same dish exist or not.
     * @param dish
     * @param
     */
    public void addDishInChoosedList(Dish dish, ArrayList<DishChooseSubitem> subItems) {
        ChoosedDish choosedDish = null;
        if (dish.isAutoMergeWhileChoose()){
            //first check if the dish is exist in the list already
            for (ChoosedDish cf : choosedDishList) {
                if (cf.getDish().getId() == dish.getId()) {
                    choosedDish = cf;
                    break;
                }
            }
            if (choosedDish != null) {
                choosedDish.setAmount(choosedDish.getAmount() + 1);
            } else {
                choosedDish = new ChoosedDish(dish);
                choosedDishList.add(choosedDish);
            }

        } else {
            choosedDish = new ChoosedDish(dish);
            choosedDishList.add(choosedDish);
        }
        if (subItems != null && !subItems.isEmpty()) {
            choosedDish.setDishSubitemList(subItems);
        }
        choosedDishAdapter.notifyDataSetChanged();
        calculateDishPrice();
        refreshChooseAmountOnDishCell(dish);
    }

    public void addDishInChoosedList(Dish dish) {
        addDishInChoosedList(dish, null);
    }

    private void refreshChooseAmountOnDishCell(Dish dish){
        DishCellComponent fc = mapDishCellComponents.get(dish.getId());
        int amount = 0;
        for(ChoosedDish cf : choosedDishList){
            if (cf.getDish().getId() == dish.getId())
                amount += cf.getAmount();
        }
        fc.changeAmount(amount);
    }

    private void calculateDishPrice(){
        double totalPrice = 0.0;
        for(ChoosedDish cf : choosedDishList){
            totalPrice += cf.getAmount() * cf.getPrice();
        }
//        double gst = totalPrice / 11;
        tvChoosedItems.setText(String.valueOf(choosedDishList.size()));
        tvChoosedPrice.setText(InstantValue.DOLLAR + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, totalPrice));
    }

    public void plusDish(int position) {
        choosedDishList.get(position).setAmount(choosedDishList.get(position).getAmount() + 1);
        //show choosed icon
        refreshChooseAmountOnDishCell(choosedDishList.get(position).getDish());
        calculateDishPrice();
        choosedDishAdapter.notifyItemChanged(position);
    }

    public void minusDish(int position) {
        if (position >= choosedDishList.size()){
            return; //点击太快可以导致同时触发多次事件, 前面的时间把列表清空后, 后面的就出发OutofBounds异常
        }
        Dish dish = choosedDishList.get(position).getDish();
        int oldAmount = choosedDishList.get(position).getAmount();

        if (oldAmount == 1) {
            choosedDishList.remove(position);
            choosedDishAdapter.notifyItemRemoved(position);
            choosedDishAdapter.notifyItemRangeChanged(position, choosedDishList.size());
        }else {
            choosedDishList.get(position).setAmount( oldAmount - 1);
            choosedDishAdapter.notifyItemChanged(position);
        }
        calculateDishPrice();
        refreshChooseAmountOnDishCell(dish);
    }

    public void flavorDish(int position){
        ChoosedDish cd = choosedDishList.get(position);
        dlgChooseFlavor.initValue(cd);
        dlgChooseFlavor.showDialog();
    }

    public void showDishDetailDialog(Dish dish){
        int choosedAmount = 0;
        for(ChoosedDish cd : choosedDishList){
            if (dish.getId() == cd.getDish().getId()){
                choosedAmount = cd.getAmount();
                break;
            }
        }
        dlgDishDetail.showDialog(getLanguage(), dish, choosedAmount);
    }
    public void notifyChoosedDishFlavorChanged(){
        choosedDishAdapter.notifyDataSetChanged();
    }
    public void notifyChoosedDishFlavorChanged(int position){
        choosedDishAdapter.notifyItemChanged(position);
    }
    public void notifyChoosedDishFlavorChanged(ChoosedDish cd){
        int position = -1;
        for (int i = 0; i< choosedDishList.size(); i++){
            if (cd.getDish().getId() == choosedDishList.get(i).getDish().getId()){
                position = i;
                break;
            }
        }
        if (position > -1){
            choosedDishAdapter.notifyItemChanged(position);
        }
    }
    public ArrayList<ChoosedDish> getChoosedDishList() {
        return choosedDishList;
    }

    public HashMap<String, String> getConfigsMap() {
        return configsMap;
    }

    public void setConfigsMap(HashMap<String, String> configsMap) {
        this.configsMap = configsMap;

        if ("1".equals(configsMap.get(InstantValue.CONFIGS_LANGUAGEAMOUNT))){
            rbFirstLanguage.setVisibility(View.GONE);
            rbSecondLanguage.setVisibility(View.GONE);
        } else {
            rbFirstLanguage.setText(configsMap.get(InstantValue.CONFIGS_FIRSTLANGUAGENAME));
            rbSecondLanguage.setText(configsMap.get(InstantValue.CONFIGS_SECONDLANGUAGENAME));
            rbFirstLanguage.setChecked(true);
        }
    }

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
        IOOperator.deleteDishPicture(InstantValue.LOCAL_CATALOG_DISH_PICTURE_ORIGIN);
        dbOperator.deleteAllData(Desk.class);
        dbOperator.deleteAllData(MenuVersion.class);
        dbOperator.deleteAllData(Flavor.class);
        dbOperator.deleteAllData(DishChoosePopinfo.class);
        dbOperator.deleteAllData(DishChooseSubitem.class);
        dbOperator.deleteAllData(Dish.class);
        dbOperator.deleteAllData(Category2.class);
        dbOperator.deleteAllData(Category1.class);
        // synchronize and persist
        httpOperator.loadDeskData();
        httpOperator.loadFlavorData();
        httpOperator.loadMenuVersionData();
        httpOperator.loadMenuData();
    }

    @Override
    public void onClick(View v) {
        if (TAG_UPLOADERRORLOG.equals(v.getTag())){
            IOOperator.onUploadErrorLog(this);
        } else if (TAG_LOOKFOR.equals(v.getTag())){
            QuickSearchDialog dlg = new QuickSearchDialog(MainActivity.this);
            dlg.showDialog();
        } else if (TAG_REFRESHDATA.equals(v.getTag())){
            RefreshDataDialog dlg = new RefreshDataDialog(MainActivity.this);
            dlg.showDialog();
        } else if (TAG_SERVERURL.equals(v.getTag())){
            SaveServerURLDialog dlg = new SaveServerURLDialog(MainActivity.this);
            dlg.showDialog();
        } else if (TAG_RBFIRSTLANGUAGE.equals(v.getTag())){
            onChangeLanguage();
        } else if (TAG_RBSECONDLANGUAGE.equals(v.getTag())){
            onChangeLanguage();
        } else if (TAG_BTNORDER.equals(v.getTag())){
            onStartOrder();
        } else if (TAG_EXITSYSTEM.equals(v.getTag())){
            QuitSystemDialog dlg = new QuitSystemDialog(this);
            dlg.showDialog();
        }
    }



    public SparseArray<DishDisplayFragment> getMapDishDisplayFragments() {
        return mapDishDisplayFragments;
    }

    public void setMapDishDisplayFragments(SparseArray<DishDisplayFragment> mapDishDisplayFragments) {
        this.mapDishDisplayFragments = mapDishDisplayFragments;
    }

    public SparseArray<DishCellComponent> getMapDishCellComponents() {
        return mapDishCellComponents;
    }

    public void setMapDishCellComponents(SparseArray<DishCellComponent> mapDishCellComponents) {
        this.mapDishCellComponents = mapDishCellComponents;
    }

    /**
     * while change language, use the root view to find deeply all his children.
     * if the child is ChangeLanguageTextView, then add into a list;
     * at last, loop this list to change its language.
     * REMEMBER : the ListView need to special because for those items outside the window, they build until move into screen,
     * so CategoryTabLayoutItem class add a method to check language.
     */
    private void onChangeLanguage(){
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

    /**
     * recursive call to find all children which type is ChangeLanguageTextView
     * @param view
     * @return
     */
    public ArrayList<ChangeLanguageTextView> lookforAllChangeLanguageTextView(View view){
        ArrayList<ChangeLanguageTextView> list = new ArrayList<>();

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
        if (rbSecondLanguage.isChecked())
             return LANGUAGE_SECONDLANGUAGE;
        else return LANGUAGE_FIRSTLANGUAGE;
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

    public ArrayList<Flavor> getFlavors() {
        return flavors;
    }

    public void setFlavors(ArrayList<Flavor> flavors) {
        this.flavors = flavors;
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
    }
}
