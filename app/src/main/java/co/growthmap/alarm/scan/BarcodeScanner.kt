package co.growthmap.alarm.scan

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Live camera preview that emits the first decoded barcode value via [onBarcode].
 * Reused in configuration (register Target Code) and the Alarm Session (dismiss).
 */
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun BarcodeScanner(
    modifier: Modifier = Modifier,
    onBarcode: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    processFrame(scanner, imageProxy, onBarcode)
                }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

@SuppressLint("UnsafeOptInUsageError")
private fun processFrame(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcode: (String) -> Unit,
) {
    val media = imageProxy.image
    if (media == null) { imageProxy.close(); return }
    val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
    scanner.process(input)
        .addOnSuccessListener { codes ->
            codes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue?.let(onBarcode)
        }
        .addOnCompleteListener { imageProxy.close() }
}
