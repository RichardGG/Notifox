package au.id.richardg.notifox;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.DisplayMetrics;
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



       // PebbleDictionary newData = new PebbleDictionary();
        //newData.addInt32(0,0);
        //newData.addString(1, currentNotifications[0].getNotification().extras.getString(Notification.EXTRA_TITLE));
        //PebbleKit.sendDataToPebble(getApplicationContext(),PEBBLE_APP_UUID,newData);

        sendIcon(currentNotifications[0]);
    }

    private void sendIcon(StatusBarNotification sbn){

        int ICON_MESSAGE_ROWS = 16;



        try {
            //get appname
            String packagename = sbn.getPackageName();
            //create context (to access resources) may create exception
            Context appContext = createPackageContext(packagename,CONTEXT_IGNORE_SECURITY);
            //get iconID
            int iconID = sbn.getNotification().extras.getInt(Notification.EXTRA_SMALL_ICON);
            //get the drawable as HIGH_DENSITY (48x48px) (32x32 is another option, but may scale poorly)
            Drawable iconDrawable = appContext.getResources().getDrawableForDensity(iconID, DisplayMetrics.DENSITY_HIGH);
            //create empty bitmap
            Bitmap iconBitmap = Bitmap.createBitmap(iconDrawable.getIntrinsicWidth(), iconDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            //set the bitmap as the canvas
            Canvas canvas = new Canvas(iconBitmap);
            //make sure the drawable bounds are the same
            iconDrawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
            //drawable will draw into canvas (which is the bitmap)
            iconDrawable.draw(canvas);

            iconBitmap = Bitmap.createScaledBitmap(iconBitmap, 48, 48, true);

            byte iconData[] = new byte[ICON_MESSAGE_ROWS*(48 / 8)];

            for(int row = 0; row < 48; row++) {
                PebbleDictionary dictionary = new PebbleDictionary();

                int messageRow = row%ICON_MESSAGE_ROWS;
                int dataStart = messageRow * (48/8);
                if(messageRow == 0)
                    iconData = new byte[ICON_MESSAGE_ROWS * (48 / 8)];
                for (int byteNo = 0; byteNo < 6; byteNo++) {


                    iconData[dataStart+byteNo] = 0;
                    for (int bitNo = 0; bitNo < 8; bitNo++) {
                        int color = iconBitmap.getPixel(byteNo * 8+7- bitNo, row);

                        int alpha = Color.alpha(color);
                        int red = Color.red(color);
                        int green = Color.green(color);
                        int blue = Color.blue(color);

                        int average = (int) ((float) ((blue + green + red) / 3)  * ((float) alpha / 255.0));

                        iconData[dataStart + byteNo] += (average < 140) ? 0 : 1;

                        if (bitNo != 7)
                            iconData[dataStart + byteNo] <<= 1;
                    }
                }
                if(messageRow == ICON_MESSAGE_ROWS - 1) {
                    dictionary.addInt8(0, (byte)1);
                    dictionary.addBytes(1, iconData);
                    //dictionary.addInt32(ID, position);
                    dictionary.addInt32(2, row - ICON_MESSAGE_ROWS + 1);
                    //MessageInterface.send(getApplicationContext(), dictionary);
                    PebbleKit.sendDataToPebble(getApplicationContext(),PEBBLE_APP_UUID,dictionary);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
