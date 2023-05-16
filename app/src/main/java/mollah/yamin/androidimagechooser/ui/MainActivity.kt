package mollah.yamin.androidimagechooser.ui

import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mollah.yamin.androidimagechooser.BuildConfig
import mollah.yamin.androidimagechooser.R
import mollah.yamin.androidimagechooser.databinding.MainActivityBinding
import mollah.yamin.androidimagechooser.ui.dialogs.ImageChooserDialog
import mollah.yamin.androidimagechooser.utils.BitmapUtils
import mollah.yamin.androidimagechooser.utils.FileUtils
import mollah.yamin.androidimagechooser.utils.PermissionUtils
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var picFromCameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var picFromGalleryLauncher: ActivityResultLauncher<Intent>

    private lateinit var permissionUtils: PermissionUtils

    private var contrast = 1f
    private var brightness = 0f

    var latestCameraPhotoPath: String = ""
    private var savedBitmap: Bitmap? = null
        set(value) {
            field = value

            runOnUiThread {
                binding.sliderBright.isEnabled = value != null
                binding.sliderContrast.isEnabled = value != null
            }
        }

    private var tempBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionUtils = PermissionUtils(activity = this, isFragmentContext = false)

        picFromCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            /* Since a uri is passed to the external selected camera app
             * Image will be save to that path. There is no need to find
             * image into the result*/
            onCaptureImageResult()
        }

        picFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val photoUri = result?.data?.data
            photoUri?.let {
                try {
                    onGalleryImageResult(photoUri)
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
                        showImageChooserDialog()
                    }
                }
            )
        }

        binding.btnRotateLeft.setOnClickListener {
            savedBitmap?.let {
                binding.loader.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    savedBitmap = BitmapUtils.rotateBitmap(it, -90, flipX = false, flipY = false)
                    applyContrastAndBrightnessToTemp()
                    runOnUiThread {
                        binding.preview.setImageBitmap(tempBitmap)
                        binding.loader.visibility = View.INVISIBLE
                    }
                }
            }
        }

        binding.btnRotateRight.setOnClickListener {
            savedBitmap?.let {
                binding.loader.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    savedBitmap = BitmapUtils.rotateBitmap(it, 90, flipX = false, flipY = false)
                    applyContrastAndBrightnessToTemp()
                    runOnUiThread {
                        binding.preview.setImageBitmap(tempBitmap)
                        binding.loader.visibility = View.INVISIBLE
                    }
                }
            }
        }

        binding.btnFlipHorizontal.setOnClickListener {
            savedBitmap?.let {
                binding.loader.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    savedBitmap = BitmapUtils.rotateBitmap(it, 0, flipX = true, flipY = false)
                    applyContrastAndBrightnessToTemp()
                    runOnUiThread {
                        binding.preview.setImageBitmap(tempBitmap)
                        binding.loader.visibility = View.INVISIBLE
                    }
                }
            }
        }

        binding.btnFlipVertical.setOnClickListener {
            savedBitmap?.let {
                binding.loader.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    savedBitmap = BitmapUtils.rotateBitmap(it, 0, flipX = false, flipY = true)
                    applyContrastAndBrightnessToTemp()
                    runOnUiThread {
                        binding.preview.setImageBitmap(tempBitmap)
                        binding.loader.visibility = View.INVISIBLE
                    }
                }
            }
        }

        binding.sliderBright.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
                applyContrastAndBrightness()
            }
        })

        binding.sliderBright.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
            brightness = value
        }

        binding.sliderContrast.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
                applyContrastAndBrightness()
            }
        })

        binding.sliderContrast.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
            contrast = value / 10
        }

        savedBitmap = null
    }

    private fun applyContrastAndBrightnessToTemp() {
        savedBitmap?.let { bitmap ->
            tempBitmap = BitmapUtils.changeBitmapContrastBrightness(
                bmp = bitmap, contrast = contrast, brightness = brightness
            )
        }
    }

    private fun applyContrastAndBrightness() {
        savedBitmap?.let { bitmap ->
            binding.loader.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                tempBitmap = BitmapUtils.changeBitmapContrastBrightness(
                    bmp = bitmap, contrast = contrast, brightness = brightness
                )
                runOnUiThread {
                    binding.preview.setImageBitmap(tempBitmap)
                    binding.loader.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun showImageChooserDialog() {
        val imageChooserDialog = ImageChooserDialog(object: ImageChooserDialog.ImageChooserCallback {
            override fun openCamera() {
                selectImageFromCamera()
            }

            override fun openGallery() {
                selectImageFromGallery()
            }

        })
        imageChooserDialog.show(supportFragmentManager, "#ImageChooserDialog")
    }

    private fun onCaptureImageResult() {
        try {
            binding.loader.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                var photoPath = Uri.parse(latestCameraPhotoPath).path ?: return@launch
                var bitmap = BitmapUtils.getCorrectlyOrientedImage(photoPath)

                // Resize bitmap if you wish
                bitmap = BitmapUtils.getResizedBitmap(bitmap, 1000) ?: return@launch

                // Check file size and compress
                // 1 Byte X 1024 = 1 KB && 1 KB X 1024 = 1MB
                if (File(photoPath).length() > (4*1024*1024) /* 4 MB */ ) {
                    photoPath = BitmapUtils.compressBitmap(
                        imageBitmap = bitmap,
                        imageQuality = 50,
                        path = photoPath
                    ).absolutePath
                }

                savedBitmap = BitmapUtils.getBitmapFromFilePath(photoPath) ?: return@launch
                runOnUiThread {
                    binding.preview.setImageBitmap(savedBitmap)
                    binding.loader.visibility = View.INVISIBLE
                    Toast.makeText(this@MainActivity,
                        "Size: ${FileUtils.formatFileSizeInText(File(photoPath).length().toDouble())}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (exception: Exception) {
            binding.loader.visibility = View.INVISIBLE
            exception.printStackTrace()
        }
    }

    private fun onGalleryImageResult(photoUri: Uri) {
        try {
            binding.loader.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                var photoPath = FileUtils.getFilePathFromMediaContentUri(photoUri, this@MainActivity) ?: return@launch
                var bitmap = BitmapUtils.getCorrectlyOrientedImage(photoPath)

                // Resize bitmap if you wish
                bitmap = BitmapUtils.getResizedBitmap(bitmap, 1000) ?: return@launch

                // Check file size and compress
                // 1 Byte X 1024 = 1 KB && 1 KB X 1024 = 1MB
                if (File(photoPath).length() > (4*1024*1024) /* 4 MB */ ) {
                    photoPath = BitmapUtils.compressBitmap(
                        imageBitmap = bitmap,
                        imageQuality = 50,
                        path = photoPath
                    ).absolutePath
                }

                savedBitmap = BitmapUtils.getBitmapFromFilePath(photoPath) ?: return@launch
                runOnUiThread {
                    binding.preview.setImageBitmap(savedBitmap)
                    binding.loader.visibility = View.INVISIBLE
                    Toast.makeText(this@MainActivity,
                        "Size: ${FileUtils.formatFileSizeInText(File(photoPath).length().toDouble())}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (exception: Exception) {
            binding.loader.visibility = View.INVISIBLE
            exception.printStackTrace()
        }
    }

    private fun selectImageFromCamera() {
        val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val imageFile = FileUtils.createImageFileInImageDir(this)
            if (!imageFile.exists()) {
                Toast.makeText(this, "Device rejected to create image file!", Toast.LENGTH_LONG).show()
                return
            }

            // Save path to get image from it after completion
            latestCameraPhotoPath = imageFile.absolutePath

            // Get a photo uri from file provider to share the file path with external camera apps
            val photoURI = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.provider", // File provider
                imageFile
            )
            // Share the uri with external camera apps
            photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            // Check that the intent does not grant URI permissions
            val flags: Int = photoIntent.flags
            if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0 &&
                flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION == 0
            ) {
                picFromCameraLauncher.launch(photoIntent)
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            } else {
                Toast.makeText(this, "Device rejected to create image file!", Toast.LENGTH_LONG).show()
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
        } catch (exception: PackageManager.NameNotFoundException) {
            exception.printStackTrace()
            Toast.makeText(this, "No camera app found in the device!", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImageFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        picFromGalleryLauncher.launch(galleryIntent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }
}