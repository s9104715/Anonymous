package com.test.anonymous.Tools.RecyclerViewTools.ChatList;


import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.test.anonymous.Main.MainActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.MyTime;
import com.test.anonymous.Tools.TextProcessor;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.BaseViewHolder> {

    private List<ItemChat> list;//清單
    private BaseViewHolder viewHolder;

    //firebase
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    //點擊效果
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    //載入order_item.xml
    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout cl;
        //對方的對話
        private CircleImageView otherSelfie;
        private CardView otherLine;
        private TextView otherText;
        private ImageView otherImg;
        private TextView otherTime , otherImgTime;
        //自己的對話
        private CircleImageView mySelfie;
        private CardView myLine;
        private TextView myText;
        private ImageView myImg;
        private TextView myTime , myImgTime;

        private BaseViewHolder(View itemView , final OnItemClickListener listener) {
            super(itemView);

            cl = itemView.findViewById(R.id.cl);
            otherSelfie = itemView.findViewById(R.id.other_selfie);
            otherLine = itemView.findViewById(R.id.other_line);
            otherText = itemView.findViewById(R.id.other_text);
            otherImg = itemView.findViewById(R.id.other_img);
            otherImgTime = itemView.findViewById(R.id.other_img_time);
            otherTime = itemView.findViewById(R.id.other_time);
            mySelfie = itemView.findViewById(R.id.my_selfie);
            myLine = itemView.findViewById(R.id.my_line);
            myText = itemView.findViewById(R.id.my_text);
            myImg = itemView.findViewById(R.id.my_img);
            myImgTime = itemView.findViewById(R.id.my_img_time);
            myTime = itemView.findViewById(R.id.my_time);

            //監聽器設置
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            };
//            otherSelfie.setOnClickListener(onClickListener);
            otherImg.setOnClickListener(onClickListener);
//            mySelfie.setOnClickListener(onClickListener);
            myImg.setOnClickListener(onClickListener);
        }
    }

    public ChatAdapter(List<ItemChat> list) {
        this.list = list;
        auth = FirebaseAuth.getInstance();
    }

    //Adapter載入order_item.xml方法
    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat ,parent , false);
        viewHolder = new BaseViewHolder(v, clickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final BaseViewHolder holder, final int position) {

        final ItemChat currentItem = list.get(position);

        if(currentItem.getUserUID().equals(auth.getCurrentUser().getUid())){//符合自己的帳號，因此判定為自己的對話
            //隱藏對方的物件
            holder.otherSelfie.setVisibility(View.INVISIBLE);
            holder.otherLine.setVisibility(View.INVISIBLE);
            holder.otherText.setVisibility(View.INVISIBLE);
            holder.otherTime.setVisibility(View.INVISIBLE);
            //firebase設置大頭貼
            Picasso.get()
                    .load(currentItem.getMySelfiePath())
                    //圖片使用最低分辨率,降低使用空間大小
                    .fit()
                    .centerCrop()
                    .into(holder.mySelfie);
            holder.myText.setText(new TextProcessor().textAutoWrap(currentItem.getText() , 16));
            holder.myTime.setText(new MyTime().getFormatTime(currentItem.getTime() , "a hh:mm"));
            if(currentItem.isImg()){
                //載入圖片
                //載入圖片
                holder.myImg.getLayoutParams().width = 2*(MainActivity.WINDOW_WIDTH /3);
                Glide.with(holder.myImg.getContext())
                        .load(currentItem.getImgUrl())
                        .into(holder.myImg);
                holder.myImgTime.setText(new MyTime().getFormatTime(currentItem.getTime() , "a hh:mm"));
                holder.myImgTime.setVisibility(View.VISIBLE);
                holder.myLine.setVisibility(View.INVISIBLE);
                holder.myTime.setVisibility(View.INVISIBLE);
            }
        }else {

            //隱藏自己的物件
            holder.mySelfie.setVisibility(View.INVISIBLE);
            holder.myLine.setVisibility(View.INVISIBLE);
            holder.myText.setVisibility(View.INVISIBLE);
            holder.myTime.setVisibility(View.INVISIBLE);
            //firebase設置大頭貼
            Picasso.get()
                    .load(currentItem.getOtherSelfiePath())
                    //圖片使用最低分辨率,降低使用空間大小
                    .fit()
                    .centerCrop()
                    .into(holder.otherSelfie);
            holder.otherText.setText(new TextProcessor().textAutoWrap(currentItem.getText() , 16));
            holder.otherTime.setText(new MyTime().getFormatTime(currentItem.getTime() , "a hh:mm"));
            if(currentItem.isImg()){
                //載入圖片
                holder.otherImg.getLayoutParams().width = 2*(MainActivity.WINDOW_WIDTH /3);
                Glide.with(holder.otherImg.getContext())
                        .load(currentItem.getImgUrl())
                        .into(holder.otherImg);
                holder.otherImgTime.setText(new MyTime().getFormatTime(currentItem.getTime() , "a hh:mm"));
                holder.otherImgTime.setVisibility(View.VISIBLE);
                holder.otherLine.setVisibility(View.INVISIBLE);
                holder.otherTime.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //新增item
    public void addMsg(ItemChat itemChat){
        list.add(itemChat);
        notifyDataSetChanged();
    }

    public List<ItemChat> getList() {
        return list;
    }

    public BaseViewHolder getViewHolder() {
        return viewHolder;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }
}

