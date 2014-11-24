package au.id.richardg.notifox;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.Map;
import java.util.UUID;

/*
 *
 * Parses notifications to be sent to Pebble
 *
 */

public class MessageBuilder {

    private final static String TAG = "MessageBuilder";

    private MessageInterface mMessageInterface;
    private UUID mPebbleAppUuid;

    private Map<Integer, StatusBarNotification> uniqueNotifications;
    private static int uniqueIDCounter = 0;

    public MessageBuilder(MessageInterface messageInterface, UUID pebbleAppUuid) {
        mMessageInterface = messageInterface;
        mPebbleAppUuid = pebbleAppUuid;
    }


    //124 byte max - 8 byte overhead =
    //116 bytes but can go up to 136?


    //icon 96 bytes (16 rows) 3 messages
    //image 108 bytes (6 rows) 24 messages
    //126 bytes 20 messages?

    public void activeNotificationsRequest(Context context, StatusBarNotification[] activeNotifications, int currentPos){
        Log.i(TAG, "activeNotificationsRequest()");

        //make sure all notifications have unique ids
        updateUniqueIDs(activeNotifications);

        //sort the notifications
        activeNotifications = sortNotifications(activeNotifications);

        //find most suitable position to send back


        //first notif in range
        int firstPos = currentPos;
        if(currentPos+2 >= activeNotifications.length)
            firstPos = activeNotifications.length - 4;//eg 10-4 = 6 (6,7,8,9)

        //last notif in range
        int lastPos = firstPos + 4;
        if(lastPos > activeNotifications.length)
            lastPos = activeNotifications.length;

        //send ids of range
        int[] ids = new int[4];
        for(int i = firstPos; i < firstPos+4; i++) {
            ids[0] = getUniqueID(activeNotifications[i]);
        }
        sendIds(context, ids, firstPos, activeNotifications.length);

        //send notifs in range
        for(int i = firstPos; i < firstPos+4; i++){
            sendNotification(context, activeNotifications[i]);
        }
    }

    private StatusBarNotification[] sortNotifications(StatusBarNotification[] notifications){
        //TODO
        return notifications;
    }

    private void sendIds(Context context, int[] ids, int pos, int count){
        //TODO

        PebbleDictionary sizeTest = new PebbleDictionary();

        byte[] bytes = new byte[136];
        for(int i = 0; i < 136; i++)
            bytes[i] = (byte)i;
        sizeTest.addBytes(0,bytes);

        mMessageInterface.send(context, sizeTest);
    }

    private void sendNotification(Context context, StatusBarNotification sbn){
        //TODO
    }

    private int getUniqueID(StatusBarNotification sbn){

        //find the unique key
        int id = -1;
        boolean found = false;
        for(Map.Entry<Integer,StatusBarNotification> entry : uniqueNotifications.entrySet()){
            if(sbn.getPackageName().contentEquals(entry.getValue().getPackageName()) && sbn.getId() == entry.getValue().getId()){
                found = true;
                id = entry.getKey();
                entry.setValue(sbn);
            }
        }

        //if not found, create it
        if(!found){
            //find a new unique key
            while(uniqueNotifications.containsKey(uniqueIDCounter))
                uniqueIDCounter++;

            uniqueNotifications.put(uniqueIDCounter,sbn);
            id = uniqueIDCounter;
        }

        return id;
    }

    private void updateUniqueIDs(StatusBarNotification[] notifications){

        //for each notification
        for(int i = 0; i < notifications.length; i++) {
            //find or create
            getUniqueID(notifications[i]);
        }

        //clean up any removed notifications
        cleanUniqueIDs(notifications);
    }

    private void cleanUniqueIDs(StatusBarNotification[] notifications){

        //for all saved notifications
        for(Map.Entry<Integer,StatusBarNotification> entry : uniqueNotifications.entrySet()){
            //see if it is active
            boolean found = false;
            for(int i = 0; i < notifications.length; i++){
                if(notifications[i].getPackageName().contentEquals(entry.getValue().getPackageName()) && notifications[i].getId() == entry.getValue().getId()){
                    found = true;
                }
            }

            //otherwise remove it
            if(!found)
                uniqueNotifications.remove(entry.getKey());
        }
    }


}
