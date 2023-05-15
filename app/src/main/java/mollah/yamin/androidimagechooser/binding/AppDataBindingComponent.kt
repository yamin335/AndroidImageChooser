package mollah.yamin.androidimagechooser.binding

import androidx.databinding.DataBindingComponent

/**
 * A default binding component implementation for data binding adapters.
 */
class AppDataBindingComponent : DataBindingComponent {
    private val adapter = BindingAdapters()
    override fun getBindingAdapters() = adapter
}
