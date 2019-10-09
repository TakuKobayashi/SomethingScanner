package net.taptappun.taku.kobayashi.extendcamerascanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gc.materialdesign.views.ButtonRectangle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import net.taptappun.taku.kobayashi.runtimepermissionchecker.RuntimePermissionChecker
import io.karn.notify.Notify

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val analytics = FirebaseAnalytics.getInstance(this.applicationContext)

        val scanIntent = Intent(this, ScannerActivity::class.java)
        val faceScanButton = findViewById<ButtonRectangle>(R.id.faceScanButton)
        faceScanButton.setOnClickListener({ v ->
            scanIntent.putExtra(Const.MOVE_TO_SCAN_INITENT_KEY, Const.ScanMode.Face.modeName)
            startActivity(scanIntent)
        })

        val textScaneButton = findViewById<ButtonRectangle>(R.id.textScaneButton)
        textScaneButton.setOnClickListener({ v ->
            scanIntent.putExtra(Const.MOVE_TO_SCAN_INITENT_KEY, Const.ScanMode.Text.modeName)
            startActivity(scanIntent)
        })

        val barcodeScanButton = findViewById<ButtonRectangle>(R.id.barcodeScanButton)
        barcodeScanButton.setOnClickListener({ v ->
            scanIntent.putExtra(Const.MOVE_TO_SCAN_INITENT_KEY, Const.ScanMode.Barcode.modeName)
            startActivity(scanIntent)
        })

        val agePicker = findViewById<NumberPicker>(R.id.agePicker)
        agePicker.minValue = 0
        agePicker.maxValue = 100
        agePicker.value = 30

        val analyticsHookButton = findViewById<ButtonRectangle>(R.id.analyticsHookButton)
        analyticsHookButton.setOnClickListener({ v ->
            Log.d(Const.TAG, "Button Press")
            val instanceId = FirebaseInstanceId.getInstance().instanceId
            instanceId.addOnCompleteListener { task ->
                Log.d(Const.TAG, "Log Event Success")
                var titleText = "";
                var contentText = "";
                if (task.isSuccessful) {
                    Log.d(Const.TAG, "FCM Generate InstanceId:" + task.result?.token)
                    titleText = "Success"
                    contentText = "FCM Generate InstanceId:" + task.result?.token
                }else{
                    Log.d(Const.TAG, "FCM Generate InstanceId Fail")
                    titleText = "Fail"
                    contentText = "FCM Generate InstanceId Fail"
                }
                val notifyMe = Notify.with(applicationContext).content {
                    title = titleText
                    text = contentText
                }.show()
                Toast.makeText(this, "Success:" + task.isSuccessful + " token:" + task.result?.token, Toast.LENGTH_LONG).show()
            }
            analytics.setUserProperty("age", agePicker.value.toString())
            Log.d(Const.TAG, "Log Event Success")

        })
        Log.d(Const.TAG, "analytics" + analyticsHookButton)

        RuntimePermissionChecker.requestAllPermissions(this, PERMISSION_REQUEST_CODE)
    }
}
