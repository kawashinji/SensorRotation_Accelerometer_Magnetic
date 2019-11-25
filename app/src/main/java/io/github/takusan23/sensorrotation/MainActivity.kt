package io.github.takusan23.sensorrotation

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    lateinit var sensorManager: SensorManager
    lateinit var sensorEventListener: SensorEventListener

    //加速度の値。配列になっている
    var accelerometerList = floatArrayOf()
    //磁気の値。こちらも配列になっている
    var magneticList = floatArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //加速度
        val accelerometer = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)
        //磁気
        val magnetic = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD)
        //受け取る
        sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //つかわん
            }

            override fun onSensorChanged(event: SensorEvent?) {
                //値はここで受けとる
                when (event?.sensor?.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        //加速度
                        accelerometerList = event.values.clone()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        //地磁気
                        magneticList = event.values.clone()
                    }
                }
                //配列に値があることを確認
                if (accelerometerList.isNotEmpty() && magneticList.isNotEmpty()) {
                    /*
                    * こっから先は何やってるかわからん！
                    * 回転行列ってなに？？？
                    * ここみて→https://developer.android.com/guide/topics/sensors/sensors_position
                    * */
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        accelerometerList,
                        magneticList
                    )
                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    //画面回転は配列の３番目の値で-1か1のときに横になる。
                    val yokoTate = if (orientationAngles[2].roundToInt() >= 1) {
                        "よこ"
                    } else if (orientationAngles[2].roundToInt() <= -1) {
                        "よこ"
                    } else {
                        "たて"
                    }
                    //画面回転する
                    when (orientationAngles[2].roundToInt()) {
                        -1 -> {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                        1 -> {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                        else -> {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                    }
                    textview.text = """

                    方位角（z 軸に関する回転度数）：${orientationAngles[0]}
                    勾配（x 軸に関する回転度数）${orientationAngles[1]}
                    回転（y 軸に関する回転度数）：${orientationAngles[2]}

                    端末の向きは：$yokoTate

                """.trimIndent()
                }
            }
        }
        //加速度センサー登録
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer[0],  //配列のいっこめ。
            SensorManager.SENSOR_DELAY_NORMAL  //更新頻度
        )

        //磁気センサー登録
        sensorManager.registerListener(
            sensorEventListener,
            magnetic[0],  //配列のいっこめ。
            SensorManager.SENSOR_DELAY_NORMAL  //更新頻度
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }

}
