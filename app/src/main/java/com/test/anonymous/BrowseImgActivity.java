package com.test.anonymous;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

public class BrowseImgActivity extends AppCompatActivity {

    private ImageView img;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_browse_img);

        img = findViewById(R.id.img);

        loadImg();
    }

    private void loadImg(){
        Glide.with(this)
                .load(getIntent().getExtras().get("picUri"))
                .centerCrop()
                .fitCenter()
                .into(img);
    }
}
