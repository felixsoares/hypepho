package br.com.felix.hypepho

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.github.florent37.camerafragment.CameraFragment
import com.github.florent37.camerafragment.CameraFragmentApi
import com.github.florent37.camerafragment.configuration.Configuration
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_PREVIEW_CODE = 1001

    private val FRAGMENT_TAG = "camera"
    private val CAMERA_REQUEST = 1888
    private var flashActive = false
    private var isFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

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
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onPhotoTaken(bytes: ByteArray?, filePath: String?) {
                    progress.show()

                    Handler().postDelayed({
                        val bundle = Bundle()
                        bundle.putString(ImageActivity.FILE_PATH, filePath)
                        bundle.putBoolean(ImageActivity.PHOTO_FRONT, isFront)

                        val intent = Intent(this@MainActivity, ImageActivity::class.java)
                        intent.putExtras(bundle)
                        startActivityForResult(intent, REQUEST_PREVIEW_CODE)

                        progress.hide()
                    }, 1000)
                }
            }, getExternalFilesDir(Environment.DIRECTORY_PICTURES).path + "/hypepho", UUID.randomUUID().toString())
        }

        llTurn.setOnClickListener {
            Handler().postDelayed({
                val cameraFragment = getCameraFragment()
                cameraFragment.switchCameraTypeFrontBack()
                isFront = !isFront
            }, 350)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCamera()
                } else {
                    Toast.makeText(this@MainActivity, "Sem permissão", Toast.LENGTH_LONG).show()
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
                .commit()

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
