package com.test.anonymous.Tools.RecyclerViewTools.InvitationList;

import java.util.Comparator;

public class ItemInvitationComparator implements Comparator<ItemInvitation> {
    @Override
    public int compare(ItemInvitation o1, ItemInvitation o2) {
        return (o1.getTime().compareTo(o2.getTime()));
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
