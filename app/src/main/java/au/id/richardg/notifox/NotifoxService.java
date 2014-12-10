package au.id.richardg.notifox;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/*
 *
 * Handles incoming events (notifications and incoming pebble messages)
 *
 */

public class NotifoxService extends NotificationListenerService{

    private final static String TAG = "NotifoxService";
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("42a10d1a-fd03-4d98-b296-2737d083b3ad");

    //Incoming message types
    private final static int
            REQUEST_ACTIVE = 0,
            DISMISS_NOTIFICATION = 1,
            TAKE_ACTION = 2;

    //Class instances
    private MessageInterface mMessageInterface;
    private MessageBuilder mMessageBuilder;



    @Override
    public void  onCreate() {
        Log.i(TAG, "onCreate()");

        //create classes
        mMessageInterface = new MessageInterface(PEBBLE_APP_UUID);
        mMessageBuilder = new MessageBuilder(mMessageInterface);

        registerCallbacks(getApplicationContext());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationPosted()");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved()");
    }

    private void onMessageReceived(PebbleDictionary message) {
        Log.i(TAG, "onMessageReceived()");
       switch (message.getInteger(0).intValue()) {
           case REQUEST_ACTIVE:
               Log.i(TAG,"Message: REQUEST_ACTIVE");
               int pos = message.getInteger(1).intValue();
               Log.d(TAG,"Position: " + pos);
               mMessageBuilder.activeNotificationsRequest(getApplicationContext(), getActiveNotifications(), pos);
               break;
           case DISMISS_NOTIFICATION:
               Log.i(TAG,"Message: DISMISS_NOTIFICATION");
               break;
           default:
               Log.w(TAG,"Message: Invalid");
       }
    }

    private void onAckReceived(int transactionId) {
        Log.i(TAG, "onAckReceived()");
        mMessageInterface.success(transactionId);
    }

    private void onNackReceived(int transactionId) {
        Log.i(TAG, "onNackReceived()");
        mMessageInterface.fail(transactionId);
    }

    private void registerCallbacks(final Context appContext){
        Log.i(TAG, "registerCallbacks()");

        PebbleKit.registerReceivedDataHandler(getApplicationContext(), new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(Context context, final int transactionId, PebbleDictionary data) {
                PebbleKit.sendAckToPebble(appContext, transactionId);
                onMessageReceived(data);
            }
        });

        PebbleKit.registerReceivedAckHandler(appContext, new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveAck(Context context, int transactionId) {
                onAckReceived(transactionId);
            }
        });

        PebbleKit.registerReceivedNackHandler(appContext, new PebbleKit.PebbleNackReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveNack(Context context, int transactionId) {
                onNackReceived(transactionId);
            }
        });
    }

}
