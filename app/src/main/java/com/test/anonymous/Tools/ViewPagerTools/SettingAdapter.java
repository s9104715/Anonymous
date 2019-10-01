package com.test.anonymous.Tools.ViewPagerTools;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.test.anonymous.R;
import java.util.List;

public class SettingAdapter extends PagerAdapter {

    private List<ItemSetting> list;
    private View.OnClickListener onClickListener;

    public SettingAdapter(List<ItemSetting> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_setting , viewGroup , false);
        view.setOnClickListener(this.onClickListener);
        View background = view.findViewById(R.id.background);
        TextView nameTV = view.findViewById(R.id.name);

        background.setBackgroundDrawable(list.get(position).getDrawable());
        nameTV.setText(list.get(position).getName());

        viewGroup.addView(view , 0);
        return view;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
