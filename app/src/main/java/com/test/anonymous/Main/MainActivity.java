package com.test.anonymous.Main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.infideap.drawerbehavior.AdvanceDrawerLayout;
import com.squareup.picasso.Picasso;
import com.test.anonymous.Main.FragmentPosSearch.FragmentPosSearch;
import com.test.anonymous.FragmentFriendsList;
import com.test.anonymous.Login.InitUserActivity;
import com.test.anonymous.Main.FragmentRandomChat.FragmentRandomChat;
import com.test.anonymous.Main.FragmentSetting.FragmentSetting;
import com.test.anonymous.Login.LoginActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.Keyboard;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //底部toolbar
    private BottomNavigationView botToolBar;
    private ProgressDialog signOutPD;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    //constant fields
    //螢幕長寬
    public static int WINDOW_WIDTH;
    public static int WINDOW_HEIGHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        firestore =FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        setupBotToolbar();
        //載入螢幕長寬
        WINDOW_WIDTH = getWindowManager().getDefaultDisplay().getWidth();
        WINDOW_HEIGHT = getWindowManager().getDefaultDisplay().getHeight();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUser();
        setupNavigationView((Toolbar) findViewById(R.id.main_toolbar));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
//        if (FragmentRandomChat.editNameView!=null && FragmentRandomChat.editNameView.getVisibility() == View.VISIBLE){
//            new Keyboard(getSystemService(INPUT_METHOD_SERVICE) , this.getCurrentFocus()).close();
//            FragmentRandomChat.chatBtn.setVisibility(View.VISIBLE);
//            Animation move = AnimationUtils.loadAnimation(this , R.anim.move_down);
//            move.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    FragmentRandomChat.coverView.setVisibility(View.GONE);
//                    FragmentRandomChat.editNameView.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//            FragmentRandomChat.editNameView.startAnimation(move);
//        }
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
           signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //測選單建置
    private void setupNavigationView(Toolbar topToolbar){

        AdvanceDrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, topToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.setViewScale(Gravity.START, 0.9f);
        drawer.setRadius(Gravity.START, 35);
        drawer.setViewElevation(Gravity.START, 20);
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
        final FragmentPosSearch chatRoom = new FragmentPosSearch();
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

    private void signOut(){
        //登出
        showSignOutDialog();
        //3秒後登出及結束MainActivity回到LoginActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //fb登出
                LoginManager.getInstance().logOut();
                //google 登出
                GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this , LoginActivity.class));
                finish();
                signOutPD.dismiss();
            }
        } , 3000);
    }

    private void showSignOutDialog(){
        signOutPD = new ProgressDialog(this);
        signOutPD.setCancelable(false);
        signOutPD.setCanceledOnTouchOutside(false);
        signOutPD.setTitle("登出");
        signOutPD.setMessage("登出中.....");
        signOutPD.show();
    }

    //讓新用戶於建立完帳號後更新性別年齡等資料
    private void initUser(){

        firestore.collection("User").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.getString("gender").equals("")){
                            //start initUser
                            startActivity(new Intent(MainActivity.this , InitUserActivity.class));
                            finish();
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
