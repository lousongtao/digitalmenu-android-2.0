package com.shuishou.digitalmenu.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuishou.digitalmenu.InstantValue;
import com.shuishou.digitalmenu.R;
import com.shuishou.digitalmenu.bean.DishConfig;
import com.shuishou.digitalmenu.bean.Flavor;
import com.shuishou.digitalmenu.io.IOOperator;
import com.shuishou.digitalmenu.ui.components.ChangeLanguageTextView;
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChoosedDish cd = choosedFoods.get(position);
        holder.tvAmount.setText(cd.getAmount()+InstantValue.NULLSTRING);

        holder.tvAddtionalRequirements.setTxtFirstLanguageName(getAdditionalRequirementsFirstLanguage(cd));
        holder.tvAddtionalRequirements.setTxtSecondLanguageName(getAdditionalRequirementsSecondLanguage(cd));
        holder.tvAddtionalRequirements.show(mainActivity.getLanguage());
//        holder.foodImage.setBackground(IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL + cd.getDish().getPictureName()));
        holder.imgDishPicture.setImageDrawable(IOOperator.getDishImageDrawable(mainActivity.getResources(), InstantValue.LOCAL_CATALOG_DISH_PICTURE_SMALL + cd.getDish().getPictureName()));
        holder.tvFoodName.setTxtFirstLanguageName(cd.getFirstLanguageName());
        holder.tvFoodName.setTxtSecondLanguageName(cd.getSecondLanguageName());
        holder.tvFoodName.show(mainActivity.getLanguage());
        holder.tvFoodPrice.setText(InstantValue.DOLLAR + String.format(InstantValue.FORMAT_DOUBLE_2DECIMAL, cd.getPrice()));

        holder.plusImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION, position);
        holder.minusImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION, position);
        holder.flavorImage.setTag(R.id.CHOOSEDDISHIMAGEBUTTON_TAG_KEY_POSITION, position);
        ChoosedFoodClickListener listener = new ChoosedFoodClickListener(mainActivity);
        holder.plusImage.setOnClickListener(listener);
        holder.minusImage.setOnClickListener(listener);
        holder.flavorImage.setOnClickListener(listener);
        holder.flavorImage.setVisibility(cd.getDish().isAllowFlavor() ? View.VISIBLE : View.GONE);
//        //如果dish不可以自动合并, 隐藏掉加号
//        holder.plusImage.setVisibility(cd.getDish().isAutoMergeWhileChoose() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return choosedFoods.size();
    }

    public String getAdditionalRequirementsFirstLanguage(ChoosedDish cd){
        StringBuffer sb = new StringBuffer();
        if (cd.getDishConfigList() != null && !cd.getDishConfigList().isEmpty()){
            for ( DishConfig si: cd.getDishConfigList()) {
                sb.append(si.getFirstLanguageName() + InstantValue.SLLASHSTRING);
            }
        }
        if (cd.getFlavorList() != null && !cd.getFlavorList().isEmpty()){
            for (Flavor f: cd.getFlavorList()){
                sb.append(f.getFirstLanguageName() + InstantValue.SLLASHSTRING);
            }
        }
        return sb.toString();
    }

    public String getAdditionalRequirementsSecondLanguage(ChoosedDish cd){
        StringBuffer sb = new StringBuffer();
        if (cd.getDishConfigList() != null && !cd.getDishConfigList().isEmpty()){
            for ( DishConfig si: cd.getDishConfigList()) {
                sb.append(si.getSecondLanguageName() + InstantValue.SLLASHSTRING);
            }
        }
        if (cd.getFlavorList() != null && !cd.getFlavorList().isEmpty()){
            for (Flavor f: cd.getFlavorList()){
                sb.append(f.getSecondLanguageName() + InstantValue.SLLASHSTRING);
            }
        }
        return sb.toString();
    }

    public MainActivity getMainActivity(){
        return mainActivity;
    }
}
