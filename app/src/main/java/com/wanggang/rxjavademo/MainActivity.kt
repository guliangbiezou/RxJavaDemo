package com.wanggang.rxjavademo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.SimpleAdapter
import android.widget.TextView
import com.wanggang.rxjavademo.util.LogUtil
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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
        (pop.contentView as ListView).setOnItemClickListener { adapterView, view, i, l ->
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
    }

    fun mainClick(v: View) {
        when (v.id) {
            R.id.bt_check_observable -> {
                popupWindow.showAsDropDown(bt_check_observable)
            }
            R.id.bt_create -> create()
            R.id.bt_delay -> delay()
            R.id.bt_do -> testDo()
        }
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
        observable.subscribe(observer)
    }

    private fun delay() {
        observable.delay(5, TimeUnit.SECONDS)
            .subscribeOn(AndroidSchedulers.mainThread()).subscribe(observer)
    }



}
