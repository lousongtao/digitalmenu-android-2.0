package com.shuishou.digitalmenu.ui;

import android.annotation.SuppressLint;
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
import android.widget.RadioButton;
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
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.bean.MenuVersion;
import com.shuishou.digitalmenu.bean.UserData;
import com.shuishou.digitalmenu.db.DBOperator;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.ui.components.ChangeLanguageTextView;
import com.shuishou.digitalmenu.ui.dishconfig.DishConfigDialogBuilder;
import com.shuishou.digitalmenu.uibean.ChoosedDish;
import com.shuishou.digitalmenu.utils.CommonTool;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
    private String TAG_UPGRADEAPP = "upgradeapp";

    private CategoryTabListView listViewCategorys;
    private CategoryTabAdapter categoryTabAdapter;
    private RadioButton rbFirstLanguage;
    private RadioButton rbSecondLanguage;
    private TextView tvChoosedItems;
    private TextView tvChoosedPrice;
    private View rightUpPanel;
    private View rightBottomPanel;
    private RecyclerView lvChoosedDish;

    private ArrayList<Desk> desks;
    private ArrayList<Flavor> flavors;
    private ArrayList<UserData> waiters;
    private RecyclerChoosedDishAdapter choosedDishAdapter;
    private ArrayList<ChoosedDish> choosedDishList= new ArrayList<>();
    private ArrayList<Category1> category1s = new ArrayList<>(); // = TestData.makeCategory1();
    private HashMap<String, String> configsMap;
    private HttpOperator httpOperator;
    private DBOperator dbOperator;

