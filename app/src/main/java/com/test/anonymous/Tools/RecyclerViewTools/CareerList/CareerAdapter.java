package com.test.anonymous.Tools.RecyclerViewTools.CareerList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.test.anonymous.R;
import java.util.List;

public class CareerAdapter extends RecyclerView.Adapter<CareerAdapter.BaseViewHolder> {

    private List<ItemCareer> list;//清單
    private int selectedItemPosition;
    private boolean selected;//是否已選擇一個項目

    //點擊效果
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public CareerAdapter(List<ItemCareer> list) {
        this.list = list;
        this.selectedItemPosition = -1;
        this.selected = false;
        //穩定Item順序
        setHasStableIds(true);
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTV;
        private EditText otherET;
        private ImageView okImg;

        private BaseViewHolder(View itemView , final OnItemClickListener listener ) {
            super(itemView);

            nameTV = itemView.findViewById(R.id.name_TV);
            otherET = itemView.findViewById(R.id.other_ET);
            okImg = itemView.findViewById(R.id.ok);
            okImg.bringToFront();//移到最上層

            //監聽器設置
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_init_user_data, viewGroup, false);
        return new BaseViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {

        ItemCareer career = list.get(position);
        if(!career.isOther()){
            //TextView
            baseViewHolder.nameTV.setText(career.getCareer());
        }else {
            //EditText
            baseViewHolder.nameTV.setVisibility(View.GONE);
            baseViewHolder.otherET.setVisibility(View.VISIBLE);
            baseViewHolder.otherET.setText(career.getOtherCareer());
            baseViewHolder.otherET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    list.get(list.size() -1).setOtherCareer(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
        //is selected
        if(career.isSelected()) {
            baseViewHolder.okImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //穩定Item順序
    @Override
    public long getItemId(int position) {
        return position;
    }
    //穩定Item順序
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public List<ItemCareer> getList() {
        return list;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void add(ItemCareer career){
        list.add(career);
        notifyItemInserted(list.size() -1);
    }

    //選擇item
    public void select(int position){
        list.get(position).setSelected(true);
        setSelected(true);
        setSelectedItemPosition(position);
        notifyItemChanged(position);
    }

    public void unSelect(int position){
        list.get(position).setSelected(false);
        setSelected(false);
        setSelectedItemPosition(-1);
        notifyItemChanged(position);
    }

    public void selectOther(String otherCareer){
        list.get(list.size() -1).setSelected(true);
        list.get(list.size() -1).setOtherCareer(otherCareer);
        setSelected(true);
        setSelectedItemPosition(list.size() -1);
        notifyItemChanged(list.size() -1);
    }

    public void delete(int position){
        list.remove(position);
        notifyItemRemoved(position);
    }
//    //初始化其他選項
//    public void initOther(){
//        list.get(list.size()-1).setOtherCareer("");
//        notifyItemChanged(list.size()-1);
//    }
}
