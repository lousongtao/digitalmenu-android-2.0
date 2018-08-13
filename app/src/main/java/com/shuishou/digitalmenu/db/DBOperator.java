package com.shuishou.digitalmenu.db;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Desk;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.bean.DishChoosePopinfo;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.ui.MainActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/9.
 */

public class DBOperator {
//    private final MainActivity mainActivity;
    private static LiteOrm liteOrm;

    public DBOperator(MainActivity mainActivity){
//        this.mainActivity = mainActivity;
        if (liteOrm == null){
            liteOrm = LiteOrm.newCascadeInstance(mainActivity, "digitalmenu.db");
        }
        liteOrm.setDebugged(true);
    }

//    public void saveObjectBySingle(Object o){
//        liteOrm.single().save(o);
//    }
//
//    public void saveObjectsBySingle(List objects){
//        liteOrm.single().save(objects);
//    }

    public void saveObjectByCascade(Object o){
        liteOrm.cascade().save(o);
    }

    public void saveObjectsByCascade(ArrayList objects){
        liteOrm.cascade().save(objects);
    }

    public void updateObject(Object o){
        liteOrm.update(o);
    }

    public Object queryObjectById(int id, Class c){
        return liteOrm.queryById(id, c);
    }
    public void deleteAllData(Class c){
        liteOrm.deleteAll(c);
    }
    public void deleteObject(Object o){
        liteOrm.delete(o);
    }
    public ArrayList<Category1> queryAllMenu(){
        return liteOrm.cascade().query(Category1.class);
    }

    public Dish queryDishById(int dishId){
        return liteOrm.queryById(dishId, Dish.class);
    }

    public ArrayList<Dish> queryDishByParentId(int category2Id){
        return liteOrm.query(new QueryBuilder<Dish>(Dish.class).where("category2Id = " + category2Id ));
    }

    public ArrayList<Category2> queryCategory2ByParentId(int category1Id){
        return liteOrm.query(new QueryBuilder<Category2>(Category2.class).where("category1Id = " + category1Id ));
    }

    public void clearMenu(){
        liteOrm.deleteAll(DishChoosePopinfo.class);
        liteOrm.deleteAll(DishChooseSubitem.class);
        liteOrm.deleteAll(Dish.class);
        liteOrm.deleteAll(Category2.class);
        liteOrm.deleteAll(Category1.class);
    }

    public void clearDesk(){
        liteOrm.deleteAll(Desk.class);
    }

    public ArrayList<Desk> queryDesks(){
        return liteOrm.query(new QueryBuilder<Desk>(Desk.class).appendOrderAscBy("sequence"));
    }

    public void clearFlavor(){
        liteOrm.deleteAll(Flavor.class);
    }

    public ArrayList<Flavor> queryFlavors(){
        return liteOrm.query(Flavor.class);
    }
    public LiteOrm getLiteOrm(){
        return liteOrm;
    }
}
