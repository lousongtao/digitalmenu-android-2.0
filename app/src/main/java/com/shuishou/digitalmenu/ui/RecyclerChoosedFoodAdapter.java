package com.shuishou.digitalmenu.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;

import java.util.ArrayList;


/**
 * Created by Administrator on 2016/12/25.
 */

public class RecyclerChoosedFoodAdapter extends RecyclerView.Adapter<RecyclerChoosedFoodAdapter.ViewHolder> {
    private final int resourceId;
    private final ArrayList<ChoosedDish> choosedFoods;
    private final MainActivity mainActivity;

    static class ViewHolder extends RecyclerView.ViewHolder{
        final ChangeLanguageTextView tvFoodName;
        final LinearLayout foodImage;
        final TextView tvFoodPrice;
        final TextView tvAmount;
        final ChangeLanguageTextView tvAddtionalRequirements;
        final ImageView plusImage;
        final ImageView minusImage;
        public ViewHolder(View view){
            super(view);
            foodImage = (LinearLayout) view.findViewById(R.id.choosedfood_image);
            tvFoodName = (ChangeLanguageTextView) view.findViewById(R.id.choosedfood_name);
            tvFoodPrice = (TextView) view.findViewById(R.id.choosedfood_price);
            tvAmount = (TextView) view.findViewById(R.id.choosedfood_amount);
            tvAddtionalRequirements = (ChangeLanguageTextView) view.findViewById(R.id.choosedfood_addtionrequirements);
            plusImage = (ImageView) view.findViewById(R.id.choosedfood_add_icon);
            minusImage = (ImageView) view.findViewById(R.id.choosedfood_minus_icon);
        }
    }



    public RecyclerChoosedFoodAdapter(MainActivity mainActivity,int resourceId, ArrayList<ChoosedDish> objects){
        choosedFoods = objects;
        this.resourceId = resourceId;
        this.mainActivity = mainActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resourceId, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        ChoosedDish cf = choosedFoods.get(position);
        holder.tvAmount.setText(cf.getAmount()+InstantValue.NULLSTRING);

        holder.tvAddtionalRequirements.setText(cf.getAdditionalRequirements());
        holder.foodImage.setBackground(IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL + cf.getDish().getPictureName()));
        holder.tvFoodName.setTxtEnglish(cf.getName_en());
        holder.tvFoodName.setTxtChinese(cf.getName_cn());
        holder.tvFoodName.show(mainActivity.getLanguage());
        holder.tvFoodPrice.setText(InstantValue.DOLLAR + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, cf.getPrice()));
        holder.plusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.plusDish(pos);
            }
        });

        holder.minusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.minusDish(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return choosedFoods.size();
    }
}
