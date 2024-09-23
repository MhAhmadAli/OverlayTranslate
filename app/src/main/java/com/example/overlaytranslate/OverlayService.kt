package com.example.overlaytranslate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        Log.d("OverlayService", "OverlayActivity: " + "Creating overlay view")

        val layoutInflator = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = layoutInflator.inflate(R.layout.overlay_layout, null) as FrameLayout

        // Set layout parameters for the overlay
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Requires Android O (API 26) and above
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START // Position it at the top-left of the screen
        params.x = 120
        params.y = 500 // Adjust the position

        // Get the window manager and add the view
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, params)

        // Handle resizing
        setupResizeListener(overlayView.findViewById(R.id.resize_handle), params)

        Log.d("OverlayService", "OverlayActivity: " + "Overlay view added")
    }

    @SuppressLint("ClickableViewAccessibility", "NewApi")
    private fun setupResizeListener(resizeHandle: View, params: WindowManager.LayoutParams) {
        resizeHandle.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    captureScreen( resizeHandle)
                }
//                MotionEvent.ACTION_MOVE -> {
//                    // Update the width and height of the overlay based on the touch event
//                    params.width = (event.rawX - params.x).toInt()
//                    params.height = (event.rawY - params.y).toInt()
//
//                    // Update the layout
//                    windowManager.updateViewLayout(overlayView, params)
//                }
            }
            true
        }
    }

    private fun captureScreen(view: View) {
        Bitmap. createBitmap()
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        saveBitmap(bitmap)
    }

//    @RequiresApi(Build.VERSION_CODES.R)
//    private fun captureScreen() {
//        Log.d("OverlayService", "OverlayActivity: " + "Capturing screenshot")
//        val windowMetrics = windowManager.currentWindowMetrics
//        val bitmap = Bitmap.createBitmap(
//            windowMetrics.bounds.width(),
//            windowMetrics.bounds.height(),
//            Bitmap.Config.ARGB_8888
//        )
//        val location = intArrayOf(overlayView.x.toInt(), overlayView.y.toInt())
////        val location = IntArray(2)
//        overlayView.getLocationInWindow(location)
//
//        val pixelCopyFinishedListener = PixelCopy.OnPixelCopyFinishedListener { copyResult ->
//            if (copyResult == PixelCopy.SUCCESS) {
//                saveBitmap(bitmap)
//            } else {
//                Log.d(
//                    "OverlayService",
//                    "OverlayActivity: Failed to capture screenshot, Error $copyResult"
//                )
//                Toast.makeText(this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
//            }
//        }

    private fun saveBitmap(bitmap: Bitmap) {
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val file = File(path, "screenshot_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
        Toast.makeText(this, "Screenshot saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView) // Remove the view when service is destroyed
            Log.d("OverlayService", "OverlayActivity: " + "Overlay view removed")
        }
    }
}