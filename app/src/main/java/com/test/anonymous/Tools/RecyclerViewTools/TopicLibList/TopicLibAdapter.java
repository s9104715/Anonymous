package com.test.anonymous.Tools.RecyclerViewTools.TopicLibList;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.test.anonymous.R;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class TopicLibAdapter extends RecyclerView.Adapter<TopicLibAdapter.BaseViewHolder> {

    private List<ItemTopicLib> list;//清單
    private int textClickedColor;//for download num when has downloaded

    //點擊效果
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public TopicLibAdapter(List<ItemTopicLib> list, int textClickedColor) {
        this.list = list;
        this.textClickedColor = textClickedColor;
        setHasStableIds(true);
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView selfie;
        private TextView nameTV , topicTV , downloadTime;
        private ImageView downloadBtn;

        private BaseViewHolder(View itemView , final OnItemClickListener listener) {
            super(itemView);

            selfie = itemView.findViewById(R.id.selfie);
            nameTV = itemView.findViewById(R.id.name_TV);
            topicTV = itemView.findViewById(R.id.topic);
            downloadTime = itemView.findViewById(R.id.download_time);
            downloadBtn = itemView.findViewById(R.id.download_btn);

            //監聽器設置
            downloadBtn.setOnClickListener(new View.OnClickListener() {
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
    public TopicLibAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_topic_lib, viewGroup, false);
        return new BaseViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, final int position) {

        ItemTopicLib topic = list.get(position);
        Picasso.get().load(topic.getSelfiePath())
                //圖片使用最低分辨率,降低使用空間大小
                .fit()
                .centerCrop()
                .into(baseViewHolder.selfie);//取得大頭貼
        baseViewHolder.nameTV.setText(topic.getName());
        baseViewHolder.topicTV.setText(topic.getTopic());
        if(topic.isHasDownloaded()){
            //set download text style
            baseViewHolder.downloadTime.setTextColor(textClickedColor);
            baseViewHolder.downloadTime.setTypeface(null, Typeface.BOLD);
            //set download btn tint
            baseViewHolder.downloadBtn.setColorFilter(textClickedColor);
        }
        baseViewHolder.downloadTime.setText(String.valueOf(topic.getDownloadTime()));
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

    public List<ItemTopicLib> getList() {
        return list;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void download(int position){
        list.get(position).addDownloadTime();
        notifyItemChanged(position);
    }

    public void setHasDownloaded(int position){
        list.get(position).setHasDownloaded(true);
        notifyItemChanged(position);
    }


//
//    public void unSelect(int position){
//        list.get(position).setSelected(false);
//        notifyItemChanged(position);
//    }
//
//    public void add(ItemTopic itemTopic){
//        list.add(itemTopic);
//        notifyItemInserted(list.size() - 1);
//    }
//
//    public void delete(int position){
//        list.remove(position);
//        notifyItemRemoved(position);
//    }
}
