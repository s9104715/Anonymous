package com.test.anonymous.Tools;

import android.content.Context;
import cc.cloudist.acplibrary.ACProgressFlower;

public class LoadingProcessDialog {

    private ACProgressFlower loadingPD;

    public LoadingProcessDialog(int direction, int themeColor, boolean cancelable, boolean canceledOnTouchOutside, Context ctx) {
        this.loadingPD = new ACProgressFlower.Builder(ctx)
                .direction(direction)
                .themeColor(themeColor)
                .build();
        this.loadingPD.setCancelable(cancelable);
        this.loadingPD.setCanceledOnTouchOutside(canceledOnTouchOutside);
    }

    public LoadingProcessDialog  show(){
        this.loadingPD.show();
        return this;
    }

    public void dismiss(){
     this.loadingPD.dismiss();
    }
}
