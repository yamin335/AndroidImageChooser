package mollah.yamin.androidimagechooser.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.math.BigDecimal
import java.math.RoundingMode

fun Double.toRounded(digit: Int): Double {
    return BigDecimal(this).setScale(digit, RoundingMode.HALF_UP).toDouble()
}
fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission)

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
