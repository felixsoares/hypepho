package br.com.felix.hypepho

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_image.*
import net.alhazmy13.imagefilter.ImageFilter
import java.io.File
import java.util.*
import android.os.Environment
import java.io.FileOutputStream


class ImageActivity : AppCompatActivity() {

    companion object {
        val FILE_PATH = "FILE_PATH"
        val PHOTO_FRONT = "PHOTO_FRONT"
    }

    private lateinit var mBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_image)

        llBack.setOnClickListener {
            finish()
        }

        download.setOnClickListener {
            progress.show()

            Handler().postDelayed({
                if (saveImage(mBitmap)) {
                    download.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.success))
                    txtSuccess.visibility = View.VISIBLE

                    progress.hide()
                    finish()
                }
            }, 1000)
        }

        if (intent.extras != null) {
            val file = File(intent.getStringExtra(FILE_PATH))
            if (file.exists()) {
                configureImageView(file, intent.getBooleanExtra(PHOTO_FRONT, false))
            }
        }
    }

    private fun configureImageView(file: File, isFront: Boolean) {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val first = ImageFilter.applyFilter(bitmap, ImageFilter.Filter.GAUSSIAN_BLUR)
        mBitmap = rotateImage(ImageFilter.applyFilter(first, ImageFilter.Filter.LOMO), if (isFront) -90f else 90f)
        image.setImageBitmap(mBitmap)
    }

    private fun saveImage(finalBitmap: Bitmap):Boolean {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val myDir = File(root + "/hypepho")
        myDir.mkdirs()
        val generator = Random()

        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(myDir, fileName)
        if (file.exists()) file.delete()

        return try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun rotateImage(src: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }
}
