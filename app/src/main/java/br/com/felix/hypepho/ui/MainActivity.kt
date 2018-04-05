package br.com.felix.hypepho.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import br.com.felix.hypepho.R
import com.github.florent37.camerafragment.CameraFragment
import com.github.florent37.camerafragment.CameraFragmentApi
import com.github.florent37.camerafragment.configuration.Configuration
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var PATH: String
    private val FRAGMENT_TAG = "camera"
    private val CAMERA_REQUEST = 1888
    private var flashActive = false
    private var isFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        requestPermissions()

        llFlash.setOnClickListener {
            Handler().postDelayed({
                val cameraFragment = getCameraFragment()
                cameraFragment.toggleFlashMode()

                flashActive = if (flashActive) {
                    flash.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.flash))
                    false
                } else {
                    flash.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.flash_s))
                    true
                }
            }, 350)
        }

        pick.setOnClickListener {
            val cameraFragment = getCameraFragment()
            cameraFragment.takePhotoOrCaptureVideo(object : CameraFragmentResultListener {
                override fun onVideoRecorded(filePath: String?) {
                    TODO("not implemented")
                }

                override fun onPhotoTaken(bytes: ByteArray?, filePath: String?) {
                    launch(UI) {
                        progress.show()
                        async { callIntent(filePath!!) }.await()
                    }
                }
            }, PATH, UUID.randomUUID().toString())
        }

        llTurn.setOnClickListener {
            Handler().postDelayed({
                val cameraFragment = getCameraFragment()
                cameraFragment.switchCameraTypeFrontBack()
                isFront = !isFront
            }, 350)
        }

        PATH = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).path + "/hypepho"

        launch { deleteFiles(PATH) }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        )

        ActivityCompat.requestPermissions(
                this@MainActivity,
                permissions,
                CAMERA_REQUEST
        )
    }

    private fun deleteFiles(path: String) {
        try {
            val file = File(path)

            if (file.exists()) {
                val deleteCmd = "rm -r " + path
                val runtime = Runtime.getRuntime()
                runtime.exec(deleteCmd)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun callIntent(filePath: String) {
        delay(800)

        val bundle = Bundle()
        bundle.putString(ImageActivity.FILE_PATH, filePath)
        bundle.putBoolean(ImageActivity.PHOTO_FRONT, isFront)

        progress.hide()

        val intent = Intent(this@MainActivity, ImageActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCamera()
                } else {
                    Toast.makeText(this@MainActivity, "Please give me permissions", Toast.LENGTH_LONG).show()
                    requestPermissions()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun callCamera() {
        val builder = Configuration.Builder()
        builder
                .setCamera(Configuration.CAMERA_FACE_REAR)
                .setFlashMode(Configuration.FLASH_MODE_OFF)
                .setMediaAction(Configuration.MEDIA_ACTION_PHOTO)

        val cameraFragment = CameraFragment.newInstance(builder.build())

        supportFragmentManager.beginTransaction()
                .replace(R.id.content, cameraFragment, FRAGMENT_TAG)
                .commitAllowingStateLoss()

        cameraFragment.setResultListener(object : CameraFragmentResultListener {
            override fun onVideoRecorded(filePath: String) {
            }

            override fun onPhotoTaken(bytes: ByteArray, filePath: String) {
            }
        })
    }

    private fun getCameraFragment(): CameraFragmentApi {
        return supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as CameraFragmentApi
    }
}
