package com.test.anonymous.Tools;

import java.text.DecimalFormat;

public class TextProcessor {

    public TextProcessor() {
    }

    //文字自動換行
    public String textAutoWrap(String text , int textLength){

        StringBuffer sb = new StringBuffer(text);
        if(sb.length() > textLength){
            //text超過16個字
            for(int i = textLength ; i < sb.length() ; i += textLength){
                //每16個字換行
                sb.insert(i , "\n");
            }
        }
        return sb.toString();
    }

    //文字過長則format
    public String textFormat(String text , int textLength){

        StringBuffer sb = new StringBuffer(text);
        if(sb.length() > textLength){
            //text超過16個字
            sb.delete(textLength ,sb.length());
            sb.insert(sb.length() , "......");
        }
        return sb.toString();
    }

    public String doubleFormat(String pattern , Double d){
        return new DecimalFormat(pattern).format(d);
    }
}
