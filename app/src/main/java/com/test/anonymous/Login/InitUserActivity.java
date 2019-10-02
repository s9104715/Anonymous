package com.test.anonymous.Login;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;
import com.test.anonymous.Main.MainActivity;
import com.test.anonymous.R;
import com.test.anonymous.Tools.LoadingProcessDialog;

import java.util.HashMap;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import de.hdodenhof.circleimageview.CircleImageView;

public class InitUserActivity extends AppCompatActivity implements View.OnClickListener {

     private ImageView confirmBtn;
     private CircleImageView selfie;
     private EditText ageET;
     private RadioGroup genderRG;

     private AlertDialog confirmAD;

    //Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_init_user);

        confirmBtn = findViewById(R.id.confirm_btn);
        selfie =findViewById(R.id.selfie);
        ageET = findViewById(R.id.age_ET);
        genderRG = findViewById(R.id.gender_RG);

        confirmBtn.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        firestore =FirebaseFirestore.getInstance();

        setupUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.confirm_btn:
                RadioButton radioButton =  findViewById(genderRG.getCheckedRadioButtonId());
                String gender = (String)radioButton.getText();
                String age = ageET.getText().toString().trim();
                if(age.isEmpty()){
                    ageET.setError("年齡不能為空");
                    ageET.requestFocus();
                    return;
                }
                if(Integer.parseInt(age) < 12 || Integer.parseInt(age) > 99){
                    ageET.setError("年齡須介於12~99");
                    ageET.requestFocus();
                    return;
                }
                initUser(gender , age);
                break;
        }
    }

    private void setupUI(){
        firestore.collection("User").document(auth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Picasso.get().load(documentSnapshot.getString("selfiePath"))
                        //圖片使用最低分辨率,降低使用空間大小
                        .fit()
                        .centerCrop()
                        .into(selfie);//取得大頭貼
            }
        });
    }

    private void initUser(final String gender , final String age){
        AlertDialog.Builder ADBuilder = new AlertDialog.Builder(this)
                .setTitle("提醒")
                .setMessage(getString(R.string.gender_setting_msg))
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        confirmAD.dismiss();
                        //upload data
                        //show loadingPD
                        final LoadingProcessDialog loadingPD = new LoadingProcessDialog(ACProgressConstant.DIRECT_CLOCKWISE ,
                                Color.WHITE , false , false , InitUserActivity.this)
                                .show();

                        //更新使用者資料
                        Map<String  , Object> update = new HashMap<>();
                        update.put("gender" , gender);
                        update.put("age" , Integer.parseInt(age));
                        firestore.collection("User").document(auth.getCurrentUser().getUid())
                                .update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                loadingPD.dismiss();
                                //start main
                                startActivity(new Intent(InitUserActivity.this , MainActivity.class));
                                Toast.makeText(InitUserActivity.this , "設定完成" , Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        confirmAD.dismiss();
                    }
                });
        confirmAD = ADBuilder.create();
        confirmAD.show();
    }
}
