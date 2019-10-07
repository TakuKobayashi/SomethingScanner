package net.taptappun.taku.kobayashi.extendcamerascanner

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ReceivedFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(Const.TAG, "From: ${remoteMessage.from}")

        val notification = remoteMessage.notification;
        if(notification != null){
        }
        
        val messageData = remoteMessage.data;
        if(messageData != null){
            messageData.forEach {
                Log.d(Const.TAG, "Message Key: " + it.key + " value:" + it.value)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(Const.TAG, "generated Token:" + token)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(Const.TAG, "deleteMessage")
    }
}
