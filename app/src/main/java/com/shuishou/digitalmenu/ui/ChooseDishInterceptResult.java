package com.shuishou.digitalmenu.ui;

public class ChooseDishInterceptResult {
    public boolean success;
    public Object obj;

    public ChooseDishInterceptResult(boolean success, Object obj){
        this.success = success;
        this.obj = obj;
    }
}
