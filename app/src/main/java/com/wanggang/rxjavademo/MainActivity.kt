package com.wanggang.rxjavademo

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.SimpleAdapter
import android.widget.TextView
import com.wanggang.rxjavademo.util.LogUtil
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

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
        test1()
    }

    private fun test1() {
        var observable:Observable<Int>? = null
        var observer:Observer<Int>? = null
        thread(true) {
            observable = Observable.create<Int> {
                printExecute("create")
                it.onNext(1)
                it.onComplete()
            }
            printExecute("thread1")
        }
        thread(true) {
            observer = object : Observer<Int> {
                override fun onComplete() {
                    printExecute("onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    //订阅发生在什么线程，该方法就在什么线程调用
                    printExecute("onSubscribe")
                }

                override fun onNext(t: Int) {
                    printExecute("onNext")
                }

                override fun onError(e: Throwable) {
                    printExecute("onError")
                }
            }
            printExecute("thread2")
            Thread.sleep(1000)
            observable?.subscribeOn(AndroidSchedulers.mainThread())?.observeOn(Schedulers.io())?.subscribe(observer!!)
        }


    }

    fun mainClick(v: View) {
        when (v.id) {
            R.id.bt_check_observable -> {
                popupWindow.showAsDropDown(bt_check_observable)
            }
            R.id.bt_create -> create()
            R.id.bt_delay -> delay()
            R.id.bt_do -> testDo()
            R.id.bt_onErrorReturn -> onErrorReturn()
            R.id.bt_onErrorResume -> onErrorResumeNext()
            R.id.bt_retry -> retry()
            R.id.bt_repeat -> btRepeat()
            R.id.bt_flatMap -> flatMap()
        }
    }

    @SuppressLint("CheckResult")
    private fun flatMap() {
        observable.flatMap {
            val list = arrayListOf<String>()
            for (i in 0..3) {
                list.add("事件$it 拆分事件$i")
            }
            if (it != 2)
            Observable.fromIterable(list).delay(1,TimeUnit.SECONDS)
            else Observable.fromIterable(list)
        }.subscribe{
            printExecute(it)
        }
    }

    private fun btRepeat() {
        observable.repeat(3).subscribe(observer)
    }

    private fun retry() {
        observable.retryWhen {
            //当此处返回的Observable调用 onError 或者 onComplete 原Observable不再重试，否则原Observable会重试直到该Observable
            //调用onError 或者 onComplete其中一个
//            Observable.just(1)
            it.flatMap { t->
                if (t is Exception) {
                    Observable.just(null)
                }
                Observable.error<Throwable>(t)
            }
        }.subscribe(observer)


    }

    private fun onErrorReturn() {
        observable.onErrorReturn {
            5
        }.subscribe(observer)
    }

    private fun onErrorResumeNext() {
        //onErrorResumeNext 处理的是上游被捕获处理了的Throwable的子类对象，所以当上游抛出的异常为Throwable的子类
        //则onErrorResumeNext 会生效
        observable.onErrorResumeNext(ObservableFactory.observableNNENNC).subscribe(observer)

        //onExceptionResumeNext 只处理上游抛出的异常即Exception 若上游抛出的是一个Throwable或者一个Error
        //该操作符都不会生效
        observable.onExceptionResumeNext(ObservableFactory.observableNNENNC).subscribe(observer)

        //两者比较 onErrorResumeNext接受面相比较于onExceptionResumeNext更广
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
            .subscribe(observer)
    }

    @SuppressLint("CheckResult")
    private fun test() {
        Observable.create<List<String>> {
            it.onNext(getLists())
            it.onComplete()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                printExecute("test subscribe")
            }
//        Observable.just(getLists())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(Schedulers.io())
//            .subscribe {
//                printExecute("test subscribe")
//            }

    }

    private fun getLists():List<String> {
        printExecute("getLists")
        Thread.sleep(3000)
        printExecute("getLists")
        return emptyList()
    }



}
