package com.lb.mediamuxersample

import TimeLapseEncoder
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.gifdecoder.GifHeaderParser
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.integration.webp.WebpImage
import com.bumptech.glide.integration.webp.decoder.WebpDecoder
import com.bumptech.glide.integration.webp.decoder.WebpFrameCacheStrategy
import com.bumptech.glide.integration.webp.decoder.WebpFrameLoader
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        thread {
            testImages()
            testImage()
            testGif()
            testWebP()
            testLottie()
        }
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                var url: String? = null
                when (item.itemId) {
                    R.id.menuItem_all_my_apps -> url =
                        "https://play.google.com/store/apps/developer?id=AndroidDeveloperLB"

                    R.id.menuItem_all_my_repositories -> url =
                        "https://github.com/AndroidDeveloperLB"

                    R.id.menuItem_current_repository_website -> url =
                        "https://github.com/AndroidDeveloperLB/MediaMuxerSample"
                }
                if (url == null)
                    return true
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                @Suppress("DEPRECATION")
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivity(intent)
                return true
            }
        })
    }

    @WorkerThread
    private fun testImages() {
        Log.d("AppLog", "testImages")
        val startTime = System.currentTimeMillis()
        Log.d("AppLog", "start")
        val videoFile = File(ContextCompat.getExternalFilesDirs(this, null)[0], "images.mp4")
        if (videoFile.exists())
            videoFile.delete()
        videoFile.parentFile!!.mkdirs()
//        Log.d("AppLog", "success creating parent?${videoFile.parentFile.exists()}")
        val timeLapseEncoder = TimeLapseEncoder()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.frame1)
        val width = bitmap.width
        val height = bitmap.height
        timeLapseEncoder.prepareForEncoding(videoFile.absolutePath, width, height)
        val delay = 5000
//        for (i in 0 until 500)
//            timeLapseEncoder.encodeFrame(bitmap, 10)
        timeLapseEncoder.encodeFrame(bitmap, delay)
        val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.frame2)
