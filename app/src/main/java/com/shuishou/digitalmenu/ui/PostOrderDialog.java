package com.shuishou.digitalmenu.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Desk;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.bean.HttpResult;
import com.shuishou.digitalmenu.http.HttpOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;
import com.shuishou.digitalmenu.utils.CommonTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Administrator on 2017/7/21.
 */

public class PostOrderDialog {
    private static PostOrderDialog instance;
    private EditText txtCode;
    private EditText txtCustomerAmount;
    private EditText txtComments;
    private TableLayout deskAreaLayout;
    private ArrayList<ChoosedDish> choosedFoodList;
    private HttpOperator httpOperator;

    private AlertDialog dlg;

    private ArrayList<DeskIcon> deskIconList = new ArrayList<>();

    private MainActivity mainActivity;

    private DeskClickListener deskClickListener = new DeskClickListener();
    private final static int MESSAGEWHAT_CHECKCONFIRMCODE=1;
    private final static int MESSAGEWHAT_CHECKDESKAVAILABLE=2;
    private final static int MESSAGEWHAT_MAKEORDERSUCCESS=3;
    private final static int MESSAGEWHAT_ADDDISHSUCCESS=4;
    private final static int MESSAGEWHAT_ASKTOADDDISHINORDER=5;
    private final static int MESSAGEWHAT_ERRORTOAST=8;
    private final static int MESSAGEWHAT_ERRORDIALOG=9;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dealHandlerMessage(msg);
            super.handleMessage(msg);
        }
    };

    private PostOrderDialog(@NonNull MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        initUI();
    }

    public static PostOrderDialog getInstance(MainActivity mainActivity){
        if (instance == null)
            instance = new PostOrderDialog(mainActivity);
        return instance;
    }
    private void initUI(){
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.postorderdialog_layout, null);
        txtCode = (EditText) view.findViewById(R.id.txt_confirmcode);
        deskAreaLayout = (TableLayout)view.findViewById(R.id.postorder_deskarea);
        txtCustomerAmount = (EditText) view.findViewById(R.id.txt_customeramount);
        txtComments = (EditText) view.findViewById(R.id.txtComments);
        initDeskData(mainActivity.getDesks());
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
//        builder.setTitle("Confirm");
//        builder.setMessage("Please input the CONFIRMATION CODE before post this order!");
//        builder.setIcon(R.drawable.info);
        //here cannot use listener on the positive button because the dialog will dismiss no matter
        //the input value is valiable or not. I wish the dialog keep while input info is wrong.
        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", null);
        builder.setView(view);
        dlg = builder.create();

        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //add listener for YES button
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeOrder();
                    }
                });
            }
        });
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);

    }

    public void showDialog(HttpOperator httpOperator, ArrayList<ChoosedDish> choosedFoodList){
        this.choosedFoodList = choosedFoodList;
        this.httpOperator = httpOperator;
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 0;
        param.width = WindowManager.LayoutParams.MATCH_PARENT;
        param.height = 330;
        window.setAttributes(param);
        dlg.show();

    }

    private void dealHandlerMessage(Message msg){
        switch (msg.what){
            case MESSAGEWHAT_ERRORTOAST :
                Toast.makeText(mainActivity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                break;
            case MESSAGEWHAT_ERRORDIALOG:
                CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "WRONG", msg.obj.toString());
                break;
            case MESSAGEWHAT_MAKEORDERSUCCESS:
                mainActivity.onFinishMakeOrder("SUCCESS", "Finish make order! Order Sequence : " + msg.obj);
                break;
            case MESSAGEWHAT_ASKTOADDDISHINORDER:
                addDishToOrderWithAsk(Integer.parseInt(msg.obj.toString()));
                break;
            case MESSAGEWHAT_ADDDISHSUCCESS:
                mainActivity.onFinishMakeOrder("SUCCESS", "Add dish successfully");
                break;
        }
    }

    private void makeOrder(){
        if (txtCode.getText() == null || txtCode.getText().length() == 0) {
            Toast.makeText(mainActivity, "Please input the Confirmation Code to post this order!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (txtCustomerAmount.getText() == null || txtCustomerAmount.getText().length() == 0){
            Toast.makeText(mainActivity, "Please input the amount of customer!", Toast.LENGTH_SHORT).show();
            return;
        }
        DeskIcon choosedDeskIcon = null;
        for (DeskIcon di: deskIconList) {
            if (di.isChoosed()){
                choosedDeskIcon = di;
                break;
            }
        }
        if (choosedDeskIcon == null){
            Toast.makeText(mainActivity, "Please select the desk before post this order!", Toast.LENGTH_SHORT).show();
            return;
        }
        final Desk choosedDesk = choosedDeskIcon.getDesk();
        //check confirm code and desk status
        new Thread(){
            @Override
            public void run() {
                if (mainActivity.getConfigsMap().get(InstantValue.CONFIGS_CONFIRMCODE).equals(txtCode.getText().toString())){
                    String deskstatus = httpOperator.checkDeskStatus(choosedDesk.getName());
                    if (InstantValue.CHECKDESK4MAKEORDER_OCCUPIED.equals(deskstatus)){
                        handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ASKTOADDDISHINORDER, choosedDesk.getId()));
                    } else if (InstantValue.CHECKDESK4MAKEORDER_AVAILABLE.equals(deskstatus)){
                        makeNewOrder(choosedDesk.getId());
                    } else {
                        handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ERRORDIALOG, deskstatus));
                    }
                } else {
                    handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ERRORDIALOG, "confirm code is wrong"));
                }
            }
        }.start();
    }

    //this function must be call in a non-UI thread
    private void makeNewOrder(int deskid){
        JSONArray os = null;
        try {
            os = generateOrderJson();
        } catch (JSONException e) {
            handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ERRORDIALOG,
                    "There are error to build JSON Object, please restart APP!"));
            return;
        }

        if (os != null){
            HttpResult<Integer> result = httpOperator.makeOrder(txtCode.getText().toString(), os.toString(), deskid,
                    Integer.parseInt(txtCustomerAmount.getText().toString()), txtComments.getText().toString());
            if (result.success){
                handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_MAKEORDERSUCCESS, result.data));
            } else {
                handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ERRORDIALOG,
                        "Something wrong happened while making order! \n\nError message : " + result.result));
            }
        }
    }

    private void addDishToOrderWithAsk(final int deskid){
        AlertDialog askDialog = new AlertDialog.Builder(dlg.getContext())
                .setTitle("Add Order")
                .setMessage("There is an order on this table already. \n\nWill you add these dishes in the order?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONArray os = null;
                        try {
                            os = generateOrderJson();
                        } catch (JSONException e) {
                            Toast.makeText(mainActivity, "There are error to build JSON Object, please !", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (os != null){
                            final String oss = os.toString();
                            new Thread(){
                                @Override
                                public void run() {
                                    HttpResult<Integer> result = httpOperator.addDishToOrder(deskid,oss);
                                    if (result.success){
                                        handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ADDDISHSUCCESS));
                                    } else {
                                        handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ERRORDIALOG,
                                                "Something wrong happened while add dishes! \n\nError message : " + result.result));
                                    }
                                }
                            }.start();
                        }
                    }
                }).create();
        askDialog.show();
    }

    private JSONArray generateOrderJson() throws JSONException {
        JSONArray ja = new JSONArray();
        for(ChoosedDish cd: choosedFoodList){
            JSONObject jo = new JSONObject();
            jo.put("id", cd.getDish().getId());
            jo.put("amount", cd.getAmount());
            StringBuffer sbReq = new StringBuffer();
            if (cd.getDishSubitemList() != null && !cd.getDishSubitemList().isEmpty()){
                for ( DishChooseSubitem si: cd.getDishSubitemList()) {
                    sbReq.append(si.getChineseName() + InstantValue.SPACESTRING);
                }
            }
            if (cd.getFlavorList() != null && !cd.getFlavorList().isEmpty()){
                for (Flavor f: cd.getFlavorList()){
                    sbReq.append(f.getChineseName()+ InstantValue.SPACESTRING);
                }
            }
            jo.put("additionalRequirements", sbReq.toString());
            ja.put(jo);
        }
        return ja;
    }
    public void initDeskData(ArrayList<Desk> desks){
        deskAreaLayout.removeAllViews();
        int margin = 5;
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.setMargins(margin, margin ,0 ,0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int rowamount = (int) Math.floor((displayMetrics.widthPixels - 200) / InstantValue.DESKWIDTH_IN_POSTORDERDIALOG);
//        int rowamount = (int) Math.floor(mainActivity.getWindow().getAttributes().width / InstantValue.DESKWIDTH_IN_POSTORDERDIALOG);
        TableRow tr = null;
        for (int i = 0; i < desks.size(); i++) {
            if (i % rowamount == 0){
                tr = new TableRow(mainActivity);
                deskAreaLayout.addView(tr);
            }
            DeskIcon di = new DeskIcon(mainActivity, desks.get(i));
            deskIconList.add(di);
            tr.addView(di, trlp);
        }

    }

    //clear up old data in the components
    public void clearup(){
        txtCode.setText(InstantValue.NULLSTRING);
        txtComments.setText(InstantValue.NULLSTRING);
        for(DeskIcon di : deskIconList){
            di.setChoosed(false);
        }
    }

    public void dismiss(){
        dlg.dismiss();
    }

    class DeskIcon extends android.support.v7.widget.AppCompatTextView{
        private Desk desk;
        private boolean choosed;
        public DeskIcon(Context context, Desk desk){
            super(context);
            this.desk = desk;
            initDeskUI();
        }

        private void initDeskUI(){
            setTextSize(18);
            setTextColor(Color.BLACK);
            setBackgroundColor(Color.LTGRAY);
            setText(desk.getName());
            setHeight(InstantValue.DESKHEIGHT_IN_POSTORDERDIALOG);
            setWidth(InstantValue.DESKWIDTH_IN_POSTORDERDIALOG);
            setOnClickListener(deskClickListener);
            setEllipsize(TextUtils.TruncateAt.END);
        }

        public void setChoosed(boolean b){
            choosed = b;
            if (b){
                this.setBackgroundColor(Color.GREEN);
            } else {
                this.setBackgroundColor(Color.LTGRAY);
            }
        }

        public boolean isChoosed(){
            return choosed;
        }

        public Desk getDesk() {
            return desk;
        }
    }

    class DeskClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getClass().getName().equals(DeskIcon.class.getName())){
                for(DeskIcon di : deskIconList){
                    di.setChoosed(false);
                }
                ((DeskIcon)v).setChoosed(true);
            }
        }
    }
}
