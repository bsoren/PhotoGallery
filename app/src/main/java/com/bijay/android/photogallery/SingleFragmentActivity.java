package com.bijay.android.photogallery;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by bsoren on 30-May-16.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    @LayoutRes   // to tell android that this method will return valid layout resource.
    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm =  getSupportFragmentManager();
        Fragment crimeFragment = fm.findFragmentById(R.id.fragment_container);

        if(crimeFragment == null){
            crimeFragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container,crimeFragment)
                    .commit();
        }
    }
}
