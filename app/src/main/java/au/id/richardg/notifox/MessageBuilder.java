package au.id.richardg.notifox;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.HashMap;
import java.util.Map;

/*
 *
 * Parses notifications to be sent to Pebble
 *
 */

public class MessageBuilder {

    private final static String TAG = "MessageBuilder";

    private MessageInterface mMessageInterface;

    private Map<Integer, StatusBarNotification> uniqueIDs;
    private static int uniqueIDCounter = 0;

    public MessageBuilder(MessageInterface messageInterface) {
        mMessageInterface = messageInterface;
        uniqueIDs = new HashMap<Integer, StatusBarNotification>();
    }

    //TODO change Map to SparseArray

    //116 bytes but can go up to 136?
    //icon 96 bytes (16 rows) 3 messages
    //image 108 bytes (6 rows) 24 messages

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
        sendUniqueIDs(context, ids, firstPos, activeNotifications.length);

        //send notifs in range
        for(int i = firstPos; i < firstPos+4; i++){
            sendNotification(context, activeNotifications[i]);
        }
    }

    private StatusBarNotification[] sortNotifications(StatusBarNotification[] notifications){
        Log.i(TAG, "sortNotifications()");
        //TODO implement for this

        boolean found = false;
        for(int i = 0; i < 5; i++)
        {
            if(!found) {
                if (topNotifs[i] != null) {
                    if (topNotifs[i].getNotification().priority <= sbnA.getNotification().priority){
                        for(int j = 4; j > i; j--){
                            topNotifs[j] = topNotifs[j-1];
                        }
                        topNotifs[i] = sbnA;
                        found = true;
                    }
                } else {
                    topNotifs[i] = sbnA;
                    found = true;
                }
            }
        }


        return notifications;
    }

    private void sendUniqueIDs(Context context, int[] ids, int pos, int count){
        Log.i(TAG, "sendUniqueIDs()");
        //TODO on pebble side

        PebbleDictionary dictionary = new PebbleDictionary();

        //data
        byte[] bytes = new byte[116];

        //what to put in it
        //metadata
        bytes[0]=1;
        //count
        bytes[1]=(byte)count;
        //start position
        bytes[2]=(byte)pos;
        //4 bytes per int
        for (int idNo = 0; idNo < 4; idNo++){
            for(int i = 0; i < 4; i++) {
                int shiftAmount = (3*8)-(i*8);
                bytes[3+(idNo*(3*8)) + i] = (byte) (ids[idNo] >> (i * 8));
            }
        }
        //total bytes = metadata+count+pos+(4*4) = 19 bytes


        //add to dictionary and send
        dictionary.addBytes(0,bytes);
        mMessageInterface.send(context, dictionary);
    }

    private void sendNotification(Context context, StatusBarNotification sbn){
        Log.i(TAG, "sendNotification()");
        //TODO yeah

        PebbleDictionary dictionary = new PebbleDictionary();

        //data
        byte[] bytes = new byte[116];

        //115 chars max
        //35? title "Your Pebble is Connected Pbbl4lyf"
        //80? content "Hello sir or maddam, I hope you are having a good time. I wish to inform you th"

        //what to put in it
        for(int i = 0; i < 116; i++)
            bytes[i] = (byte)i;

        //add to dictionary and send
        dictionary.addBytes(0,bytes);
        mMessageInterface.send(context, dictionary);
    }

    private int getUniqueID(StatusBarNotification sbn){
        Log.i(TAG, "getUniqueID()");
        //find the unique key
        int id = -1;
        boolean found = false;
        for(Map.Entry<Integer,StatusBarNotification> entry : uniqueIDs.entrySet()){
            if(sbn.getPackageName().contentEquals(entry.getValue().getPackageName()) && sbn.getId() == entry.getValue().getId()){
                found = true;
                id = entry.getKey();
                entry.setValue(sbn);
            }
        }

        //if not found, create it
        if(!found){
            //find a new unique key
            while(uniqueIDs.containsKey(uniqueIDCounter))
                uniqueIDCounter++;

            uniqueIDs.put(uniqueIDCounter, sbn);
            id = uniqueIDCounter;
        }

        return id;
    }

    private void updateUniqueIDs(StatusBarNotification[] notifications){
        Log.i(TAG, "updateUniqueIDs()");
        //for each notification
        for(int i = 0; i < notifications.length; i++) {
            //find or create
            getUniqueID(notifications[i]);
        }

        //clean up any removed notifications
        cleanUniqueIDs(notifications);
    }

    private void cleanUniqueIDs(StatusBarNotification[] notifications){
        Log.i(TAG, "cleanUniqueIDs()");
        //for all saved notifications
        for(Map.Entry<Integer,StatusBarNotification> entry : uniqueIDs.entrySet()){
            //see if it is active
            boolean found = false;
            for(int i = 0; i < notifications.length; i++){
                if(notifications[i].getPackageName().contentEquals(entry.getValue().getPackageName()) && notifications[i].getId() == entry.getValue().getId()){
                    found = true;
                }
            }

            //otherwise remove it
            if(!found)
                uniqueIDs.remove(entry.getKey());
        }
    }

}
