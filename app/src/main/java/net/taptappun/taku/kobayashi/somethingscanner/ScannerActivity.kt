package net.taptappun.taku.kobayashi.somethingscanner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.util.Size

class ScannerActivity : AppCompatActivity() {

    private val textureView: TextureView by lazy {
        findViewById<TextureView>(R.id.cameraPreview)
    }
    private val cameraManager: CameraManager by lazy {
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private lateinit var currentCameraIdStringIntPair: Pair<String, Int>

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private val mCameraCaptureSessionCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureBufferLost(
            session: CameraCaptureSession,
            request: CaptureRequest,
            target: Surface,
            frameNumber: Long
        ) {
            super.onCaptureBufferLost(session, request, target, frameNumber)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
        }

        override fun onCaptureFailed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            failure: CaptureFailure
        ) {
            super.onCaptureFailed(session, request, failure)
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            super.onCaptureProgressed(session, request, partialResult)
        }

        override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
            super.onCaptureSequenceAborted(session, sequenceId)
        }

        override fun onCaptureSequenceCompleted(
            session: CameraCaptureSession,
            sequenceId: Int,
            frameNumber: Long
        ) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
        }

        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scanview)
        val currentCameraId = intent.getStringExtra(Const.CAMERAID_INITENT_KEY)
        var cameraIdInt = CameraCharacteristics.LENS_FACING_FRONT
        if(!currentCameraId.isNullOrBlank()){
            cameraIdInt = convertCameraIdStringToInt(cameraManager, currentCameraId)
        }

        val cameraIdPair = getSuppurtedCameraIdPair(cameraManager, cameraIdInt)
        if(cameraIdPair == null){
            finish()
            return;
        }
        currentCameraIdStringIntPair = cameraIdPair

        val previewSize = getMaxImagePreviewSize();
        if(previewSize != null){
            imageReader = ImageReader.newInstance(previewSize!!.width, previewSize!!.height, )
        }
    }

    override fun onResume() {
        super.onResume()
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
        if (textureView.isAvailable) {
            openCamera(currentCameraIdStringIntPair.first)
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(texture: SurfaceTexture?, p1: Int, p2: Int) {
                    openCamera(currentCameraIdStringIntPair.first)
                }

                override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture?, p1: Int, p2: Int) {}
                override fun onSurfaceTextureUpdated(texture: SurfaceTexture?) {}
                override fun onSurfaceTextureDestroyed(texture: SurfaceTexture?): Boolean = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopBackgroundThread()
        closeCamera()
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
//            imageReader?.close()
//            imageReader = null
    }

    private fun getMaxImagePreviewSize(): Size? {
        val characteristics = cameraManager.getCameraCharacteristics(currentCameraIdStringIntPair.first)
        val map: StreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes: List<Size> = map.getOutputSizes(ImageFormat.YV12).toList()
        val maxSize = sizes.maxBy{size -> size.width * size.height }
        return maxSize
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(cameraId: String) {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
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
        val surface = Surface(texture)

        val previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder.addTarget(surface)

        cameraDevice!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                // プレビューがぼやけては困るのでオートフォーカスを利用する
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                // カメラプレビューを開始(TextureViewにカメラの画像が表示され続ける)
                captureSession?.setRepeatingRequest(
                    previewRequestBuilder.build(),
                    mCameraCaptureSessionCallback,
                    backgroundHandler
                )
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
            }
        }, null)
    }

    private fun convertCameraIdStringToInt(manager: CameraManager, cameraId: String): Int{
        val characteristics = manager.getCameraCharacteristics(cameraId)
        return characteristics.get(CameraCharacteristics.LENS_FACING)
    }

    private fun getSuppurtedCameraIdPair(manager: CameraManager, willGetIntCameraId: Int? = null): Pair<String, Int>? {
        var supportCameraIds = manager.getCameraIdList()
        var result: Pair<String, Int>? = null
        if (willGetIntCameraId != null) {
            val supportCameraId = supportCameraIds.firstOrNull({ cameraId ->
                val lensFacing = convertCameraIdStringToInt(manager, cameraId)
                lensFacing == willGetIntCameraId
            })
            if(supportCameraId != null){
                result = Pair(supportCameraId, willGetIntCameraId)
            }
        }
        if (result == null) {
            for (cameraId in supportCameraIds) {
                val lensFacing = convertCameraIdStringToInt(manager, cameraId)
                result = Pair(cameraId, lensFacing)
                break
            }
        }
        return result
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
//          Log.e(TAG, e.toString())
        }
    }

    private fun setupScan() {
        val builder = FirebaseVisionFaceDetectorOptions.Builder()
        builder.setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
        builder.setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        builder.setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        builder.setMinFaceSize(0.05f)
        builder.setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
        builder.enableTracking()

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(builder.build())
    }
}
