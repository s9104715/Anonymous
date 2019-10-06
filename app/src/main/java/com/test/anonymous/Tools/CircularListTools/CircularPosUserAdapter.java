package com.test.anonymous.Tools.CircularListTools;

import android.view.LayoutInflater;
import android.view.View;

import com.jh.circularlist.CircularAdapter;
import com.squareup.picasso.Picasso;
import com.test.anonymous.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CircularPosUserAdapter extends CircularAdapter {

    private List<ItemPosUserCircular> items;       // custom data, here we simply use string
    private LayoutInflater inflater;
    private ArrayList<View> views;     // to store all list item


    public CircularPosUserAdapter(List<ItemPosUserCircular> items, LayoutInflater inflater) {
        this.items = items;
        this.inflater = inflater;
        this.views = new ArrayList<>();

        for(final ItemPosUserCircular item : items){
            View view = inflater.inflate(R.layout.item_pos_search_circular, null);
            Picasso.get().load(item.getSelfiePath())
                    //圖片使用最低分辨率,降低使用空間大小
                    .fit()
                    .centerCrop()
                    .into((CircleImageView)view.findViewById(R.id.selfie));//取得大頭貼
            views.add(view);
        }
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public ArrayList<View> getAllViews() {
        return views;
    }

    @Override
    public View getItemAt(int i) {
        return views.get(i);
    }

    @Override
    public void removeItemAt(int i) {

    }

    @Override
    public void addItem(View view) {
        // add to view list
        views.add(view);
        // // this is necessary to call to notify change
        notifyItemChange();
    }

    public void removeAll(){
        views.clear();
        notifyItemChange();
    }
}
