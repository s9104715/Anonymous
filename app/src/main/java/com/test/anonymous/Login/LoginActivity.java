package com.test.anonymous.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.anonymous.MainActivity;
import com.test.anonymous.R;
import java.util.HashMap;
import java.util.Map;
import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private EditText accET , pwdET;
    private CheckBox checkBox;
    private Button loginBtn , oLogBtn , registBtn;

    private ProgressDialog loginPD;

    //anim
    private Animation fadeInAnim;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //google login
    private SignInButton googleSignInBtn;
    private GoogleApiClient googleApiClient;
    private  static final int REQ_CODE = 9001;

    //facebook login
    private LoginButton fbSignInBtn;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login);

        accET = findViewById(R.id.acc_ET);
        pwdET = findViewById(R.id.pwd_ET);
        checkBox = findViewById(R.id.checkBox);
        loginBtn = findViewById(R.id.login_btn);
        oLogBtn = findViewById(R.id.other_log_btn);
        registBtn = findViewById(R.id.register_btn);
        googleSignInBtn = findViewById(R.id.google_login_btn);
        fadeInAnim = AnimationUtils.loadAnimation(this , R.anim.fade_in);
        fbSignInBtn = findViewById(R.id.facebook_login_btn);

        loginBtn.setOnClickListener(this);
        oLogBtn.setOnClickListener(this);
        registBtn.setOnClickListener(this);
        googleSignInBtn.setOnClickListener(this);

        //hide sign btn
        googleSignInBtn.setClickable(false);
        googleSignInBtn.setVisibility(View.INVISIBLE);
        fbSignInBtn.setClickable(false);
        fbSignInBtn.setVisibility(View.INVISIBLE);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //google login
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                                                                                .requestIdToken(getString(R.string.server_client_id))
                                                                                                                .requestEmail()
                                                                                                                .build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this , this)
                                                                                                                .addApi(Auth.GOOGLE_SIGN_IN_API , signInOptions)
                                                                                                                .build();


        Paper.init(this);//paperDB初始化
        rememberMe();//載入之前記得的帳密
    }

    @Override
    protected void onStart() {
        super.onStart();
        //如果已登入直接進入主頁
        if(auth.getCurrentUser()!= null){
            finish();
            startActivity(new Intent(this , MainActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_btn:

                String acc = accET.getText().toString().trim();
                String pwd = pwdET.getText().toString().trim();

                if(acc.isEmpty()){
                    accET.setError("信箱不能為空");
                    accET.requestFocus();
                    return;
                }
                if(!Linkify.addLinks(accET.getText(), Linkify.EMAIL_ADDRESSES)){
                    accET.setError("信箱格式不正確！");
                    accET.requestFocus();
                    return;
                }if(pwd.isEmpty()){
                    pwdET.setError("密碼不能為空");
                    pwdET.requestFocus();
                    return;
                }
                //登入
                login(acc , pwd);
                break;
            case R.id.register_btn:
                startActivity(new Intent(this , RegisterActivity.class));
                break;
            case R.id.other_log_btn:
                    //anim :  show signInBtn
                if(!googleSignInBtn.isClickable()){
                    googleSignInBtn.setClickable(true);
                    googleSignInBtn.setVisibility(View.VISIBLE);
                    googleSignInBtn.startAnimation(fadeInAnim);
                    fbSignInBtn.setClickable(true);
                    fbSignInBtn.setVisibility(View.VISIBLE);
                    fbSignInBtn.startAnimation(fadeInAnim);
                    //enable fb btn
                    setupFbSignInBtn();
                }

                break;
            case R.id.google_login_btn:
                //log with google
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent , REQ_CODE);//導入onActivityResult
                break;
        }
    }

    //一般帳號登入
    private void login(String acc , String pwd){

        showLoginDialog();
        auth.signInWithEmailAndPassword(acc , pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loginPD.dismiss();
                if (task.isSuccessful()){
                    //信箱驗證信有無通過
                    if(task.getResult().getUser().isEmailVerified()){

                        updateUserAccIsVerify(task.getResult().getUser().getUid());//更新使用者isVerify為true

                        //acc store in paperDB
                        if(checkBox.isChecked()){
                            Paper.book().write("acc" , accET.getText().toString().trim());
                            Paper.book().write("pwd" , pwdET.getText().toString().trim());
                        }
                        finish();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//銷毀目前的activity和其上的activity,並重新創建新的activity
                        startActivity(intent);
                    }else {
                        Toast.makeText(LoginActivity.this,"此帳號尚未啟用，請至信箱中的驗證信啟用帳號",Toast.LENGTH_SHORT ).show();
                    }
                }else {
                    //錯誤的帳號或密碼
                    Toast.makeText(LoginActivity.this,"錯誤的帳號或密碼",Toast.LENGTH_SHORT ).show();
                    Log.e("LoginError：" , task.getException().toString());
                }
            }
        });
    }

    private void showLoginDialog() {
        loginPD = new ProgressDialog(LoginActivity.this);
        loginPD.setTitle("登入");
        loginPD.setMessage("登入中.....");
        loginPD.show();
    }

    //更新使用者的帳號為已啟用帳號
    private void updateUserAccIsVerify(final String UID){
        firestore.collection("User").document(UID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //初次登入時一定會更改verify欄位 , 之後的登入則避免資料的修改以節省寫入次數
                       if(documentSnapshot.getBoolean("verify")!=null){
                           if(!documentSnapshot.getBoolean("verify")){
                               Map<String , Object> update = new HashMap<>();
                               update.put("verify" , true);
                               firestore.collection("User").document(UID).update(update);
                               Log.e("isVerify" , "is true");
                           }
                       }
                    }
                });
    }

    //如果key acc ,pwd有儲存資料的話則載入資料
    private void rememberMe(){
        if(Paper.book().read("acc") != "" && Paper.book().read("pwd") != ""){
            accET.setText((CharSequence) Paper.book().read("acc"));
            pwdET.setText((CharSequence) Paper.book().read("pwd"));
        }
    }

    //已google帳號登入
    private void loginWithGoogleAcc(final GoogleSignInAccount acc){

        AuthCredential credential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loginPD.dismiss();
                if(task.isSuccessful()){

                    Log.e("loginWithGoogleAcc" , "success");

                    //判斷是否為第一次創建 是的話創建新的會員資料
                    firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){//會員已創建

                                        //略過會員資料創建
                                        Log.e("login user" , "success");
                                        finish();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//銷毀目前的activity和其上的activity,並重新創建新的activity
                                        startActivity(intent);

                                    }else {//是新會員

//                                        //上傳照片
//                                        if(acc.getPhotoUrl()!=null){
//                                            storeRef.child("UserSelfie/" + auth.getCurrentUser().getUid() + ".png").putFile(acc.getPhotoUrl());
//                                        }

                                        User user = new User(acc.getDisplayName() , "" , 0 , true , acc.getPhotoUrl().toString() , "GOOGLE_LOGIN");//透過google帳號登入則verify值為true
                                        firestore.collection("User").document(auth.getCurrentUser().getUid()).set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.e("add new user" , "success");
                                                        finish();
                                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//銷毀目前的activity和其上的activity,並重新創建新的activity
                                                        startActivity(intent);
                                                    }
                                                });

                                    }
                                }
                            });

                }else {
                    Log.e("loginWithGoogleAccError" , ""+task.getException());
                    Toast.makeText(LoginActivity.this , "登入失敗" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("ConnectionFailure" , connectionResult.getErrorMessage());
        Toast.makeText(this , "ConnectionFailure" , Toast.LENGTH_LONG).show();
    }
    //fb btn的建置
    private void setupFbSignInBtn(){
        callbackManager = CallbackManager.Factory.create();
        fbSignInBtn.setReadPermissions("email" , "public_profile");
        fbSignInBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //fb登入
                loginWithFBAcc(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                loginPD.dismiss();
                Log.e("fbLoginCancel" , "cancel");
            }

            @Override
            public void onError(FacebookException error) {
                loginPD.dismiss();
                Log.e("fbLoginError" , ""+error);
                Toast.makeText(LoginActivity.this , "登入失敗" , Toast.LENGTH_LONG).show();
            }
        });
    }
    //以fb帳號登入
    private void loginWithFBAcc(final AccessToken token){

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        loginPD.dismiss();
                        if(task.isSuccessful()){

                            Log.e("fbLogin" , "success");

                            //判斷是否為第一次創建 是的話創建新的會員資料
                            firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.exists()){//會員已創建

                                                //略過會員資料創建
                                                Log.e("login user" , "success");
                                                finish();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//銷毀目前的activity和其上的activity,並重新創建新的activity
                                                startActivity(intent);

                                            }else {//是新會員

//                                                //上傳照片
//                                                if(task.getResult().getUser().getPhotoUrl() != null){
//                                                    storeRef.child("UserSelfie/" + auth.getCurrentUser().getUid() + ".png").putFile(task.getResult().getUser().getPhotoUrl());
//                                                }

                                                User user = new User(task.getResult().getUser().getDisplayName() , "" , 0 ,  true , task.getResult().getUser().getPhotoUrl().toString() , "FACEBOOK_LOGIN");//透過fb帳號登入則verify值為true
                                                firestore.collection("User").document(auth.getCurrentUser().getUid()).set(user)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.e("add new user" , "success");
                                                                finish();
                                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//銷毀目前的activity和其上的activity,並重新創建新的activity
                                                                startActivity(intent);
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }else {
                            Log.e("fbLoginFailure" , ""+task.getException());
                            Toast.makeText(LoginActivity.this , "登入失敗" , Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==REQ_CODE){
            //for google login send data to loginWithGoogleAcc using REQ_CODE)
            showLoginDialog();
            try{
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                //google acc data
                GoogleSignInAccount googleAcc = result.getSignInAccount();
                loginWithGoogleAcc(googleAcc);
            }catch (Exception e){
                loginPD.dismiss();
                Log.e("GoogleSignInError：" , e.toString());
            }
        }else {
            //for fb login send data to loginWithFBAcc using callbackManager
            showLoginDialog();
            callbackManager.onActivityResult(requestCode , resultCode , data);
        }
    }
}
