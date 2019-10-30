package com.test.anonymous.Tools.RecyclerViewTools.InvitationList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.test.anonymous.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.BaseViewHolder> {

    private List<ItemInvitation> list;//清單
    private BaseViewHolder viewHolder;

    //點擊效果
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int position);
    }

    public InvitationAdapter(List<ItemInvitation> list) {
        this.list = list;
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView selfie;
        private TextView nameTV , info , distance;
        private RelativeLayout newInvitation;

        private BaseViewHolder(View itemView , final OnItemClickListener listener , final OnItemLongClickListener longClickListener) {
            super(itemView);

            selfie = itemView.findViewById(R.id.selfie);
            nameTV = itemView.findViewById(R.id.name_TV);
            info = itemView.findViewById(R.id.info);
            distance = itemView.findViewById(R.id.distance);
            newInvitation = itemView.findViewById(R.id.new_invitation);

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
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            longClickListener.onItemLongClick(position);
                        }
                    }
                    return true;
                }
            });
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_invitation, viewGroup, false);
        viewHolder= new BaseViewHolder(view, clickListener , longClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {

        final ItemInvitation currentItem = list.get(position);
        //load friends info
        Picasso.get().load(currentItem.getSelfiePath())
                //圖片使用最低分辨率,降低使用空間大小
                .fit()
                .centerCrop()
                .into(baseViewHolder.selfie);//取得大頭貼
        baseViewHolder.nameTV.setText(currentItem.getName());
        baseViewHolder.info.setText(currentItem.getInfo());
        baseViewHolder.distance.setText(currentItem.getDistance());
        if(!currentItem.isRead()){
            baseViewHolder.newInvitation.setVisibility(View.VISIBLE);
        }else {
            baseViewHolder.newInvitation.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    //如果讀取單筆邀請則newInvitation gone
    public void read(int position){
        list.get(position).setRead(true);
        notifyItemChanged(position);
    }
}
