package com.shuishou.digitalmenu.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.shuishou.digitalmenu.ui.MainActivity;

/**
 * Created by Administrator on 2017/1/24.
 */

public class ChangeLanguageTextView extends android.support.v7.widget.AppCompatTextView {
    protected String txtFirstLanguageName;
    protected String txtSecondLanguageName;

    public ChangeLanguageTextView(Context context){
        super(context);
        setTypeface(null, Typeface.BOLD);
        setEllipsize(TextUtils.TruncateAt.END);
    }

    public ChangeLanguageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(null, Typeface.BOLD);
        setEllipsize(TextUtils.TruncateAt.END);
    }

    public ChangeLanguageTextView(Context context, String txtFirstLanguageName, String txtSecondLanguageName){
        super(context);
        setTypeface(null, Typeface.BOLD);
        setEllipsize(TextUtils.TruncateAt.END);
        this.txtFirstLanguageName = txtFirstLanguageName;
        this.txtSecondLanguageName = txtSecondLanguageName;
    }

    public void show(byte language){
        if (language == MainActivity.LANGUAGE_FIRSTLANGUAGE){
            setText(txtFirstLanguageName);
        } else if (language == MainActivity.LANGUAGE_SECONDLANGUAGE){
            setText(txtSecondLanguageName);
        }
    }

    public String getTxtFirstLanguageName() {
        return txtFirstLanguageName;
    }

    public void setTxtFirstLanguageName(String txtFirstLanguageName) {
        this.txtFirstLanguageName = txtFirstLanguageName;
    }

    public String getTxtSecondLanguageName() {
        return txtSecondLanguageName;
    }

    public void setTxtSecondLanguageName(String txtSecondLanguageName) {
        this.txtSecondLanguageName = txtSecondLanguageName;
    }

//    public void
}
