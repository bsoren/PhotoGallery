package com.bijay.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class PhotoPageActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }

    public static Intent newIntent(Context context, Uri photoPageUri){
        Intent i = new Intent(context,PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }
}
