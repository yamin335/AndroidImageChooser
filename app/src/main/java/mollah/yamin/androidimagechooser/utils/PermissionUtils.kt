package mollah.yamin.androidimagechooser.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import mollah.yamin.androidimagechooser.BuildConfig
import mollah.yamin.androidimagechooser.R
import mollah.yamin.androidimagechooser.ui.dialogs.CommonMessageShowingBottomDialog

class PermissionUtils constructor(
    private val activity: AppCompatActivity,
    isFragmentContext: Boolean
) {
    private lateinit var hostFragment: Fragment
    private var requiredPermissions: Array<String> = arrayOf()
    private var permission: String = ""
    private var rationaleMsg: String = ""
    private var callback: PermissionRequestResultCallback? = null
    private lateinit var multiplePermissionRequestLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var singlePermissionRequestLauncher: ActivityResultLauncher<String>

    init {
        if (!isFragmentContext) {
            singlePermissionRequestLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                val shouldShowPermissionRationaleDialog = activity.shouldShowRequestPermissionRationaleCompat(permission)
                when {
                    isGranted -> {
                        callback?.onPermissionGranted()
                    }
                    shouldShowPermissionRationaleDialog -> {
                        showPermissionRationaleDialog {
                            launchSinglePermissionRequest()
                        }
                    }
                    else -> {
                        showPermissionDeniedDialog()
                    }
                }
            }

            multiplePermissionRequestLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->

                val granted = permissions.entries.all {
                    it.value
                }

                val shouldShowPermissionRationaleDialog = checkPermissionRationale(
                    activity,
                    requiredPermissions
                )

                when {
                    granted -> {
                        callback?.onPermissionGranted()
                    }
                    shouldShowPermissionRationaleDialog -> {
                        showPermissionRationaleDialog {
                            launchMultiplePermissionRequest()
                        }
                    }
                    else -> {
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }

    constructor(appCompatActivity: AppCompatActivity, fragment: Fragment, isFragmentContext: Boolean) : this(appCompatActivity, isFragmentContext) {
        this.hostFragment = fragment
        singlePermissionRequestLauncher = hostFragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            val shouldShowPermissionRationaleDialog = activity.shouldShowRequestPermissionRationaleCompat(permission)
            when {
                isGranted -> {
                    callback?.onPermissionGranted()
                }
                shouldShowPermissionRationaleDialog -> {
                    showPermissionRationaleDialog {
                        launchSinglePermissionRequest()
                    }
                }
                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }

        multiplePermissionRequestLauncher = hostFragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.entries.all {
                it.value
            }

            val shouldShowPermissionRationaleDialog = checkPermissionRationale(
                activity,
                requiredPermissions
            )

            when {
                granted -> {
                    callback?.onPermissionGranted()
                }
                shouldShowPermissionRationaleDialog -> {
                    showPermissionRationaleDialog {
                        launchMultiplePermissionRequest()
                    }
                }
                else -> {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val explanationDialog = CommonMessageShowingBottomDialog(
            activity.getString(R.string.permission_denied),
            activity.getString(R.string.denied_permissions_can_be_allowed_from_settings),
            activity.getString(R.string.go_to_settings),
            object :  CommonMessageShowingBottomDialog.CommonMessageDialogCallback {
                override fun onOkPressed() {
                    goToSettings(activity, BuildConfig.APPLICATION_ID)
                }
            })
        explanationDialog.show(activity.supportFragmentManager, "#permission_denied_dialog")
    }

    private fun launchSinglePermissionRequest() = singlePermissionRequestLauncher.launch(permission)
    private fun launchMultiplePermissionRequest() = multiplePermissionRequestLauncher.launch(requiredPermissions)

    fun requestSinglePermission(permission: String, rationaleMsg: String, listener: PermissionRequestResultCallback) {
        this.permission = permission
        this.rationaleMsg = rationaleMsg
        this.callback = listener

        when {
            activity.checkSelfPermissionCompat(permission) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                callback?.onPermissionGranted()
            }
            activity.shouldShowRequestPermissionRationaleCompat(permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                showPermissionRationaleDialog {
                    launchMultiplePermissionRequest()
                }
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                launchMultiplePermissionRequest()
            }
        }
    }

    fun requestMultiplePermissions(permissions: Array<String>, rationaleMsg: String, listener: PermissionRequestResultCallback) {
        requiredPermissions = permissions
        this.rationaleMsg = rationaleMsg
        this.callback = listener

        val shouldShowRequestPermissionRationale = checkPermissionRationale(
            activity,
            requiredPermissions
        )
        when {
            checkPermission(activity, requiredPermissions) -> {
                // You can use the API that requires the permission.
                callback?.onPermissionGranted()
            }
            shouldShowRequestPermissionRationale -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                showPermissionRationaleDialog {
                    launchMultiplePermissionRequest()
                }
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                launchMultiplePermissionRequest()
            }
        }
    }

    private fun showPermissionRationaleDialog(action: () -> Unit) {
        val explanationDialog = CommonMessageShowingBottomDialog(
            activity.getString(R.string.allow_permission),
            rationaleMsg, activity.getString(R.string.ok_string),
            object :  CommonMessageShowingBottomDialog.CommonMessageDialogCallback {
                override fun onOkPressed() {
                    action()
                }
            })
        explanationDialog.show(activity.supportFragmentManager, "#permission_rationale_dialog")
    }

    interface PermissionRequestResultCallback {
        fun onPermissionGranted()
    }

    companion object {
        fun checkPermission(activity: AppCompatActivity, permissions: Array<String>): Boolean = permissions.all { permission ->
            activity.checkSelfPermissionCompat(permission) == PackageManager.PERMISSION_GRANTED
        }

        fun checkPermissionRationale(activity: AppCompatActivity, permissions: Array<String>): Boolean = permissions.all { permission ->
            activity.shouldShowRequestPermissionRationaleCompat(permission)
        }

        fun goToSettings(activity: AppCompatActivity, packageName: String) {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also { intent ->
                activity.startActivity(intent)
            }
        }

        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES) + arrayOf(Manifest.permission.CAMERA)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) + arrayOf(Manifest.permission.CAMERA)
        }

        val onlyGalleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val onlyCameraPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES) + arrayOf(Manifest.permission.CAMERA)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) + arrayOf(Manifest.permission.CAMERA)
        }
    }
}