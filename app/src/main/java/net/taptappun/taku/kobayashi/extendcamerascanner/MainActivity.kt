package net.taptappun.taku.kobayashi.extendcamerascanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gc.materialdesign.views.ButtonRectangle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import net.taptappun.taku.kobayashi.runtimepermissionchecker.RuntimePermissionChecker
import com.allyants.notifyme.NotifyMe

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

        val analyticsHookButton = findViewById<ButtonRectangle>(R.id.analyticsHookButton)
        analyticsHookButton.setOnClickListener({ v ->
            val instanceId = FirebaseInstanceId.getInstance().instanceId
            instanceId.addOnCompleteListener { task ->
                val notifyMe = NotifyMe.Builder(applicationContext)
                if (!task.isSuccessful) {
                    Log.d(Const.TAG, "FCM Generate InstanceId:" + task.result?.token)
                    notifyMe.title("Success");
                    notifyMe.content("FCM Generate InstanceId:" + task.result?.token);
                }else{
                    Log.d(Const.TAG, "FCM Generate InstanceId Fail")
                    notifyMe.title("Fail");
                    notifyMe.content("FCM Generate InstanceId Fail");
                }
                notifyMe.build();
            }
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, R.id.analyticsHookButton.toString())
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "analyticsHookButton")
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        })

        RuntimePermissionChecker.requestAllPermissions(this, PERMISSION_REQUEST_CODE)
    }
}
