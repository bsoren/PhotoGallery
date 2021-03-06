package com.bijay.android.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.List;

public class PollService extends IntentService {

    private static final String TAG = PollService.class.getSimpleName();
    private static final long POLL_INTERVAL =  1000 * 60;
    public static final String ACTION_SHOW_NOTIFICATION =
            "com.bijay.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE =
            "com.bijay.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";


    public PollService() {
        super("PollService");
    }

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG,"Received an Intent : "+intent);

        int requestID = (int) System.currentTimeMillis();

        if(!isNetworkAvailableAndConnected()){
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId =  QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if(query == null){
            items =  new FlickrFetchr().fetchRecentPhotos();
        }else{
            items =  new FlickrFetchr().searchPhotos(query);
        }

        if(items.size() == 0 ){
            return;
        }

        String resultId =  items.get(0).getId();
        if(resultId.equals(lastResultId)){
            Log.i(TAG, "Got the old result : "+resultId);
        }else{

            Log.i(TAG,"Got a new result : "+resultId);
            Resources resources =  getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);



            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(PhotoGalleryActivity.class);

            stackBuilder.addNextIntent(i);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            Notification notification =  new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(this);

//            notificationManagerCompat.notify(0,notification);
//            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);
            showBackgroundNotification(0,notification);
            Log.i(TAG,"Broadcast Notification sent");
        }

        QueryPreferences.setLastResultId(this,resultId);
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable =  cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected =  isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    public static void setServiceAlarm(Context context, boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(context.ALARM_SERVICE);

        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),POLL_INTERVAL, pi);
        }else{
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryPreferences.setAlarmOn(context,isOn);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private void showBackgroundNotification(int requestCode, Notification notification){
        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE,requestCode);
        intent.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(intent,PERM_PRIVATE,null,null, Activity.RESULT_OK,null,null);
    }

}