//        for (i in 0 until 500)
//            timeLapseEncoder.encodeFrame(bitmap2, 10)
        timeLapseEncoder.encodeFrame(bitmap2, delay)
        timeLapseEncoder.finishEncoding()
        val endTime = System.currentTimeMillis()
        Log.d(
            "AppLog",
            "it took ${endTime - startTime} ms to convert a single image ($width x $height) to ${videoFile.absolutePath} ${videoFile.exists()} ${videoFile.length()}"
        )
    }

    @WorkerThread
    private fun testImage() {
        Log.d("AppLog", "testImage")
        val startTime = System.currentTimeMillis()
        Log.d("AppLog", "start")
        val videoFile = File(ContextCompat.getExternalFilesDirs(this, null)[0], "image.mp4")
        if (videoFile.exists())
            videoFile.delete()
        videoFile.parentFile!!.mkdirs()
        val timeLapseEncoder = TimeLapseEncoder()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test)
        val width = bitmap.width
        val height = bitmap.height
        timeLapseEncoder.prepareForEncoding(videoFile.absolutePath, width, height)
        val frameDurationInMs = 1000
        timeLapseEncoder.encodeFrame(bitmap, frameDurationInMs)
        timeLapseEncoder.finishEncoding()
        val endTime = System.currentTimeMillis()
        Log.d(
            "AppLog",
            "it took ${endTime - startTime} ms to convert a single image ($width x $height) to ${videoFile.absolutePath} "
        )
    }

    @SuppressLint("ResourceType")
    @WorkerThread
    private fun testGif() {
        Log.d("AppLog", "testGif")
        val startTime = System.currentTimeMillis()
        val videoFile = File(ContextCompat.getExternalFilesDirs(this, null)[0], "gif.mp4")
        if (videoFile.exists())
            videoFile.delete()
        videoFile.parentFile!!.mkdirs()
        val timeLapseEncoder = TimeLapseEncoder()
        // found from GifDrawableResource StreamGifDecoder StandardGifDecoder
        val data = resources.openRawResource(R.drawable.animated_gif).readBytes()
        val byteBuffer = ByteBuffer.wrap(data)
        val glide = Glide.get(this)
        val gifBitmapProvider = GifBitmapProvider(glide.bitmapPool, glide.arrayPool)
        val header = GifHeaderParser().setData(byteBuffer).parseHeader()
        val standardGifDecoder = StandardGifDecoder(gifBitmapProvider, header, byteBuffer, 1)
        //alternative, without getting header and needing sample size:
//        val standardGifDecoder = StandardGifDecoder(gifBitmapProvider)
//        standardGifDecoder.read(data)
        val width = standardGifDecoder.width
        val height = standardGifDecoder.height
        timeLapseEncoder.prepareForEncoding(videoFile.absolutePath, width, height)
        val frameCount = standardGifDecoder.frameCount
        Log.d("AppLog", "gif size:${width}x$height frameCount:$frameCount")
        for (i in 0 until frameCount) {
            standardGifDecoder.advance()
            val delay = standardGifDecoder.nextDelay
            val bitmap = standardGifDecoder.nextFrame ?: break
            Log.d(
                "AppLog",
                "bitmap for frame $i  delay:$delay status:${standardGifDecoder.status} ${bitmap.width}x${bitmap.height}"
            )
            timeLapseEncoder.encodeFrame(bitmap, delay)
        }
        timeLapseEncoder.finishEncoding()
        val endTime = System.currentTimeMillis()
        Log.d(
            "AppLog",
            "it took ${endTime - startTime} ms to get all $frameCount frames of the gif ($width x $height) as bitmaps to ${videoFile.absolutePath} "
        )
    }

    @SuppressLint("ResourceType")
    @WorkerThread
    private fun testWebP() {
        Log.d("AppLog", "testWebP")
        val startTime = System.currentTimeMillis()
        val videoFile = File(ContextCompat.getExternalFilesDirs(this, null)[0], "webp.mp4")
        if (videoFile.exists())
            videoFile.delete()
        videoFile.parentFile!!.mkdirs()
        val timeLapseEncoder = TimeLapseEncoder()
        //found from  ByteBufferWebpDecoder  StreamWebpDecoder  WebpDecoder
        val data = resources.openRawResource(R.drawable.animated_webp).readBytes()
        val cacheStrategy: WebpFrameCacheStrategy? =
            Options().get(WebpFrameLoader.FRAME_CACHE_STRATEGY)
        val glide = Glide.get(this)
        val bitmapPool = glide.bitmapPool
        val arrayPool = glide.arrayPool
        val gifBitmapProvider = GifBitmapProvider(bitmapPool, arrayPool)
        val webpImage = WebpImage.create(data)
        val sampleSize = 1
        val webpDecoder = WebpDecoder(gifBitmapProvider, webpImage, ByteBuffer.wrap(data), sampleSize, cacheStrategy)
        val width = webpDecoder.width
        val height = webpDecoder.height
        timeLapseEncoder.prepareForEncoding(videoFile.absolutePath, width, height)
        val frameCount = webpDecoder.frameCount
        Log.d("AppLog", "webp size:${width}x$height frameCount:$frameCount")
//        webpDecoder.advance()

        for (i in 0 until frameCount) {
            webpDecoder.advance()
            val delay = webpDecoder.nextDelay
            val bitmap = webpDecoder.nextFrame ?: break
            Log.d(
                "AppLog",
                "bitmap for frame $i  delay:$delay status:${webpDecoder.status} ${bitmap.width}x${bitmap.height}"
            )
            //bitmap ready here
            timeLapseEncoder.encodeFrame(bitmap, delay)
        }
        timeLapseEncoder.finishEncoding()
        val endTime = System.currentTimeMillis()
        Log.d(
            "AppLog",
            "it took ${endTime - startTime} ms to get all $frameCount frames of the webp ($width x $height) as bitmaps to ${videoFile.absolutePath} "
        )
    }

    @WorkerThread
    private fun testLottie() {
        Log.d("AppLog", "testLottie")
        val startTime = System.currentTimeMillis()
        val countDownLatch = CountDownLatch(1)
        LottieCompositionFactory.fromRawRes(this, R.raw.lottie)
            .addListener { composition ->
                // Create a LottieDrawable from the LottieComposition
                val drawable = LottieDrawable().apply {
                    setComposition(composition)
                }
                thread {
                    try {
                        val videoFile =
                            File(ContextCompat.getExternalFilesDirs(this, null)[0], "lottie.mp4")
                        if (videoFile.exists())
                            videoFile.delete()
                        videoFile.parentFile!!.mkdirs()
                        val timeLapseEncoder = TimeLapseEncoder()
                        val totalFrames = composition.durationFrames.toInt()
                        val frameDuration = composition.duration / totalFrames
                        val frameDurationInt = frameDuration.roundToInt()
                        //                Log.d("AppLog", "isUiThread?${Thread.currentThread().id==mainLooper.thread.id}")
                        Log.d(
                            "AppLog",
                            "duration of each frame:$frameDurationInt ms . Frames count:$totalFrames totalDuration:${composition.duration}"
                        )
                        //                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                        val width = drawable.intrinsicWidth
                        val height = drawable.intrinsicHeight
                        val bitmap =
                            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        timeLapseEncoder.prepareForEncoding(videoFile.absolutePath, width, height)
                        for (i in 0 until totalFrames) {
                            drawable.frame = i
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                            drawable.draw(canvas)
                            //bitmap ready here
                            timeLapseEncoder.encodeFrame(bitmap, frameDurationInt)
                            Log.d("AppLog", "bitmap for frame $i  ")
                        }
                        timeLapseEncoder.finishEncoding()
                        val endTime = System.currentTimeMillis()
                        Log.d(
                            "AppLog",
                            "it took ${endTime - startTime} ms to get all $totalFrames frames ($width x $height) as bitmaps to ${videoFile.absolutePath} "
                        )
                    } catch (e: Exception) {
                        Log.d("AppLog", "error:$e")
                        e.printStackTrace()
                    }
                    countDownLatch.countDown()
                }
            }
        countDownLatch.await()
    }
}
