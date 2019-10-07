package net.taptappun.taku.kobayashi.somethingscanner

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ReceivedFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(Const.TAG, "From: ${remoteMessage.from}")
        
        val messageData = remoteMessage.data;
        messageData.forEach {
            Log.d(Const.TAG, "Message Key: " + it.key + " value:" + it.value)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(Const.TAG, "deleteMessage")
    }
}
