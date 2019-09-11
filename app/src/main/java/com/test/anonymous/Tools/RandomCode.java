package com.test.anonymous.Tools;

import java.util.Random;

public class RandomCode {

    public RandomCode() {
    }

    //產生隨機碼
    public String generateCode(int capacity){
        final String DATA= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder code = new StringBuilder(capacity);

        for(int i = 0 ; i < code.capacity() ; i++){
            code.append(DATA.charAt(random.nextInt(DATA.length())));
        }
        return code.toString();
    }
}
