package com.test.anonymous.Tools.RecyclerViewTools.TopicList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.test.anonymous.R;

import java.util.List;

public class SendableTopicAdapter extends RecyclerView.Adapter<SendableTopicAdapter.BaseViewHolder> {

    private List<ItemTopic> list;//清單

    //點擊效果
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SendableTopicAdapter(List<ItemTopic> list) {
        this.list = list;
        //穩定Item順序
        setHasStableIds(true);
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private TextView topic;

        private BaseViewHolder(View itemView , final OnItemClickListener listener) {
            super(itemView);

            topic = itemView.findViewById(R.id.topic);

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
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_sendable_topic, viewGroup, false);
        return new BaseViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, final int position) {

        ItemTopic topic = list.get(position);
        baseViewHolder.topic.setText(topic.getTopic());
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

    public List<ItemTopic> getList() {
        return list;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

//    public void select(int position){
//        list.get(position).setSelected(true);
//        notifyItemChanged(position);
//    }
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
