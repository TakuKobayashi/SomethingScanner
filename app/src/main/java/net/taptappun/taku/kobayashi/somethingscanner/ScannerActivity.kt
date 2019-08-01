package net.taptappun.taku.kobayashi.somethingscanner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector

class ScannerActivity : AppCompatActivity() {

    private val textureView: TextureView by lazy {
        findViewById<TextureView>(R.id.cameraPreview);
    }
    private val cameraManager: CameraManager by lazy {
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    private var backgroundThread: HandlerThread? = null;
    private var backgroundHandler: Handler? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
    }

    override fun onResume() {
        super.onResume()
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(texture: SurfaceTexture?, p1: Int, p2: Int) {
                    openCamera()
                }

                override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture?, p1: Int, p2: Int) {}
                override fun onSurfaceTextureUpdated(texture: SurfaceTexture?) {}
                override fun onSurfaceTextureDestroyed(texture: SurfaceTexture?): Boolean = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopBackgroundThread();
        closeCamera();
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
//            imageReader?.close()
//            imageReader = null
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera("0", object: CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createCameraPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                cameraDevice?.close()
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice, p1: Int) {
                cameraDevice?.close()
                cameraDevice = null
            }
        }, backgroundHandler)
    }

    private fun createCameraPreviewSession() {
        if (cameraDevice == null) {
            return
        }
        val texture = textureView.surfaceTexture
        texture.setDefaultBufferSize(640, 480)
        val surface = Surface(texture)

        val previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder.addTarget(surface)

        cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                // カメラプレビューを開始(TextureViewにカメラの画像が表示され続ける)
                captureSession?.setRepeatingRequest(previewRequestBuilder.build(), null, null)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {

            }
        }, null)
    }

    /*
     * 物理的なカメラの向きを取得
     */
    private fun getCameraOrientation(cameraId: String): Int {
        val characteristic = cameraManager.getCameraCharacteristics(cameraId)
        return characteristic.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
    }

    /*
     * 端末の向きを取得
     */
    private fun getApplicationOrientation(): Int {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.defaultDisplay.rotation
        return when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> throw IllegalStateException()
        }
    }

    /*
     * プレビューの向きを調整する
     */
    private fun rotateTextureView() {
        val orientation = getApplicationOrientation()
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        val matrix = Matrix()
        matrix.postRotate(-orientation.toFloat(), viewWidth * 0.5F, viewHeight * 0.5F)
        textureView.setTransform(matrix)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
//            Log.e(TAG, e.toString())
        }

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
}
