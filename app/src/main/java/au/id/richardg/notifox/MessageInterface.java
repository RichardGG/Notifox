package au.id.richardg.notifox;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/*
 *
 * Manages the queue of messages to be sent to Pebble
 *
 */

public class MessageInterface {

    private final static String TAG = "MessageInterface";

    private boolean mReadyForSend;
    private List<Message> mMessageQueue;
    private int mTransactionId;
    private UUID mPebbleAppUuid;

    public MessageInterface(UUID pebbleAppUuid) {
        Log.i(TAG, "MessageInterface()");
        mPebbleAppUuid = pebbleAppUuid;
        mReadyForSend = true;
        mTransactionId = 0;
        mMessageQueue = new ArrayList<Message>();
    }

    public synchronized void send(Context context, PebbleDictionary pebbleDictionary) {
        Log.i(TAG, "send()");
        if (pebbleDictionary != null) {
            mTransactionId += 1;
            //Log.v(WearService.TAG, "MessageInterface.send() New message: " + mTransactionId);
            mMessageQueue.add(new Message(mTransactionId, pebbleDictionary));
        }
        if (mReadyForSend) {
            Log.d(TAG, "ready for send");
            if (mMessageQueue.size() > 0) {
                Message message = mMessageQueue.get(0);
                //Log.v(WearService.TAG, "MessageInterface.send() Sending message: "+ message.getTransactionId());
                PebbleKit.sendDataToPebbleWithTransactionId(context, mPebbleAppUuid,
                        message.getMessage(), message.getTransactionId());
            }
            mReadyForSend = false;
        }
        else
            Log.d(TAG, "not ready for send");
    }

    public synchronized void success(int transactionId) {
        Log.i(TAG, "success()");
        for (Iterator<Message> iterator = mMessageQueue.listIterator(); iterator.hasNext(); ) {
            Message message = iterator.next();
            if (message.getTransactionId() == transactionId) {
                //Log.v(WearService.TAG, "MessageInterface.success() Removing from queue: " + message.getTransactionId());
                iterator.remove();
                //return;  screw this line in particular
            }
        }
        mReadyForSend = true;
        Log.d(TAG, "mreadyforsend = " + mReadyForSend);
    }

    public void fail(int transactionId) {
        Log.i(TAG, "fail()");
        // TODO: Do something smart with the NACK'ed responses.
        success(transactionId);
    }

    private class Message {

        int mTransactionId;
        PebbleDictionary mMessage;

        private Message(int transactionId, PebbleDictionary message) {
            Log.i(TAG, "Message()");
            mTransactionId = transactionId;
            mMessage = message;
        }

        public int getTransactionId() {
            return mTransactionId;
        }

        public PebbleDictionary getMessage() {
            return mMessage;
        }
    }
}