//    private PostOrderDialog dlgPostOrder;
//    private ChooseFlavorDialog dlgChooseFlavor;
//    private DishDetailDialog dlgDishDetail;

    private RefreshMenuTimer refreshMenuTimer;

    private String logTag = "TestTime-MainActivity";

    private SparseArray<DishDisplayFragment> mapDishDisplayFragments = new SparseArray<>();
    private SparseArray<DishCellComponent> mapDishCellComponents = new SparseArray<>();

    public static final int PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA = 3;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH = 2;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_SHOWPROGRESS = 1;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG = 0;
    private ProgressDialog progressDlg;
    @SuppressLint("HandlerLeak")
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
    @SuppressLint("HandlerLeak")
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
        lvChoosedDish = (RecyclerView) findViewById(R.id.list_choosedfood);
        choosedDishAdapter = new RecyclerChoosedDishAdapter(this, R.layout.choosedfood_item, choosedDishList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        lvChoosedDish.setLayoutManager(layoutManager);
        lvChoosedDish.setAdapter(choosedDishAdapter);
        tvChoosedItems = (TextView) findViewById(R.id.tvChoosedFoodItems);
        tvChoosedPrice  = (TextView) findViewById(R.id.tvChoosedFoodPrice);
        rbFirstLanguage = (RadioButton) findViewById(R.id.rbFirstLanguage);
        rbSecondLanguage = (RadioButton) findViewById(R.id.rbSecondLanguage);
        FrameLayout btnOrder = (FrameLayout) findViewById(R.id.checkoutButton);
        TextView tvRefreshData = (TextView)findViewById(R.id.drawermenu_refreshdata);
        TextView tvServerURL = (TextView)findViewById(R.id.drawermenu_serverurl);
        TextView tvUploadErrorLog = (TextView)findViewById(R.id.drawermenu_uploaderrorlog);
        TextView tvExit = (TextView)findViewById(R.id.drawermenu_exit);
        TextView tvUpgradeAPP = (TextView) findViewById(R.id.drawermenu_upgradeapp);
        TextView tvTest = (TextView) findViewById(R.id.drawermenu_test);
        listViewCategorys = (CategoryTabListView) findViewById(R.id.categorytab_listview);
        ImageButton btnLookfor = (ImageButton)findViewById(R.id.btnLookforDish);
        rightUpPanel = findViewById(R.id.rightUpPanel);
        rightBottomPanel = findViewById(R.id.rightBottomPanel);

        tvUploadErrorLog.setTag(TAG_UPLOADERRORLOG);
        tvExit.setTag(TAG_EXITSYSTEM);
        tvUpgradeAPP.setTag(TAG_UPGRADEAPP);
        btnLookfor.setTag(TAG_LOOKFOR);
        tvRefreshData.setTag(TAG_REFRESHDATA);
        tvServerURL.setTag(TAG_SERVERURL);
        tvTest.setTag("test");
        rbFirstLanguage.setTag(TAG_RBFIRSTLANGUAGE);
        rbSecondLanguage.setTag(TAG_RBSECONDLANGUAGE);
        btnOrder.setTag(TAG_BTNORDER);
        tvUploadErrorLog.setOnClickListener(this);
        tvUpgradeAPP.setOnClickListener(this);
        tvExit.setOnClickListener(this);
        btnLookfor.setOnClickListener(this);
        tvRefreshData.setOnClickListener(this);
        tvServerURL.setOnClickListener(this);
        tvTest.setOnClickListener(this);
        rbFirstLanguage.setOnClickListener(this);
        rbSecondLanguage.setOnClickListener(this);
        btnOrder.setOnClickListener(this);

        //init tool class, NoHttp
        NoHttp.initialize(this);
        Logger.setDebug(true);
        Logger.setTag("digitalmenu:nohttp");

        InstantValue.URL_TOMCAT = IOOperator.loadServerURL(InstantValue.FILE_SERVERURL);
        loadConfigInfo();

        httpOperator = new HttpOperator(this);
        dbOperator = new DBOperator(this);

        if (InstantValue.URL_TOMCAT != null && InstantValue.URL_TOMCAT.length() > 0) {
            httpOperator.queryConfigsMap();
        }

        //read local database to memory
        desks = dbOperator.queryDesks();
        flavors = dbOperator.queryFlavors();
        httpOperator.loadWaiterData();

        buildMenu();

        loadLogoFile();

        refreshMenuTimer = new RefreshMenuTimer(this);
    }

    private void loadConfigInfo(){
        Map<String, Object> config = IOOperator.loadConfigInfo(InstantValue.FILE_CONFIGINFO);
        if (config != null){
            if(config.get(InstantValue.CONFIGINFO_SHOWDISHPIC) != null){
                InstantValue.SETTING_SHOWDISHPICTURE = Boolean.parseBoolean(config.get(InstantValue.CONFIGINFO_SHOWDISHPIC).toString());
            }
        }
    }

    //每次启动APP, 先检查是否有本地logo图片, 如果没有, 需要通过Refresh Data操作来同步logo
    private void loadLogoFile(){
        Drawable d = IOOperator.getDishImageDrawable(this.getResources(), InstantValue.LOGO_PATH + "rightup.jpg");
        if (d != null){
            rightUpPanel.setBackground(d);
        }
        d = IOOperator.getDishImageDrawable(this.getResources(), InstantValue.LOGO_PATH + "rightbottom.jpg");
        if (d != null){
            rightBottomPanel.setBackground(d);
        }
        d = IOOperator.getDishImageDrawable(this.getResources(), InstantValue.LOGO_PATH + "chooseddishbg.jpg");
        if (d != null){
            lvChoosedDish.setBackground(d);
        }
    }

    /**
     * For reduce the time of switch different fragments, build all fragments at the start time and store
     * them in a SparseArray. While need to display one fragment, just get it from the list.
     * one category2 = one fragment
     */
    private void initialDishDisplayFragment(){
        int DISPLAY_DISH_COLUMN_NUMBER = 3; //菜单界面每行显示的数目/列数
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = (int) (displayMetrics.widthPixels / displayMetrics.density);
        //屏幕宽度减去左右两侧固定控件的宽度, 除以间隙数目, 得到应该保留的间隙宽度
        int leftMargin = (screenWidth - InstantValue.DISPLAY_LEFTCATEGORY_WIDTH
                - InstantValue.DISPLAY_RIGHTORDER_WIDTH - 3 * InstantValue.DISPLAY_DISH_WIDTH)
                / (DISPLAY_DISH_COLUMN_NUMBER + 1);
        if (leftMargin < 0){
            DISPLAY_DISH_COLUMN_NUMBER = 2; //for small screen, show 2 columns
            //recalculate the margin
            leftMargin = (screenWidth - InstantValue.DISPLAY_LEFTCATEGORY_WIDTH
                    - InstantValue.DISPLAY_RIGHTORDER_WIDTH - 3 * InstantValue.DISPLAY_DISH_WIDTH)
                    / (DISPLAY_DISH_COLUMN_NUMBER + 1);
            if (leftMargin < 0) {
                DISPLAY_DISH_COLUMN_NUMBER = 1;
            }
            }
        if (leftMargin < 7)
            leftMargin = 7;
        if (category1s != null){
            Bundle bundle = new Bundle();
            bundle.putInt(DishDisplayFragment.BUNDLE_COLUMNS, DISPLAY_DISH_COLUMN_NUMBER);
            bundle.putInt(DishDisplayFragment.BUNDLE_LEFTMARGIN, (int)(leftMargin * displayMetrics.density));
            bundle.putInt(DishDisplayFragment.BUNDLE_TOPMARGIN, 15);
            for (Category1 c1 : category1s){
                if (c1.getCategory2s() != null){
                    for (Category2 c2 : c1.getCategory2s()){
                        DishDisplayFragment frag = new DishDisplayFragment();
                        bundle.putSerializable(DishDisplayFragment.BUNDLE_CATEGORY2, c2);
                        frag.setArguments(bundle);
                        frag.init(this);
                        mapDishDisplayFragments.put(c2.getId(), frag);
                    }
                }
            }
        }
    }

