/***********************************************************************************
 * The MIT License (MIT)

 * Copyright (c) 2014 Robin Chutaux

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/
package com.shuishou.digitalmenu.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public class CategoryTabListView extends ListView {

    private String logTag = "TestTime-TabListView";
    private Integer lastOpenPosition = -1;

    public CategoryTabListView(Context context) {
        super(context);
        setOnScrollListener(new OnExpandableLayoutScrollListener());
    }

    public CategoryTabListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnScrollListener(new OnExpandableLayoutScrollListener());
    }

    public CategoryTabListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnScrollListener(new OnExpandableLayoutScrollListener());
    }

    @Override
    public boolean performItemClick(View view, int position, long id) {
        if (lastOpenPosition != position){
            chooseItemByPosition(position);
        } else {

        }

        return super.performItemClick(view, position, id);
    }

    public void chooseItemByPosition(int position){
        long time1 = System.currentTimeMillis();
        lastOpenPosition = position;

        for (int index = 0; index < getChildCount(); ++index) {
            if (index != (position - getFirstVisiblePosition())) {
                CategoryTabLayoutItem currentExpandableLayout = (CategoryTabLayoutItem) getChildAt(index).findViewWithTag(CategoryTabLayoutItem.class.getName());
                currentExpandableLayout.hide();
            }
        }
        View child = getChildAt(position - getFirstVisiblePosition());
        if (child != null){
            CategoryTabLayoutItem expandableLayout = (CategoryTabLayoutItem) child.findViewWithTag(CategoryTabLayoutItem.class.getName());
            if (expandableLayout.isOpened())
                expandableLayout.hide();
            else
                expandableLayout.show();
        }


        long time2 = System.currentTimeMillis();
        Log.d(logTag, "chooseItemByPosition: position = " + position + ", time in milli seconds "+ (time2 - time1));
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        if (!(l instanceof OnExpandableLayoutScrollListener))
            throw new IllegalArgumentException("OnScrollListner must be an OnExpandableLayoutScrollListener");

        super.setOnScrollListener(l);
    }

    public class OnExpandableLayoutScrollListener implements OnScrollListener {
        private int scrollState = 0;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            this.scrollState = scrollState;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (scrollState != SCROLL_STATE_IDLE) {
                for (int index = 0; index < getChildCount(); ++index) {
                    CategoryTabLayoutItem currentExpandableLayout = (CategoryTabLayoutItem) getChildAt(index).findViewWithTag(CategoryTabLayoutItem.class.getName());
                    if (currentExpandableLayout.isOpened() && index != (lastOpenPosition - getFirstVisiblePosition())) {
                        currentExpandableLayout.hideNow();
                    } else if (!currentExpandableLayout.getCloseByUser() && !currentExpandableLayout.isOpened() && index == (lastOpenPosition - getFirstVisiblePosition())) {
                        currentExpandableLayout.showNow();
                    }
                }
            }
        }
    }
}
