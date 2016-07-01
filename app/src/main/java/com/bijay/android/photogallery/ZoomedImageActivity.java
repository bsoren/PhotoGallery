package com.bijay.android.photogallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

/**
 * Created by bsoren on 06-Jun-16.
 */
public class ZoomedImageActivity extends AppCompatActivity {

    private static final String TAG = ZoomedImageActivity.class.getSimpleName();

    private LinearLayout zoomedImageLayout;
    private ImageView mImageView;
    private FrameLayout mFrameLayout;


    private void startSharedElementTransition(Activity activity, Intent intent,
                                              View sourceView){

        ViewCompat.setTransitionName(sourceView,"full_screen_image");

        ActivityOptionsCompat options =  ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity,sourceView,"full_screen_image");

        activity.startActivity(intent, options.toBundle());
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        Log.d(TAG,"ZoomedImageActivity oncreate");

        super.onCreate(savedInstanceState);

        Bundle extras =  getIntent().getExtras();
        final String imageUrl = extras.getString(PhotoGalleryFragment.IMAGE_DRAWABLE);

        setContentView(R.layout.zoomed_image);
        ImageView imageView = (ImageView) findViewById(R.id.zoomed_image);
        Picasso.with(this)
                .load(imageUrl)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.image_loading_animation)
                .into(imageView);

        /*
       zoomedImageLayout = (LinearLayout) findViewById(R.id.zoomed_image_layout);
        zoomedImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
        */

        mImageView = (ImageView) findViewById(R.id.zoomed_image);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(v instanceof ImageView){
                    Log.i(TAG,"imageView onTouch");
                    Intent intent = new Intent(ZoomedImageActivity.this,FullScreenImage.class);
                    intent.putExtra(PhotoGalleryFragment.IMAGE_DRAWABLE,imageUrl);
                    startSharedElementTransition(ZoomedImageActivity.this,intent,mImageView);
                    return true;
                }
                Log.i(TAG,"frameLayout onTouch");
                return false;
            }
        });
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG,"frameLayout onTouch 2");
                finish();
                return true;
            }
        });


    }
}
