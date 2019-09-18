package com.test.anonymous.Tools.RecyclerViewTools.FriendsList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;
import com.test.anonymous.R;
import com.test.anonymous.Tools.MyTime;

import java.util.Collections;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.BaseViewHolder> {

    private List<ItemFriends> list;//清單
    private BaseViewHolder viewHolder;

    //點擊效果
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public FriendsAdapter(List<ItemFriends> list) {
        this.list = list;
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView selfie;
        private TextView nameTV , lastLineTV , unreadLineNumTV , lastTimeTV;
        private RelativeLayout unreadLineNum;

        private BaseViewHolder(View itemView , final OnItemClickListener listener) {
            super(itemView);

            selfie = itemView.findViewById(R.id.selfie);
            nameTV = itemView.findViewById(R.id.name_TV);
            lastLineTV = itemView.findViewById(R.id.last_line_TV);
            lastTimeTV = itemView.findViewById(R.id.last_time_TV);
            unreadLineNum = itemView.findViewById(R.id.unRead_line_num);
            unreadLineNumTV = itemView.findViewById(R.id.unRead_line_num_TV);

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
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_friends, viewGroup, false);
        viewHolder= new BaseViewHolder(view, clickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {

        final ItemFriends currentItem = list.get(position);
        //load friends info
        Picasso.get().load(currentItem.getSelfiePath())
                //圖片使用最低分辨率,降低使用空間大小
                .fit()
                .centerCrop()
                .into(baseViewHolder.selfie);//取得大頭貼
        baseViewHolder.nameTV.setText(currentItem.getName());
        baseViewHolder.lastLineTV.setText(currentItem.getLastLine());
        baseViewHolder.lastTimeTV.setText(new MyTime().getFormatTime(currentItem.getLastTime(), "a hh:mm"));
        if(currentItem.getUnreadLineNum() > 0){
            baseViewHolder.unreadLineNumTV.setText(String.valueOf(currentItem.getUnreadLineNum()));
            baseViewHolder.unreadLineNum.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void moveItemToTop(int position , ItemFriends itemFriends){
        list.remove(position);
        notifyItemRemoved(position);
        list.add(0 , itemFriends);
        notifyItemInserted(0);
    }
}
