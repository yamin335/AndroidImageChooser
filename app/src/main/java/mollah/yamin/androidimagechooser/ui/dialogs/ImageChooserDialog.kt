package mollah.yamin.androidimagechooser.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import mollah.yamin.androidimagechooser.R
import mollah.yamin.androidimagechooser.binding.AppDataBindingComponent
import mollah.yamin.androidimagechooser.databinding.CommonMessageDialogBinding
import mollah.yamin.androidimagechooser.databinding.ImageChooserDialogBinding

/**
 * Created by YAMIN on January 21, 2021.
 */
class ImageChooserDialog(
    private val callback: ImageChooserCallback
) : BottomSheetDialogFragment() {

    private lateinit var binding: ImageChooserDialogBinding
    private val dataBindingComponent: DataBindingComponent = AppDataBindingComponent()

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    interface ImageChooserCallback {
        fun openCamera()
        fun openGallery()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ImageChooserDialogBinding.inflate(inflater)
        // Inflate the layout for this fragment
//        binding = DataBindingUtil.inflate(
//            inflater,
//            R.layout.dialog_common_message,
//            container,
//            false,
//            dataBindingComponent
//        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.openCamera.setOnClickListener {
            callback.openCamera()
            dismiss()
        }

        binding.openGallery.setOnClickListener {
            callback.openGallery()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}