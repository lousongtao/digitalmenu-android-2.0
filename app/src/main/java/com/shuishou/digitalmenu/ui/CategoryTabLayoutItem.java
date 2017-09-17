package com.shuishou.digitalmenu.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.Category1;
import com.shuishou.digitalmenu.bean.Category2;
import com.shuishou.digitalmenu.bean.Dish;
import com.shuishou.digitalmenu.io.IOOperator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class CategoryTabLayoutItem extends RelativeLayout {
    private String logTag = "TestTime-TabLayoutItem";

    private Boolean isAnimationRunning = false;
    private Boolean isOpened = false;
    private Integer duration;
    private LinearLayout contentLayout;
    private FrameLayout headerLayout;
    private Boolean closeByUser = true;
    private Category1 category1;
    private int lastChoosedCategory2Id;
    private Map<Integer, View> mapCategory2Tabs = new HashMap<>();


    private OnClickListener category2ClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickCategory2Tab((ChangeLanguageTextView)v);
        }
    };
    public CategoryTabLayoutItem(Context context) {
        super(context);
    }

    public CategoryTabLayoutItem(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init(context, attrs);
    }

    public CategoryTabLayoutItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        init(context, attrs);
    }

    public void init(final Context context, Category1 c1) {
        long time1 = System.currentTimeMillis();
        category1 = c1;
        byte language = MainActivity.getInstance().getLanguage();
        headerLayout = (FrameLayout) findViewById(R.id.view_header_category1);
        contentLayout = (LinearLayout) findViewById(R.id.view_content_category2);
        if (isInEditMode())
            return;

        duration = getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        ChangeLanguageTextView headerView = (ChangeLanguageTextView) headerLayout.findViewById(R.id.textview_header_category1);
        headerView.setTxtChinese(c1.getChineseName());
        headerView.setTxtEnglish(c1.getEnglishName());
        headerView.show(language);

        setTag(CategoryTabLayoutItem.class.getName());
        if (c1.getCategory2s() != null){
            for (int i = 0; i< c1.getCategory2s().size(); i++){
                LinearLayout c2View = (LinearLayout) View.inflate(context, R.layout.category2tab_listitem_layout, null);
                final ChangeLanguageTextView c2TextView = (ChangeLanguageTextView)c2View.findViewById(R.id.category2_textview);
                final Category2 c2 = c1.getCategory2s().get(i);
                c2TextView.setTxtChinese(c2.getChineseName());
                c2TextView.setTxtEnglish(c2.getEnglishName());
                c2TextView.show(language);
                c2TextView.setOnClickListener(category2ClickListener);
                c2TextView.setTag(c2.getId());
                contentLayout.addView(c2View);
                mapCategory2Tabs.put(c2.getId(), c2View);
//                DishDisplayFragment frag = buildDishDisplayFragment(context, c2);
//                MainActivity.getInstance().getMapDishDisplayFragments().put(c2.getId(), frag);
            }
        }
        contentLayout.setVisibility(GONE);

        long time2 = System.currentTimeMillis();
        Log.d(logTag, "TabLayoutItem "+ c1.getChineseName()+" init time in milli seconds " + (time2 - time1));
    }

    private DishDisplayFragment buildDishDisplayFragment(Context context, Category2 c2){
        DishDisplayFragment frag = new DishDisplayFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("category2", c2);
        frag.setArguments(bundle);

        return frag;
    }

    private void onClickCategory2Tab(ChangeLanguageTextView v){
        if (lastChoosedCategory2Id != (int)v.getTag()){
            chooseCategory2TabByTag((int)v.getTag());
        }
    }

    private void chooseCategory2TabByTag(int category2Id){
        long time1 = System.currentTimeMillis();
        if (lastChoosedCategory2Id > 0){
            View oldTab = mapCategory2Tabs.get(lastChoosedCategory2Id);
            ImageView arrow = (ImageView)oldTab.findViewById(R.id.imgChoosedArrow);
            arrow.setVisibility(View.INVISIBLE);
        }
        View newTab = mapCategory2Tabs.get(category2Id);
        ImageView arrow = (ImageView) newTab.findViewById(R.id.imgChoosedArrow);
        arrow.setVisibility(View.VISIBLE);
        lastChoosedCategory2Id = category2Id;

        DishDisplayFragment frag = MainActivity.getInstance().getMapDishDisplayFragments().get(category2Id);
        FragmentManager fragMgr = MainActivity.getInstance().getSupportFragmentManager();
        FragmentTransaction trans = fragMgr.beginTransaction();
        trans.replace(R.id.dishdisplayarea_layout, frag);
        trans.commit();

        long time2 = System.currentTimeMillis();
        Log.d(logTag, "TabLayoutItem chooseCategory2TabByTag " + category2Id + " time in milli seconds " + (time2 - time1));
    }

    public void clearLastChoosedCategory2Id(){
        lastChoosedCategory2Id = 0;
    }

    private void expand(final View v) {
        isOpened = true;
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setVisibility(VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = (interpolatedTime == 1) ? LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(duration);
        v.startAnimation(animation);
        //choose first one if there is no selection, otherwise choose the last selection
        if (lastChoosedCategory2Id != 0){
            chooseCategory2TabByTag(lastChoosedCategory2Id);
        } else {
            List<Category2> c2s = category1.getCategory2s();
            if (c2s != null && !c2s.isEmpty()){


                chooseCategory2TabByTag(c2s.get(0).getId());
            }
        }
    }

    private void collapse(final View v) {
        isOpened = false;
        //hide the arrow first
        if (lastChoosedCategory2Id > 0){
            View oldTab = mapCategory2Tabs.get(lastChoosedCategory2Id);
            ImageView arrow = (ImageView)oldTab.findViewById(R.id.imgChoosedArrow);
            arrow.setVisibility(View.INVISIBLE);
        }

        final int initialHeight = v.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                    isOpened = false;
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(duration);
        v.startAnimation(animation);
    }

    public void hideNow() {
        contentLayout.getLayoutParams().height = 0;
        contentLayout.invalidate();
        contentLayout.setVisibility(View.GONE);
        isOpened = false;
    }

    public void showNow() {
        if (!this.isOpened()) {
            contentLayout.setVisibility(VISIBLE);
            this.isOpened = true;
            contentLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            contentLayout.invalidate();
        }
    }

    public Boolean isOpened() {
        return isOpened;
    }

    public void show() {
        if (!isAnimationRunning) {
            expand(contentLayout);
            isAnimationRunning = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAnimationRunning = false;
                }
            }, duration);
        }
    }

    public FrameLayout getHeaderLayout() {
        return headerLayout;
    }

    public LinearLayout getContentLayout() {
        return contentLayout;
    }

    public void hide() {
        if (!isAnimationRunning) {
            collapse(contentLayout);
            isAnimationRunning = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAnimationRunning = false;
                }
            }, duration);
        }
        closeByUser = false;
    }

    public Boolean getCloseByUser() {
        return closeByUser;
    }
}
