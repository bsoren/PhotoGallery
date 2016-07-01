package com.bijay.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationReceiver.class.getSimpleName();

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG,"received result code: "+getResultCode());

        if(getResultCode() != Activity.RESULT_OK){
            // A foreground activity cancelled the broadcast
            return;
        }

        int requestCode =  intent.getIntExtra(PollService.REQUEST_CODE,0);
        Notification notification = (Notification)
                intent.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(requestCode,notification);

    }
}
