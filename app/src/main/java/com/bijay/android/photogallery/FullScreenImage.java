package com.bijay.android.photogallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenImage extends AppCompatActivity {

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        mImageView =  (ImageView)findViewById(R.id.activity_full_screen_image);
        Bundle extras = getIntent().getExtras();
        String imageUrl = extras.getString(PhotoGalleryFragment.IMAGE_DRAWABLE);

        Picasso.with(this)
                .load(imageUrl)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.image_loading_animation)
                .into(mImageView);
    }
}
