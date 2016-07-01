package com.bijay.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

    public static Intent newIntent(Context context){
        Intent i = new Intent(context,PhotoGalleryActivity.class);
        return i;
    }
}