package au.id.richardg.notifox;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.DisplayMetrics;

import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/*
 *
 * Parses notifications to be sent to Pebble
 *
 */

public class MessageBuilder {

    private MessageInterface mMessageInterface;
    private UUID mPebbleAppUuid;

    public MessageBuilder(MessageInterface messageInterface, UUID pebbleAppUuid) {
        mMessageInterface = messageInterface;
        mPebbleAppUuid = pebbleAppUuid;
    }

    public void sendActiveNotifications(Context context, StatusBarNotification[] activeNotifications){

        PebbleDictionary sizeTest = new PebbleDictionary();

        //124 byte max - 8 byte overhead =
        //116 bytes but can go up to 136?


        //icon 96 bytes (16 rows) 3 messages
        //image 108 bytes (6 rows) 24 messages
        //126 bytes 20 messages?

        byte[] bytes = new byte[136];
        for(int i = 0; i < 136; i++)
            bytes[i] = (byte)i;
        sizeTest.addBytes(0,bytes);

        mMessageInterface.send(context, sizeTest);

    }

}
