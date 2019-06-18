package net.taptappun.taku.kobayashi.somethingscanner

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.TextureView
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector





class ScannerActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView;
    private var backgroundThread: HandlerThread;
    private var backgroundHandler: Handler;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        textureView = findViewById<TextureView>(R.id.cameraPreview);
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        textureView.surfaceTextureListener = surfaceTextureListener;
    }

    private fun openCamera(width: Int, height: Int) {
        val manager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            //manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            //Log.e(TAG, e.toString())
        }
    }

    private fun closeCamera() {
        /*
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
        */
    }

    private fun setupScan(){
        val builder = FirebaseVisionFaceDetectorOptions.Builder();
        builder.setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS);
        builder.setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        builder.setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        builder.setMinFaceSize(0.05f)
        builder.setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
        builder.enableTracking();

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(builder.build())
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit

    }
}
