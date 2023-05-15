package mollah.yamin.androidimagechooser.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import mollah.yamin.androidimagechooser.R
import mollah.yamin.androidimagechooser.databinding.MainActivityBinding
import mollah.yamin.androidimagechooser.utils.PermissionUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var picFromCameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var picFromGalleryLauncher: ActivityResultLauncher<Intent>

    private lateinit var permissionUtils: PermissionUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionUtils = PermissionUtils(activity = this, isFragmentContext = false)

        picFromCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            //onCaptureImageResult(result?.data)
        }

        picFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val photoUri = result?.data?.data
            photoUri?.let {
                try {
                    //getGalleryImagePath(photoUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.btnSelectImage.setOnClickListener {
            permissionUtils.requestMultiplePermissions(
                permissions = PermissionUtils.requiredPermissions,
                rationaleMsg = getString(R.string.allow_permissions_to_continue),
                object : PermissionUtils.PermissionRequestResultCallback {
                    @SuppressLint("MissingPermission")
                    override fun onPermissionGranted() {
                        //selectImages()
                    }
                }
            )
        }
    }
}