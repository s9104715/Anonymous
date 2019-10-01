package com.test.anonymous.Tools;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Keyboard {

    private Object systemService;
    private View view;
    private EditText editText;

    public Keyboard(Object systemService, EditText editText) {
        this.systemService = systemService;
        this.editText = editText;
    }

    public Keyboard(Object systemService, View view) {
        this.systemService = systemService;
        this.view = view;
    }

    public void show(){
        if(editText!=null){
            InputMethodManager imm = (InputMethodManager) this.systemService;
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void close(){
        if(this.view!=null){
            InputMethodManager inputMethodManager = (InputMethodManager)this.systemService;
            inputMethodManager.hideSoftInputFromWindow(this.view.getWindowToken() , 0);
        }
    }
}
