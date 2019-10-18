package com.test.anonymous.Tools.RecyclerViewTools.HobbyList;

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

public class HobbyAdapter extends RecyclerView.Adapter<HobbyAdapter.BaseViewHolder> {

    private List<ItemHobby> list;//清單

    //點擊效果
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public HobbyAdapter(List<ItemHobby> list) {
        this.list = list;
        //穩定Item順序
        setHasStableIds(true);
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTV;
        private EditText otherET;
        private ImageView okImg;

        private BaseViewHolder(View itemView , final OnItemClickListener listener) {
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

        public TextView getNameTV() {
            return nameTV;
        }

        public EditText getOtherET() {
            return otherET;
        }

        public ImageView getOkImg() {
            return okImg;
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_init_user_data, viewGroup, false);
        return new BaseViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, final int position) {

        ItemHobby hobby = list.get(position);
        if(!hobby.isOther()){
            //TextView
            baseViewHolder.nameTV.setText(hobby.getHobby());
        }else {
            //EditText
            baseViewHolder.nameTV.setVisibility(View.GONE);
            baseViewHolder.otherET.setVisibility(View.VISIBLE);
            baseViewHolder.otherET.setText(list.get(position).getOtherHobby());
            baseViewHolder.getOtherET().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    list.get(position).setOtherHobby(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
        //is selected
        if(hobby.isSelected()){
            baseViewHolder.okImg.setVisibility(View.VISIBLE);
        }else {
            baseViewHolder.okImg.setVisibility(View.GONE);
        }
        //add view to list

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

    public List<ItemHobby> getList() {
        return list;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void select(int position){
        list.get(position).setSelected(true);
        notifyItemChanged(position);
    }

    public void unSelect(int position){
        list.get(position).setSelected(false);
        notifyItemChanged(position);
    }

    public void selectOther(String otherHobby){
        list.get(list.size() -1).setSelected(true);
        list.get(list.size() -1).setOtherHobby(otherHobby);
        notifyItemChanged(list.size() -1);
        this.add();
    }


    public void add(){
        list.add(new ItemHobby("" , false));
        notifyItemInserted(list.size()-1);
    }

    public void add(ItemHobby itemHobby){
        list.add(itemHobby);
        notifyItemInserted(list.size() -1);
    }

    //position之後的所有物件(不包括position)都會被刪除
    public void delete(int position){
        for(int i = list.size()-1 ; i > position ; i --){
            list.remove(i);
            notifyItemRemoved(i);
        }
        list.get(position).setSelected(false);
        notifyItemChanged(position);
    }
    //初始化其他選項:刪除position之後的物件(不包括position) position物件則clear editText
    public void init(int position){
        for(int i = list.size()-1 ; i > position ; i --){
            list.remove(i);
            notifyItemRemoved(i);
        }
        list.get(position).setOtherHobby("");
        list.get(position).setSelected(false);
        notifyItemChanged(position);
    }

    public void clear(){
        for(int i = list.size()-1 ; i >= 0 ; i--){
            list.remove(i);
            notifyItemRemoved(i);
        }
    }
}
