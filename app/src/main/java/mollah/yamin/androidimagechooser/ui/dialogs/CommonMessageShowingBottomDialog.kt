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

/**
 * Created by YAMIN on January 21, 2021.
 */
class CommonMessageShowingBottomDialog(
    private val title: String = "",
    private val message: String = "",
    private val okButtonText: String = "",
    private val callback: CommonMessageDialogCallback?
) : BottomSheetDialogFragment() {

    private lateinit var binding: CommonMessageDialogBinding
    private val dataBindingComponent: DataBindingComponent = AppDataBindingComponent()

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    interface CommonMessageDialogCallback {
        fun onOkPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CommonMessageDialogBinding.inflate(inflater)
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

        if (title.isEmpty()) {
            binding.tvTitle.visibility = View.GONE
        } else {
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvTitle.text = title
        }

        if (message.isEmpty()) {
            binding.tvMessage.visibility = View.GONE
        } else {
            binding.tvMessage.visibility = View.VISIBLE
            binding.tvMessage.text = message
        }

        if (okButtonText.isNotBlank()) {
            binding.btnOk.text = okButtonText
        }

        binding.btnOk.setOnClickListener {
            callback?.onOkPressed()
            dismiss()
        }
    }
}