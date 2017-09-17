package com.shuishou.digitalmenu;

/**
 * Created by Administrator on 2016/12/22.
 */

public final class InstantValue {

    public static final byte DISH_STATUS_NORMAL = 0;
    public static final byte DISH_STATUS_SOLDOUT = 1; //缺货
    public static final byte DISH_STATUS_ONSALE = 2;//促销

    public static final int DISPLAY_DISH_COLUMN_NUMBER = 3; //菜单界面每行显示的数目/列数

    public static final int DISPLAY_DISH_WIDTH = 240;
    public static final int DISPLAY_DISH_HEIGHT = 300;

    public static String URL_TOMCAT = null;
    public static final String SERVER_CATALOG_DISH_PICTURE_BIG = "dishimage_big";
    public static final String SERVER_CATALOG_DISH_PICTURE_SMALL = "dishimage_small";
    public static final String LOCAL_CATALOG_DISH_PICTURE_BIG = "/data/data/com.shuishou.digitalmenu/dishimage_big/";
    public static final String LOCAL_CATALOG_DISH_PICTURE_SMALL = "/data/data/com.shuishou.digitalmenu/dishimage_small/";
    public static final String FILE_SERVERURL = "/data/data/com.shuishou.digitalmenu/serverconfig";

}
