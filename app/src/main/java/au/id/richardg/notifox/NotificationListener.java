package au.id.richardg.notifox;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;

/**
 * Created by Richard on 10/11/2014.
 */
public class NotificationListener extends NotificationListenerService{

    @Override
    public void  onCreate() {

    }


    @Override
    public  void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("1new notif", "1woo");

        boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
        Log.i("test", "Pebble is " + (connected ? "connected" : "not connected"));

        Log.d("done", "done");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

}
