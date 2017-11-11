package com.shuishou.digitalmenu.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.DishChooseSubitem;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.uibean.ChoosedDish;

import java.util.ArrayList;


/**
 * Created by Administrator on 2016/12/25.
 */

public class RecyclerChoosedDishAdapter extends RecyclerView.Adapter<RecyclerChoosedDishAdapter.ViewHolder> {

    private final int resourceId;
    private final ArrayList<ChoosedDish> choosedFoods;
    private final MainActivity mainActivity;
    static class ViewHolder extends RecyclerView.ViewHolder{
        final ChangeLanguageTextView tvFoodName;
//        final FrameLayout foodImage;
        final ImageView imgDishPicture;
        final TextView tvFoodPrice;
        final TextView tvAmount;
        final ChangeLanguageTextView tvAddtionalRequirements;
        final ImageView plusImage;
        final ImageView minusImage;
        final ImageView flavorImage;
        public ViewHolder(View view){
            super(view);
//            foodImage = (FrameLayout) view.findViewById(R.id.choosedfood_image);
            imgDishPicture = (ImageView) view.findViewById(R.id.imgChoosedFood);
            tvFoodName = (ChangeLanguageTextView) view.findViewById(R.id.choosedfood_name);
            tvFoodPrice = (TextView) view.findViewById(R.id.choosedfood_price);
            tvAmount = (TextView) view.findViewById(R.id.choosedfood_amount);
            tvAddtionalRequirements = (ChangeLanguageTextView) view.findViewById(R.id.choosedfood_addtionrequirements);
            plusImage = (ImageView) view.findViewById(R.id.choosedfood_add_icon);
            minusImage = (ImageView) view.findViewById(R.id.choosedfood_minus_icon);
            flavorImage = (ImageView) view.findViewById(R.id.choosedfood_flavor_icon);
            plusImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_ACTION, ChoosedFoodClickListener.IMAGEBUTTON_TAG_KEY_ACTION_PLUS);
            minusImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_ACTION, ChoosedFoodClickListener.IMAGEBUTTON_TAG_KEY_ACTION_MINUS);
            flavorImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_ACTION, ChoosedFoodClickListener.IMAGEBUTTON_TAG_KEY_ACTION_FLAVOR);
        }
    }

    public RecyclerChoosedDishAdapter(MainActivity mainActivity,int resourceId, ArrayList<ChoosedDish> objects){
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
        ChoosedDish cd = choosedFoods.get(position);
        holder.tvAmount.setText(cd.getAmount()+InstantValue.NULLSTRING);

        holder.tvAddtionalRequirements.setTxtEnglish(getAdditionalRequirementsEN(cd));
        holder.tvAddtionalRequirements.setTxtChinese(getAdditionalRequirementsCN(cd));
        holder.tvAddtionalRequirements.show(mainActivity.getLanguage());
//        holder.foodImage.setBackground(IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL + cd.getDish().getPictureName()));
        holder.imgDishPicture.setImageDrawable(IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL + cd.getDish().getPictureName()));
        holder.tvFoodName.setTxtEnglish(cd.getName_en());
        holder.tvFoodName.setTxtChinese(cd.getName_cn());
        holder.tvFoodName.show(mainActivity.getLanguage());
        holder.tvFoodPrice.setText(InstantValue.DOLLAR + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, cd.getPrice()));

        holder.plusImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION, pos);
        holder.minusImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION, pos);
        holder.flavorImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION, pos);
        holder.plusImage.setOnClickListener(ChoosedFoodClickListener.getInstance(mainActivity));
        holder.minusImage.setOnClickListener(ChoosedFoodClickListener.getInstance(mainActivity));
        holder.flavorImage.setOnClickListener(ChoosedFoodClickListener.getInstance(mainActivity));
    }

    @Override
    public int getItemCount() {
        return choosedFoods.size();
    }

    public String getAdditionalRequirementsEN(ChoosedDish cd){
        StringBuffer sb = new StringBuffer();
        if (cd.getDishSubitemList() != null && !cd.getDishSubitemList().isEmpty()){
            for ( DishChooseSubitem si: cd.getDishSubitemList()) {
                sb.append(si.getEnglishName() + InstantValue.SPACESTRING);
            }
        }
        if (cd.getFlavorList() != null && !cd.getFlavorList().isEmpty()){
            for (Flavor f: cd.getFlavorList()){
                sb.append(f.getEnglishName() + InstantValue.SPACESTRING);
            }
        }
        return sb.toString();
    }

    public String getAdditionalRequirementsCN(ChoosedDish cd){
        StringBuffer sb = new StringBuffer();
        if (cd.getDishSubitemList() != null && !cd.getDishSubitemList().isEmpty()){
            for ( DishChooseSubitem si: cd.getDishSubitemList()) {
                sb.append(si.getChineseName() + InstantValue.SPACESTRING);
            }
        }
        if (cd.getFlavorList() != null && !cd.getFlavorList().isEmpty()){
            for (Flavor f: cd.getFlavorList()){
                sb.append(f.getChineseName() + InstantValue.SPACESTRING);
            }
        }
        return sb.toString();
    }
}
