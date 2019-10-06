package com.test.anonymous.Tools.RecyclerViewTools.PosUserList;

import java.util.Comparator;

public class ItemPosUserComparator implements Comparator<ItemPosUserRecycler> {
    //時間晚的排前面

    @Override
    public int compare(ItemPosUserRecycler o1, ItemPosUserRecycler o2) {
        return (o1.getDistance().compareTo(o2.getDistance()));
    }
}
