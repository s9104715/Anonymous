//package com.test.anonymous;
//
//import android.app.DatePickerDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.Button;
//import android.widget.DatePicker;
//import android.widget.EditText;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
//public class lost extends AppCompatActivity implements View.OnClickListener {
//
//    private EditText nameET , genderET , birthET;
//    private Button loginBtn;
//    private AlertDialog choseGenderDialog;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.content_login);
//
//        nameET = findViewById(R.id.name_ET);
//        genderET = findViewById(R.id.gender_ET);
//        birthET = findViewById(R.id.birth_ET);
//        loginBtn = findViewById(R.id.login_btn);
//        genderET.setOnClickListener(this);
//        birthET.setOnClickListener(this);
//        loginBtn.setOnClickListener(this);
//        //set ET 不可編輯
//        genderET.setKeyListener(null);
//        birthET.setKeyListener(null);
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.gender_ET:
//
//                //產生選擇性別對話框
//                AlertDialog.Builder ADBuider = new AlertDialog.Builder(this);
//                ADBuider.setTitle("請選擇");
//                View v = getLayoutInflater().inflate(R.layout.dialog_chose_gender,null);
//                ADBuider.setView(v);
//                choseGenderDialog = ADBuider.create();
//                choseGenderDialog.show();
//                break;
//            case R.id.birth_ET:
//
//                int year , month  , date ;
//                if(birthET.getText().toString().isEmpty()){
//                    //DatePickerDialog default值
//                    year = Integer.parseInt( splitDate(getFormattedTime(new Date()) , "/")[0]);
//                    month = Integer.parseInt( splitDate(getFormattedTime(new Date()) , "/")[1]) - 1;
//                    date = Integer.parseInt( splitDate(getFormattedTime(new Date()) , "/")[2]);
//                }else {
//                    //DatePickerDialog 暫存值
//                    year = Integer.parseInt( splitDate(birthET.getText().toString() , "/")[0]);
//                    month = Integer.parseInt( splitDate(birthET.getText().toString() , "/")[1]) - 1;//Integer.parseInt會讓04進位變5 需要減1
//                    date = Integer.parseInt( splitDate(birthET.getText().toString() , "/")[2]);
//                }
//
//                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
//
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.set(Calendar.YEAR, year);
//                        calendar.set(Calendar.MONTH, month);
//                        calendar.set(Calendar.DATE, date);
//                        calendar.set(Calendar.HOUR , 0);
//                        calendar.set(Calendar.MINUTE , 0);
//
//                        birthET.setText(getFormattedTime(calendar.getTime()));
//                    }
//                },  year , month , date).show();
//                break;
//            case R.id.login_btn:
//                startActivity(new Intent(this , MainActivity.class));
//                break;
//        }
//    }
//
//    //choseGenderDialog按鍵方法
//    public void maleOnClick(View view) {
//        genderET.setText("男");
//        choseGenderDialog.dismiss();
//    }
//    //choseGenderDialog按鍵方法
//    public void femaleOnClick(View view) {
//        genderET.setText("女");
//        choseGenderDialog.dismiss();
//    }
//
//    //取得以格式化的時間
//    //yyyy/MM/dd
//    private String getFormattedTime(Date date){
//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
//        return dateFormat.format(date.getTime());
//    }
//
//    //分割 birthET 中的日期字串給DatePickerDialog
//    //regex = 要分割之元素 , 陣列index: 0 = year , 1 = month , 2 = date
//    private String []splitDate(String s , String regex){
//        String[] split = s.split(regex);
//        return split;
//    }
//}
