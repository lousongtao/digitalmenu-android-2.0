package com.shuishou.digitalmenu;

/**
 * Created by Administrator on 2016/12/22.
 */

public final class InstantValue {
    public static final String DOLLAR = "$";
    public static final String DOLLARSPACE = "$ ";
    public static final String NULLSTRING = "";
    public static final String SPACESTRING = " ";
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAIL = "FAIL";
    public static final String CHECKDESK4MAKEORDER_AVAILABLE = "AVAILABLE";
    public static final String CHECKDESK4MAKEORDER_OCCUPIED = "OCCUPIED";
    public static final byte DISH_STATUS_NORMAL = 0;
    public static final byte DISH_STATUS_SOLDOUT = 1; //缺货
    public static final byte DISH_STATUS_ONSALE = 2;//促销

    public static final int DISPLAY_DISH_WIDTH = 240;
    public static final int DISPLAY_DISH_HEIGHT = 300;

    public static final int DESKWIDTH_IN_POSTORDERDIALOG = 70;
    public static final int DESKHEIGHT_IN_POSTORDERDIALOG = 70;

    public static final byte DISH_CHOOSEMODE_DEFAULT = 1;
    public static final byte DISH_CHOOSEMODE_SUBITEM = 2;
    public static final byte DISH_CHOOSEMODE_POPINFOCHOOSE = 3;
    public static final byte DISH_CHOOSEMODE_POPINFOQUIT = 4;

    public static final byte DISH_PURCHASETYPE_UNIT = 1;//按份购买
    public static final byte DISH_PURCHASETYPE_WEIGHT = 2;//按重量购买

    public static final String FORMAT_DOUBLE_2DECIMAL = "%.2f";

    public static String URL_TOMCAT = null;
    public static final String SERVER_CATALOG_DISH_PICTURE_BIG = "dishimage_big";
    public static final String SERVER_CATALOG_DISH_PICTURE_SMALL = "dishimage_small";
    public static final String SERVER_CATALOG_DISH_PICTURE_ORIGIN = "dishimage_origin";
    public static final String LOCAL_CATALOG_DISH_PICTURE_ORIGIN = "/data/data/com.shuishou.digitalmenu/dishimage_origin/";
    public static final String LOCAL_CATALOG_DISH_PICTURE_BIG = "/data/data/com.shuishou.digitalmenu/dishimage_big/";
    public static final String LOCAL_CATALOG_DISH_PICTURE_SMALL = "/data/data/com.shuishou.digitalmenu/dishimage_small/";
    public static final String LOCAL_CATALOG_ERRORLOG = "/data/data/com.shuishou.digitalmenu/errorlog/";
    public static final String FILE_SERVERURL = "/data/data/com.shuishou.digitalmenu/serverconfig";
    public static final String ERRORLOGPATH = "/data/data/com.shuishou.digitalmenu/errorlog/";

    public static final String CONFIGS_CONFIRMCODE = "CONFIRMCODE";
    public static final String CONFIGS_OPENCASHDRAWERCODE = "OPENCASHDRAWERCODE";
    public static final String CONFIGS_LANGUAGEAMOUNT = "LANGUAGEAMOUNT";
    public static final String CONFIGS_FIRSTLANGUAGENAME= "FIRSTLANGUAGENAME";
    public static final String CONFIGS_FIRSTLANGUAGEABBR = "FIRSTLANGUAGEABBR";
    public static final String CONFIGS_SECONDLANGUAGENAME= "SECONDLANGUAGENAME";
    public static final String CONFIGS_SECONDLANGUAGEABBR = "SECONDLANGUAGEABBR";
}
