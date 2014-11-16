package au.id.richardg.notifox;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by Richard on 10/11/2014.
 */
public class NotificationListener extends NotificationListenerService{

    private final static UUID PEBBLE_APP_UUID = UUID.fromString("42a10d1a-fd03-4d98-b296-2737d083b3ad");

    @Override
    public void  onCreate() {

        Log.d("NotificationListener", "Created");

        PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(Context context, final int transactionId, PebbleDictionary data) {

                PebbleKit.sendAckToPebble(getApplicationContext(),transactionId);

                Log.d("NotificationListener", "Received");

                Log.d("NotificationListener", "received: " + data.getInteger(0) + " from " + data.toJsonString());
                //Log.i("weee", "Received value=" + data.getUnsignedIntegerAsLong(0) + " for key: 0");


                sendCurrentNotifications();


            }
        });
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

    private void sendCurrentNotifications(){

        StatusBarNotification[] currentNotifications = getActiveNotifications();



        PebbleDictionary newData = new PebbleDictionary();
        newData.addString(0, currentNotifications[0].getNotification().extras.getString(Notification.EXTRA_TITLE));
        PebbleKit.sendDataToPebble(getApplicationContext(),PEBBLE_APP_UUID,newData);
    }

}