//    public PostOrderDialog getPostOrderDialog(){
//        return dlgPostOrder;
//    }

    private void onStartOrder(){
        if (choosedDishList == null || choosedDishList.isEmpty())
            return;
        PostOrderDialog dlgPostOrder = new PostOrderDialog(this);
        dlgPostOrder.showDialog(httpOperator, choosedDishList);
    }

    public void onFinishMakeOrder(String title, String message){
        //clear data
        choosedDishList.clear();
        choosedDishAdapter.notifyDataSetChanged();
//        adapter.clear();
        tvChoosedItems.setText("0");
        tvChoosedPrice.setText("$0");
//        dlgPostOrder.dismiss();
        int fcCount = mapDishCellComponents.size();
        for (int i = 0; i< fcCount; i++){
            DishCellComponent fc = mapDishCellComponents.valueAt(i);
            fc.changeAmount(0);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(R.drawable.success);
        builder.setNegativeButton("OK", null);
        builder.create().show();
    }

    public void buildMenu(){
        category1s = dbOperator.queryAllMenu();
        //do sort firstly
        if (category1s != null){
            Collections.sort(category1s, new Comparator<Category1>() {
                @Override
                public int compare(Category1 category1, Category1 t1) {
                    return category1.getSequence() - t1.getSequence();
                }
            });
            for (int i = 0; i < category1s.size(); i++) {
                ArrayList<Category2> c2s = category1s.get(i).getCategory2s();
                if (c2s != null){
                    Collections.sort(c2s, new Comparator<Category2>() {
                        @Override
                        public int compare(Category2 o1, Category2 o2) {
                            return o1.getSequence() - o2.getSequence();
                        }
                    });
                    for (int j = 0; j< c2s.size(); j++){
                        if (c2s.get(j).getDishes() != null){
                            Collections.sort(c2s.get(j).getDishes(), new Comparator<Dish>() {
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

        initialDishDisplayFragment();

        categoryTabAdapter = new CategoryTabAdapter(MainActivity.this, R.layout.categorytab_listitem_layout, category1s);
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

    public void popRestartDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(msg)
                .setIcon(R.drawable.info)
                .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });
        AlertDialog dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        dlg.show();
    }

    public Handler getProgressDlgHandler(){
        return progressDlgHandler;
    }

    public void startProgressDialog(String title, String message){
        progressDlg = ProgressDialog.show(this, title, message);
    }

    public void stopProgressDialog(){
        progressDlgHandler.sendMessage(CommonTool.buildMessage(PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG));
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
     * 1. check if DishConfig existing, if true, popup the DishConfig dialog.
     *
     * 2. CHOOSEMODE == InstantValue.DISH_CHOOSEMODE_DEFAULT, the dish does not need to do special, then add it into choosed list directly
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
        if (dish.getConfigGroups() != null && !dish.getConfigGroups().isEmpty()){
            new DishConfigDialogBuilder(this).showConfigDialog(dish);
        } else if (dish.getChooseMode() == InstantValue.DISH_CHOOSEMODE_DEFAULT){
            addDishInChoosedList(dish, null);
        } else if (dish.getChooseMode() == InstantValue.DISH_CHOOSEMODE_POPINFOCHOOSE){
            String msg = (getLanguage() == LANGUAGE_FIRSTLANGUAGE) ? dish.getChoosePopInfo().getFirstLanguageName() : dish.getChoosePopInfo().getSecondLanguageName();
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.info)
                    .setTitle("Infomation")
                    .setMessage(msg)
                    .setPositiveButton("  OK  ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addDishInChoosedList(dish, null);
                        }
                    })
                    .create().show();
        } else if (dish.getChooseMode() == InstantValue.DISH_CHOOSEMODE_POPINFOQUIT){
            String msg = (getLanguage() == LANGUAGE_FIRSTLANGUAGE) ? dish.getChoosePopInfo().getFirstLanguageName() : dish.getChoosePopInfo().getSecondLanguageName();
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
     *      自动隐藏掉选择列表中的加号, 因为不合并的不需要再添加, 但是可以减少
     *
     * otherwise, add a new item into list no matter whether the same dish exist or not.
     * @param dish
     * @param
     */
    public void addDishInChoosedList(Dish dish, ArrayList<DishConfig> configs) {
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
        if (configs != null && !configs.isEmpty()) {
            choosedDish.setDishConfigList(configs);
        }
        choosedDishAdapter.notifyDataSetChanged();
        calculateDishPrice();
        refreshChooseAmountOnDishCell(dish);
    }

    /**
     * 在dish的选择按钮上添加一个数字角标, 标记这道菜已经点过, 以免用户误操作
     * @param dish
     */
    private void refreshChooseAmountOnDishCell(Dish dish){
        DishCellComponent fc = mapDishCellComponents.get(dish.getId());
        int amount = 0;
        for(ChoosedDish cf : choosedDishList){
            if (cf.getDish().getId() == dish.getId())
                amount += cf.getAmount();
        }
        fc.changeAmount(amount);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void calculateDishPrice(){
        double totalPrice = 0.0;
        for(ChoosedDish cf : choosedDishList){
            totalPrice += cf.getAmount() * cf.getPrice();
        }
//        double gst = totalPrice / 11;
        tvChoosedItems.setText(String.valueOf(choosedDishList.size()));
        tvChoosedPrice.setText(InstantValue.DOLLAR + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, totalPrice));
    }

    public TextView getTvChoosedPrice(){
        return tvChoosedPrice;
    }

    public void plusDish(int position) {
        if (position >= choosedDishList.size()){
            return;//sometimes here report IndexOutOfboundsException
        }
        choosedDishList.get(position).setAmount(choosedDishList.get(position).getAmount() + 1);
        //show choosed icon
        refreshChooseAmountOnDishCell(choosedDishList.get(position).getDish());
        calculateDishPrice();
        choosedDishAdapter.notifyItemChanged(position);
    }

    public void minusDish(int position) {
        if (position >= choosedDishList.size()){
            return; //点击太快可以导致同时触发多次事件, 前面的事件把列表清空后, 后面的就触发OutofBounds异常
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
        if (position >= choosedDishList.size()){
            return;//sometimes here report IndexOutOfBoundsException
        }
        ChoosedDish cd = choosedDishList.get(position);
        ChooseFlavorDialog dlgChooseFlavor = new ChooseFlavorDialog(this);
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
        DishDetailDialog dlgDishDetail = new DishDetailDialog(this);
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
     * 1. stop the refresh timer
     * 2. clear local database
     * 3. clear local dish pictures
     * 4. load data from server, including desk, menu, menuversion, dish picture files
     * 5. after loading finish, redraw the UI
     */
    public void onRefreshData(){
        if (refreshMenuTimer != null){
            refreshMenuTimer.cancel();
            refreshMenuTimer.purge();
            refreshMenuTimer = null;
        }

        //clear all data and picture files
        IOOperator.deleteLocalFiles(InstantValue.LOCAL_CATALOG_DISH_PICTURE_BIG);
        IOOperator.deleteLocalFiles(InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL);
        IOOperator.deleteLocalFiles(InstantValue.LOCAL_CATALOG_DISH_PICTURE_ORIGIN);
        IOOperator.deleteLocalFiles(InstantValue.LOGO_PATH);
        dbOperator.deleteAllData(Desk.class);
        dbOperator.deleteAllData(MenuVersion.class);
        dbOperator.deleteAllData(Flavor.class);
        dbOperator.deleteAllData(DishChoosePopinfo.class);
        dbOperator.deleteAllData(DishChooseSubitem.class);
        dbOperator.deleteAllData(Dish.class);
        dbOperator.deleteAllData(Category2.class);
        dbOperator.deleteAllData(Category1.class);
        // synchronize and persist
        httpOperator.loadLogoPictureFromServer();
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
        } else if ("test".equals(v.getTag())){
            TestDialog dlg = new TestDialog(this);
            dlg.showDialog();
        } else if (TAG_UPGRADEAPP.equals(v.getTag())){
            UpgradeAppDialog dlg = new UpgradeAppDialog(this);
            dlg.showDialog();
        }
    }

    public CategoryTabAdapter getCategoryTabAdapter(){
        return categoryTabAdapter;
    }

    public RecyclerView getListViewChoosedDish(){
        return lvChoosedDish;
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
                if (child instanceof ChangeLanguageTextView){
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

    public ArrayList<UserData> getWaiters() {
        return waiters;
    }

    public void setWaiters(ArrayList<UserData> waiters) {
        this.waiters = waiters;
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

//
    /**
     * 客户现场经常发现点菜不能加入到右侧列表的现象; 初步推测, 是原有的listener对象绑定了老的MainActivity对象.
     * 尝试一下每次resume时, 重构这些listener并将其与控件绑定
     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (dlgPostOrder == null)
//            dlgPostOrder = PostOrderDialog.getInstance(this);
//        else if (dlgPostOrder.getMainActivity() != this){
//            LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " lousongtao test : find different MainActivity object for PostOrderDialog");
//            dlgPostOrder = PostOrderDialog.rebuildInstance(this);
//        }
//        if (dlgChooseFlavor == null)
//            dlgChooseFlavor = ChooseFlavorDialog.getInstance(this);
//        else if (dlgChooseFlavor.getMainActivity() != this) {
//            LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " lousongtao test : find different MainActivity object for ChooseFlavorDialog");
//            dlgChooseFlavor = ChooseFlavorDialog.rebuildInstance(this);
//        }
//        if (dlgDishDetail == null)
//            dlgDishDetail = DishDetailDialog.getInstance(this);
//        else if (dlgDishDetail.getMainActivity() != this) {
//            LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " lousongtao test : find different MainActivity object for DishDetailDialog");
//            dlgDishDetail = DishDetailDialog.rebuildInstance(this);
//        }
//        //start timer
//        if (refreshMenuTimer == null)
//            refreshMenuTimer = new RefreshMenuTimer(this);
//        else if (refreshMenuTimer.getMainActivity() != this){
//            LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " lousongtao test : find different MainActivity object for RefreshMenuTimer");
//            refreshMenuTimer.cancel();
//            refreshMenuTimer = new RefreshMenuTimer(this);
//        }
//
//        if (choosedDishAdapter != null && choosedDishAdapter.getMainActivity() != this) {
//            LOG.debug(InstantValue.DFYMDHMS.format(new Date()) + " lousongtao test : find different MainActivity object for ChoosedDishAdapter");
//            choosedDishAdapter = new RecyclerChoosedDishAdapter(this, R.layout.choosedfood_item, choosedDishList);
//            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//            lvChoosedDish.setLayoutManager(layoutManager);
//            lvChoosedDish.setAdapter(choosedDishAdapter);
//        }
//
//
//        ChooseDishListener.rebuildInstance(this);
//        ClickDishPictureListener.rebuildInstance(this);
//        ChoosedFoodClickListener.rebuildInstance(this);
//
//        for (int i = 0; i < mapDishCellComponents.size(); i++) {
//            int key = mapDishCellComponents.keyAt(i);
//            DishCellComponent cell = mapDishCellComponents.get(key);
//            cell.setListener();
//        }
//    }

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

        refreshMenuTimer = null;
//        ChooseDishListener.release();
//        ClickDishPictureListener.release();
        super.onDestroy();
    }
}
