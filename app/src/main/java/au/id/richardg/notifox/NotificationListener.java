package au.id.richardg.notifox;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Richard on 10/11/2014.
 */
public class NotificationListener extends NotificationListenerService{

    @Override
    public  void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("new notif", "woo");

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

}
