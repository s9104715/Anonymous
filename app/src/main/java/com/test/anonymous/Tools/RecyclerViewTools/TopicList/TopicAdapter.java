package com.test.anonymous.Tools.RecyclerViewTools.TopicList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.test.anonymous.R;
import java.util.List;
import at.markushi.ui.CircleButton;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.BaseViewHolder>  {

    private List<ItemTopic> list;//清單

    //點擊效果
    private OnItemClickListener clickListener;
    private OnItemClickListener deleteListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int position);
    }

    public TopicAdapter(List<ItemTopic> list) {
        this.list = list;
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        private TextView topic;
        private CircleButton deleteBtn;

        private BaseViewHolder(View itemView , final OnItemClickListener listener , final OnItemLongClickListener longClickListener , final OnItemClickListener deleteListener) {
            super(itemView);

            topic = itemView.findViewById(R.id.topic);
            deleteBtn = itemView.findViewById(R.id.delete_btn);

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

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deleteListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            deleteListener.onItemClick(position);
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
        View view = LayoutInflater.from(viewGroup.getContext() ).inflate( R.layout.item_topic, viewGroup, false);
        return new BaseViewHolder(view, clickListener , longClickListener , deleteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, final int position) {

        ItemTopic hobby = list.get(position);
        baseViewHolder.topic.setText(hobby.getTopic());
        if(hobby.isSelected()) {
            baseViewHolder.deleteBtn.setVisibility(View.VISIBLE);
        }else {
            baseViewHolder.deleteBtn.setVisibility(View.INVISIBLE);
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

    public List<ItemTopic> getList() {
        return list;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setDeleteListener(OnItemClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void select(int position){
        list.get(position).setSelected(true);
        notifyItemChanged(position);
    }

    public void unSelect(int position){
        list.get(position).setSelected(false);
        notifyItemChanged(position);
    }

    public void add(ItemTopic itemTopic){
        list.add(itemTopic);
        notifyItemInserted(list.size() - 1);
    }

    public void delete(int position){
        list.remove(position);
        notifyItemRemoved(position);
    }
}

