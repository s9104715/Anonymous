package com.test.anonymous.Tools.RecyclerViewTools.PosUserList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.test.anonymous.R;
import com.test.anonymous.Tools.TextProcessor;

import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerPosUserAdapter extends RecyclerView.Adapter<RecyclerPosUserAdapter.BaseViewHolder> {

    private List<ItemPosUserRecycler> list;//清單
    private BaseViewHolder viewHolder;

    //點擊效果
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public RecyclerPosUserAdapter(List<ItemPosUserRecycler> list) {
        this.list = list;
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView selfie;
        private TextView nameTV , distanceTV;

        private BaseViewHolder(View itemView , final OnItemClickListener listener) {
            super(itemView);

            selfie = itemView.findViewById(R.id.selfie);
            nameTV = itemView.findViewById(R.id.name_TV);
            distanceTV = itemView.findViewById(R.id.distance);

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
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_pos_search_list, viewGroup, false);
        viewHolder= new BaseViewHolder(view, clickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {

        final ItemPosUserRecycler currentItem = list.get(position);
        Picasso.get().load(currentItem.getSelfiePath())
                //圖片使用最低分辨率,降低使用空間大小
                .fit()
                .centerCrop()
                .into(baseViewHolder.selfie);//取得大頭貼
        baseViewHolder.nameTV.setText(currentItem.getName());
        baseViewHolder.distanceTV.setText(new TextProcessor().doubleFormat("#.##" , currentItem.getDistance()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void addItem(ItemPosUserRecycler item){
        list.add(item);
        notifyItemInserted(list.size()-1);
    }

    public void removeAll(){
        for(int i = list.size()-1 ; i >=0 ; i--){
            list.remove(i);
            notifyItemRemoved(i);
        }
    }

//    //監聽新訊息時所用
//    public void moveItemToTop(int position , ItemFriends itemFriends){
//        list.remove(position);
//        notifyItemRemoved(position);
//        list.add(0 , itemFriends);
//        notifyItemInserted(0);
//    }
//    //更改朋友名字所用
//    public void updateItem(int position , ItemFriends itemFriends){
//        list.remove(position);
//        notifyItemRemoved(position);
//        list.add(position , itemFriends);
//        notifyItemInserted(position);
//    }
//    //封鎖、離開聊天室所用
//    public void removeItem(int position){
//        list.remove(position);
//        notifyItemRemoved(position);
//    }
}
