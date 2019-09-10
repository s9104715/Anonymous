package com.test.anonymous;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;
import com.test.anonymous.Login.LoginActivity;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //底部toolbar
    private BottomNavigationView botToolBar;

    private ProgressDialog signOutPD;

    private AlertDialog initUserAD;//讓新用戶於建立完帳號後更新性別年齡等資料

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        auth = FirebaseAuth.getInstance();
        firestore =FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        setupNavigationView(toolbar);
        setupBotToolbar();
        initUser();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_sign_out) {
            //登出
            showSignOutDialog();
            //3秒後登出及結束MainActivity回到LoginActivity
            dismissSignOutDialog(signOutPD , 3000);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //測選單建置
    private void setupNavigationView(Toolbar topToolbar){

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, topToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);//取得nav的header及其物件
        final TextView nameTV = header.findViewById(R.id.nav_name_TV);
        final CircleImageView selfie = header.findViewById(R.id.nav_selfie);

        //載入帳號資料
        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){

                            nameTV.setText(documentSnapshot.get("name").toString());
                            Picasso.get().load(documentSnapshot.getString("selfiePath"))
                                    //圖片使用最低分辨率,降低使用空間大小
                                    .fit()
                                    .centerCrop()
                                    .into(selfie);//取得大頭貼
                        }
                    }
                });
    }

//    底部toolbar建置
//    換頁時所使用的transaction不能重複使用，每次換頁都要重新宣告一次
    private void setupBotToolbar(){

        final FragmentRandomChat randomChat = new FragmentRandomChat();
        final FragmentChatRoom chatRoom = new FragmentChatRoom();
        final FragmentFriendsList fragmentFriendsList = new FragmentFriendsList();
        final FragmentSetting setting = new FragmentSetting();

        //預設Fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container , randomChat).commit();

        botToolBar = findViewById(R.id.main_bottom_toolbar);
        botToolBar.setSelectedItemId(R.id.example1);
        botToolBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (menuItem.getItemId()){
                    case R.id.example1:
                        transaction.replace(R.id.main_container , randomChat).commit();//進入MainPageFragment
//                        FRAGMENT_INDEX = 0;
                        return true;
                    case R.id.example2:
                        transaction.replace(R.id.main_container , chatRoom).commit();//進入TaskFragment
//                        FRAGMENT_INDEX = 1;
                        return true;
                    case R.id.example3:
                        transaction.replace(R.id.main_container , fragmentFriendsList).commit();//進入ShareFragment
//                        FRAGMENT_INDEX = 2;
                        break;
                    case R.id.example4:
                        transaction.replace(R.id.main_container ,setting).commit();//SettingFragment
//                        FRAGMENT_INDEX = 3;
                        break;
                }
                return true;
            }
        });
    }

    private void showSignOutDialog(){
        signOutPD = new ProgressDialog(this);
        signOutPD.setTitle("登出");
        signOutPD.setMessage("登出中.....");
        signOutPD.show();
    }

    private void dismissSignOutDialog(final ProgressDialog progressDialog , int delayMillis){
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                //登出
                if(LoginManager.getInstance()!=null){//if is fb acc
                    LoginManager.getInstance().logOut();
                }
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this , LoginActivity.class));
                finish();
                progressDialog.dismiss();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, delayMillis);//毫秒計算
    }

    //讓新用戶於建立完帳號後更新性別年齡等資料
    private void initUser(){

        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.getString("gender").equals("")){

                            //使用者尚未更新性別年齡
                            AlertDialog.Builder ADBuider = new AlertDialog.Builder(MainActivity.this)
                                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        @Override
                                        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                                            //override AlertDialog's onBackPressed method
                                            if (keyCode == KeyEvent.KEYCODE_BACK  && keyEvent.getAction() == KeyEvent.ACTION_UP  && !keyEvent.isCanceled()) {
                                               //doing nothing
                                                Log.e("AD's onBackPressed" , "doing nothing");
                                                return true;
                                            }
                                            return false;
                                        }
                                    });
                            final View v = getLayoutInflater().inflate(R.layout.dialog_init_user_setting,null);
                            ADBuider.setView(v);
                            initUserAD = ADBuider.create();
                            initUserAD.show();

                            //load user data
                            final CircleImageView selfie = v.findViewById(R.id.selfie);
                            final TextView nameTV = v.findViewById(R.id.name_TV);
                            firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            nameTV.setText(documentSnapshot.get("name").toString());
                                            Picasso.get().load(documentSnapshot.getString("selfiePath"))
                                                    //圖片使用最低分辨率,降低使用空間大小
                                                    .fit()
                                                    .centerCrop()
                                                    .into(selfie);//取得大頭貼
                                        }
                                    });

                            final RadioGroup genderRG = v.findViewById(R.id.gender_RG);

                            v.findViewById(R.id.confirm_btn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    RadioButton radioButton =  v.findViewById(genderRG.getCheckedRadioButtonId());
                                    String gender = (String) radioButton.getText();

                                    EditText ageET = v.findViewById(R.id.age_ET);
                                    String age = ageET.getText().toString().trim();

                                    if(age.isEmpty()){
                                        ageET.setError(" 此欄位必填");
                                        ageET.requestFocus();
                                        return;
                                    }

                                    //更新使用者資料
                                    Map<String  , Object> update = new HashMap<>();
                                    update.put("gender" , gender);
                                    update.put("age" , Integer.parseInt(age));
                                    firestore.collection("User").document(auth.getCurrentUser().getUid())
                                            .set(update , SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.e("initUser" , "success");
                                            initUserAD.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }

 //產生fb登入所需的金鑰
//    public void printKey(){
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo("com.test.anonymous" , PackageManager.GET_SIGNATURES);
//            for(Signature signature:info.signatures){
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("keyhash" , Base64.encodeToString(md.digest() , Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
}
