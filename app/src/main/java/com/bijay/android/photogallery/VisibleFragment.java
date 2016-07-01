package com.bijay.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by bsoren on 20-Jun-16.
 */
public class VisibleFragment extends Fragment {
    private static final String TAG = VisibleFragment.class.getSimpleName() ;

    @Override
    public void onStart() {
        Log.i(TAG,"onStart");
        super.onStart();
        IntentFilter filter =  new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,
                PollService.PERM_PRIVATE, null);
        Log.i(TAG,"registered receiver");
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
        Log.i(TAG,"Unregistered receiver");
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getActivity(),
//                    "Got a broadcast: "+intent.getAction(),
//                    Toast.LENGTH_SHORT)
//                .show();
            Log.i(TAG,"cancelling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
