package com.test.anonymous.Tools.ViewPagerTools;

import android.graphics.drawable.Drawable;

public class ItemSetting {

    private String name;
    private Drawable drawable;

    public ItemSetting(String name, Drawable drawable) {
        this.name = name;
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public android.graphics.drawable.Drawable getDrawable() {
        return drawable;
    }
}
