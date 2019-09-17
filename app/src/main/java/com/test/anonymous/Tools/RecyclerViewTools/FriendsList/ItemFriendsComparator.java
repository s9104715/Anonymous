package com.test.anonymous.Tools.RecyclerViewTools.FriendsList;

import java.util.Comparator;

public class ItemFriendsComparator implements Comparator<ItemFriends> {
    //時間晚的排前面
    @Override
    public int compare(ItemFriends o1, ItemFriends o2) {
        return -(o1.getLastTime().compareTo(o2.getLastTime()));
    }
}
