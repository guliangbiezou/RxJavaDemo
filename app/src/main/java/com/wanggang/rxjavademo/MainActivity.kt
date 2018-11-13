package com.wanggang.rxjavademo

import android.content.Context
import android.hardware.display.DisplayManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.SimpleAdapter
import android.widget.TextView
import com.wanggang.rxjavademo.util.SerialPortUtils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RxJavaDemo"
    }

    private var observable = ObservableFactory.observableNNNNC
    private val popupWindow: PopupWindow by lazy {
        val pop = PopupWindow(ListView(this@MainActivity),ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        pop.contentView = ListView(this@MainActivity)
        val list = ObservableFactory.observableNames.map {
            mapOf(Pair("observableName",it))
        }
        (pop.contentView as ListView).adapter = SimpleAdapter(this@MainActivity,
            list,
            android.R.layout.simple_list_item_1, arrayOf("observableName"),
            intArrayOf(android.R.id.text1))
        (pop.contentView as ListView).setOnItemClickListener { _, view, _, _ ->
            bt_check_observable.text = (view as TextView).text
            observable = ObservableFactory.getObservableByName(bt_check_observable.text.toString())
            pop.dismiss()
        }
        pop
    }

    private val observer = object : Observer<Int> {
        override fun onComplete() {
            printExecute("onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            printExecute("onSubscribe",d)
        }

        override fun onNext(t: Int) {
            printExecute("onNext",t)
        }

        override fun onError(e: Throwable) {
            printExecute("onError",e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSecondWindow()
    }

    val handler = Handler()
    private fun initSecondWindow() {
        val displayManager: DisplayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays
        if (displays.size > 1) {
            val secondWindow = SecondWindow(this,displays[displays.size - 1])
            secondWindow.show()
        }

    }

    private fun serial() {
        val serialPortUtils = SerialPortUtils()
        serialPortUtils.openSerialPort()

        serialPortUtils.setOnDataReceiveListener (object :SerialPortUtils.OnDataReceiveListener {
            var mBuffer:ByteArray = byteArrayOf()
            override fun onDataReceive(buffer: ByteArray?, size: Int) {
                Log.d(TAG, "进入数据监听事件中。。。" + String(buffer!!))
                //
                //在线程中直接操作UI会报异常：ViewRootImpl$CalledFromWrongThreadException
                //解决方法：handler
                //
                mBuffer = buffer
                handler.post(runnable)
            }

            //开线程更新UI
            val runnable = Runnable {
                bt_check_observable.text = "size： ${mBuffer.size}数据监听：${mBuffer}"
            }


        })
    }

    fun mainClick(v: View) {
        when (v.id) {
            R.id.bt_check_observable -> {
                popupWindow.showAsDropDown(bt_check_observable)
            }
            R.id.bt_create -> serial()
            R.id.bt_delay -> delay()
            R.id.bt_do -> testDo()
            R.id.bt_onErrorResume -> onErrorResumeNext()
        }
    }

    private fun onErrorResumeNext() {
        observable.onExceptionResumeNext(ObservableFactory.observableNNNNC).subscribe(observer)
    }

    private fun testDo() {
            observable.doOnEach {
                printExecute("doOnEach",it)
            }.doOnNext {
                printExecute("doOnNext",it)
            }.doOnError {
                printExecute("doOnError",it)
            }.doAfterNext {
                printExecute("doAfterNext",it)
            }.doOnTerminate {
                printExecute("doOnTerminate")
            }.doAfterTerminate {
                printExecute("doAfterTerminate")
            }.doOnComplete {
                printExecute("doOnComplete")
            }.doFinally {
                printExecute("doFinally")
            }.subscribe(observer)
    }

    private fun create() {
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer)
    }

    private fun delay() {
        observable.delay(5, TimeUnit.SECONDS)
            .subscribeOn(AndroidSchedulers.mainThread()).subscribe(observer)
    }



}
