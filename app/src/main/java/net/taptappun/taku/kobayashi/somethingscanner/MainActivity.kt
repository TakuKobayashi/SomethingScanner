package net.taptappun.taku.kobayashi.somethingscanner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gc.materialdesign.views.ButtonRectangle
import net.taptappun.taku.kobayashi.runtimepermissionchecker.RuntimePermissionChecker

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanIntent = Intent(this, ScannerActivity::class.java)
        val faceScanButton = findViewById<ButtonRectangle>(R.id.faceScanButton)
        faceScanButton.setOnClickListener({v ->
            scanIntent.putExtra(Const.MOVE_TO_SCAN_INITENT_KEY, Const.ScanMode.Face.modeName)
            startActivity(scanIntent)
        })

        val textScaneButton = findViewById<ButtonRectangle>(R.id.textScaneButton)
        textScaneButton.setOnClickListener({v ->
            scanIntent.putExtra(Const.MOVE_TO_SCAN_INITENT_KEY, Const.ScanMode.Text.modeName)
            startActivity(scanIntent)
        })

        val barcodeScanButton = findViewById<ButtonRectangle>(R.id.barcodeScanButton)
        barcodeScanButton.setOnClickListener({v ->
            scanIntent.putExtra(Const.MOVE_TO_SCAN_INITENT_KEY, Const.ScanMode.Barcode.modeName)
            startActivity(scanIntent)
        })

        RuntimePermissionChecker.requestAllPermissions(this, PERMISSION_REQUEST_CODE);
    }
}
