package com.test.anonymous.Tools;

import java.text.SimpleDateFormat;
import com.google.firebase.Timestamp;

public class MyTime {

    public MyTime() {
    }

    //取得目前timeStamp , 通常用來送往資料庫
    public Timestamp getCurrentTime(){
        return Timestamp.now();
    }
    //將timeStamp轉成特定格式的字串,  通常用來取得資料
    public String getFormatTime(Timestamp timestamp , String pattern){
        return  new SimpleDateFormat(pattern).format(timestamp.toDate());
    }
}
