package mollah.yamin.androidimagechooser.app

import android.app.Application
import androidx.databinding.DataBindingUtil
import mollah.yamin.androidimagechooser.binding.AppDataBindingComponent

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.setDefaultComponent(AppDataBindingComponent())
    }
}