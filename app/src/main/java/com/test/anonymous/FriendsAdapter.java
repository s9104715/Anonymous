package com.test.anonymous;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.BaseViewHolder> {

    private List<ItemFriends> list;//清單
    private BaseViewHolder viewHolder;

    //firestore
    private static FirebaseAuth auth;
    private static FirebaseFirestore firestore;

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
        private TextView nameTV;

        private BaseViewHolder(View itemView , final OnItemClickListener listener) {
            super(itemView);

            selfie = itemView.findViewById(R.id.selfie);
            nameTV = itemView.findViewById(R.id.name_TV);

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

        //firebase初始化
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        final ItemFriends currentItem = list.get(position);
        //load friends info
        Picasso.get().load(currentItem.getSelfiePath())
                //圖片使用最低分辨率,降低使用空間大小
                .fit()
                .centerCrop()
                .into(baseViewHolder.selfie);//取得大頭貼
        baseViewHolder.nameTV.setText(currentItem.getName());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
