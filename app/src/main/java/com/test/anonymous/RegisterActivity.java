package com.test.anonymous;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity  extends AppCompatActivity implements View.OnClickListener {

    private EditText nameET , accET , pwdET , conPwdET;
    private Button registBtn;

    private ProgressDialog registPD;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_register);

        nameET = findViewById(R.id.name_ET);
        accET = findViewById(R.id.acc_ET);
        pwdET = findViewById(R.id.pwd_ET);
        conPwdET = findViewById(R.id.confirm_pwd_ET);
        registBtn = findViewById(R.id.register_btn);

        registBtn.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()){
            case R.id.register_btn:

                final String name = nameET.getText().toString().trim();
                String emailAcc = accET.getText().toString().trim();
                String pwd = pwdET.getText().toString().trim();
                String conPwd = conPwdET.getText().toString().trim();

                //防呆判斷
                if(name.isEmpty()){
                    nameET.setError("暱稱不能為空");
                    nameET.requestFocus();
                    return;
                } if(emailAcc.isEmpty()) {
                    accET.setError("電子信箱不能為空");
                    accET.requestFocus();
                    return;
                //信箱格式不正確
                }if(!Linkify.addLinks(accET.getText(), Linkify.EMAIL_ADDRESSES)){
                    accET.setError("信箱格式不正確！");
                    accET.requestFocus();
                    return;
                } if(pwd.isEmpty()){
                    pwdET.setError("此欄不能為空");
                    pwdET.requestFocus();
                    return;
                }if(pwd.length() < 8){
                    pwdET.setError("密碼必須大於8碼");
                    pwdET.requestFocus();
                    return;
                } if(conPwd.isEmpty()){
                    conPwdET.setError("此欄不能為空");
                    conPwdET.requestFocus();
                    return;
                }if(!conPwd.equals(pwd)){
                    conPwdET.setError("確認密碼有誤");
                    conPwdET.requestFocus();
                    return;
            }
                //註冊
                register(emailAcc , pwd , name);
                break;
        }
    }

    //註冊
    private void register(String emailAcc , String pwd , final String name){

        showRegistDialog();
        auth.createUserWithEmailAndPassword(emailAcc , pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            final FirebaseUser firebaseUser = task.getResult().getUser();

                            //寄送信箱驗證信
                            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    registPD.dismiss();
                                    if(task.isSuccessful()){

                                        Log.e("EmailVerification：" , "success！");

                                        //創建會員資料
                                        User user = new User(name  , "" , false , getString(R.string.user_default_selfie_url) , "NORMAL_LOGIN");
                                        firestore.collection("User").document(firebaseUser.getUid()).set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Toast.makeText(getApplicationContext() , "信箱驗證信已寄出，請到註冊的信箱啟動帳號" , Toast.LENGTH_LONG).show();
                                                        //登出讓使用者重新登入
                                                        FirebaseAuth.getInstance().signOut();
                                                        startActivity(new Intent(RegisterActivity.this , LoginActivity.class));
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    registPD.dismiss();
                                    Toast.makeText(getApplicationContext() ,   "信箱地址有誤，請重新填寫電子信箱" , Toast.LENGTH_LONG).show();
                                    Log.e("EmailVerificationError：" , e.toString());
                                }
                            });

                        }else {
                            registPD.dismiss();
                            Toast.makeText(getApplicationContext() ,   "註冊失敗！" , Toast.LENGTH_LONG).show();
                            Log.e("RegisterError：" ,  task.getException().toString());
                        }
                    }
                });
    }

    private void showRegistDialog() {
        registPD = new ProgressDialog(RegisterActivity.this);
        registPD.setTitle("註冊帳號");
        registPD.setMessage("註冊中.....");
        registPD.show();
    }
}