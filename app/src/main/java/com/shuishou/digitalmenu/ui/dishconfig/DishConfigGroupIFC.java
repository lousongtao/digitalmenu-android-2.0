package com.shuishou.digitalmenu.ui.dishconfig;

import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.DishConfigGroup;

import java.util.ArrayList;

/**
 * 每个DishConfigGroup构造的view都要实现该接口, 通过统一的方法进行操作
 * Created by Administrator on 18/02/2018.
 */

public interface DishConfigGroupIFC {
    /**
     * 检查数据完整性, 如果不完整, 需要在该方法内弹出错误对话框, 只向外界返回true/false, 外界不再显示错误信息
     * @return
     */
    boolean checkData();
    ArrayList<DishConfig> getChoosedData();
    DishConfigGroup getDishConfigGroup();

    /**
     * do extra operation while click the config item
     */
    void onConfigComponentClick(DishConfig config);

    void refreshColor();
}
