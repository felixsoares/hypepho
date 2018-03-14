package br.com.felix.hypepho

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image.*
import java.io.File


class ImageActivity : AppCompatActivity() {

    companion object {
        val FILE_PATH = "FILE_PATH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_image)

        llBack.setOnClickListener {
            finish()
        }

        download.setOnClickListener {
            download.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.success))
            txtSuccess.visibility = View.VISIBLE

            Handler().postDelayed({
                finish()
            }, 1200)
        }

        if (intent.extras != null) {
            val file = File(intent.getStringExtra(FILE_PATH))
            if (file.exists()) {
                configureImageView(file)
            }
        }
    }

    private fun configureImageView(file: File) {
        Picasso.get().load(Uri.fromFile(file)).into(image)
    }
}
