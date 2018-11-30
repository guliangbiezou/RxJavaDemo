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
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
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
            R.id.bt_buffer -> buffer()
            R.id.bt_concat -> btConcat()
            R.id.bt_merge -> btMerge()
            R.id.bt_zip -> btZip()
            R.id.bt_combineLatest -> btCombineLatest()
            R.id.bt_reduce -> btReduce()
        }
    }

    @SuppressLint("CheckResult")
    private fun btReduce() {
        Observable.just(1,2,3).observeOn(AndroidSchedulers.mainThread()).reduce<String>("哈哈") { str, num ->
            printExecute("reduce $str")
            str+num
        }.subscribe { str ->
            //此处返回最后的结果 哈哈123
            printExecute("subscribe $str")
        }
    }

    private fun btCombineLatest() {
//        Test.testCombineLatest()
        //参数中的两个Observable无论是谁其中一个发送了onNext事件，都会与另外一个observable最新发送的onNext发送的参数进行组合
        //(若此时另外一个的第一个onNext事件还未发出来那么就不会合并，必须两个observable都有onNext事件发生时才会进行合并)
        //然后再发送一次组合之后的事件
        Observable.combineLatest<Int,String,String>(Observable.create<Int> {
            printExecute("发送了事件1")
            it.onNext(1)
            Thread.sleep(1000)
            printExecute("发送了事件2")
            it.onNext(2)
            printExecute("发送了事件3")
            it.onNext(3)
            printExecute("发送了事件4")
            it.onNext(4)
            printExecute("发送了事件5")
            it.onNext(5)
            printExecute("发送了事件6")
            it.onNext(6)
//            Thread.sleep(1000)
            it.onComplete()
        }.subscribeOn(Schedulers.computation()),Observable.create<String> {
            Thread.sleep(9000)
            printExecute("发送了事件A")
            it.onNext("A")
            printExecute("发送了事件B")
            it.onNext("B")
            Thread.sleep(3000)
            printExecute("发送了事件C")
            it.onNext("C")
//            Thread.sleep(3000)
            printExecute("发送了事件D")
            it.onNext("D")
//            Thread.sleep(3000)
            it.onComplete()
        }.subscribeOn(Schedulers.io()),
            BiFunction { t1, t2 ->
                printExecute("BiFunction t1=$t1 t2=$t2")
                t1.toString()+t2
            }).subscribe(object :Observer<String>{
            override fun onComplete() {
                printExecute("onComplete")
            }

            override fun onSubscribe(d: Disposable) {
                printExecute("onSubscribe",d)
            }

            override fun onNext(t: String) {
                printExecute("onNext",t)
            }

            override fun onError(e: Throwable) {
                printExecute("onError",e)
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun btZip() {
//        Observable.zip<Int,String,String>(Observable.just(1,2,3,4).delay(4,TimeUnit.SECONDS),Observable.just("a","b","c"),
//            BiFunction { t1, t2 ->
//                printExecute("BiFunction t1=$t1 t2=$t2")
//                t1.toString()+t2
//            })
//            .subscribe {
//                printExecute("subscribe +$it")
//            }

        Test.testZip()
        Observable.zip<Int,String,String>(Observable.create<Int> {
            printExecute("发送了事件1")
            it.onNext(1)
            Thread.sleep(1000)
            printExecute("发送了事件2")
            it.onNext(2)
//            Thread.sleep(1000)
            it.onComplete()
        }.subscribeOn(Schedulers.computation()),Observable.create<String> {
            printExecute("发送了事件A")
            it.onNext("A")
            Thread.sleep(9000)
            printExecute("发送了事件B")
            it.onNext("B")
            Thread.sleep(3000)
            printExecute("发送了事件C")
            it.onNext("C")
//            Thread.sleep(3000)
            printExecute("发送了事件D")
            it.onNext("D")
//            Thread.sleep(3000)
            it.onComplete()
        }.subscribeOn(Schedulers.io()),
            BiFunction { t1, t2 ->
                printExecute("BiFunction t1=$t1 t2=$t2")
                t1.toString()+t2
            })
            .subscribe(object :Observer<String>{
                override fun onComplete() {
                    printExecute("onComplete")
                }

                override fun onSubscribe(d: Disposable) {
                    printExecute("onSubscribe",d)
                }

                override fun onNext(t: String) {
                    printExecute("onNext",t)
                }

                override fun onError(e: Throwable) {
                    printExecute("onError",e)
                }
            })
    }


    private fun btMerge() {
        //merge参数数量<=4 mergeArray参数数量>0
        //将多个参数合并一起按事件线并行发送事件 所有事件按照时间的先后顺序来发送
        Observable.merge(Observable.just(1,2,3),
            Observable.just(4,5,6),
            Observable.fromIterable(arrayListOf(7,8,9)).delay(4,TimeUnit.SECONDS),
            Observable.fromIterable(arrayListOf(10)))
            .delay(4,TimeUnit.SECONDS)
            .subscribe(observer)
        Observable.mergeArray(Observable.just(80),
            Observable.just(70),
            Observable.just(60),
            Observable.just(50),
            Observable.just(40),
            Observable.just(30),
            Observable.just(20),
            Observable.just(11))
            .subscribe(observer)
        Observable.mergeDelayError(Observable.just<Int>(11,22,33),
            Observable.create<Int> {
                it.onNext(44)
                it.onNext(55)
                it.onError(Exception())
                it.onNext(66)
            }).subscribe(observer)
    }

    private fun btConcat() {
        //concat参数数量<=4 concatArray参数数量>0
        //将多个参数合并一起顺序发送事件 无论中间是否有耗时事件，所有事件都按既定顺序，顺序发送
        Observable.concat(Observable.just(1,2,3),
            Observable.just(4,5,6),
            Observable.fromIterable(arrayListOf(7,8,9)).delay(4,TimeUnit.SECONDS),
            Observable.fromIterable(arrayListOf(10)))
            .delay(4,TimeUnit.SECONDS)
            .subscribe(observer)
        Observable.concatArray(Observable.just(80),
            Observable.just(70),
            Observable.just(60),
            Observable.just(50),
            Observable.just(40),
            Observable.just(30),
            Observable.just(20),
            Observable.just(11))
            .subscribe(observer)
        Observable.concatArrayDelayError(Observable.just<Int>(11,22,33),
            Observable.create<Int> {
                it.onNext(44)
                it.onNext(55)
                it.onError(Exception())
                it.onNext(66)
            }).subscribe(observer)
    }

    private fun buffer() {
        observable.buffer(3,1)
            .flatMap {
                Observable.fromIterable(it)
            }
            .subscribe(observer)
    }

    @SuppressLint("CheckResult")
    private fun map() {
        observable.map {
            "this is $it"
        }.subscribe {
            printExecute("subscribe $it")
        }
    }

    @SuppressLint("CheckResult")
    private fun flatMap() {
        observable.concatMap {
            val list = arrayListOf<String>()
            for (i in 0..3) {
                list.add("事件$it 拆分事件$i")
            }
//            return@concatMap Observable.fromIterable(list)
            return@concatMap if (it != 11)
            Observable.fromIterable(list).delay(2,TimeUnit.SECONDS)
            else
                Observable.fromIterable(list)
//            return@concatMap Observable.just("事件$it")
        }.subscribe({
            printExecute("subscribe $it")
        },{
            it.printStackTrace()
        })
        //这里如果observable中有发送onError事件如果订阅的时候单纯使用Consumer或者lam表达式而没有处理onError就会报错
        //并且直接创建的Observable和使用create创建的有些许区别，若用直接创建的调用会报observer为Null的错误直接崩溃
    }

    private fun btRepeat() {
        observable.repeat(3).subscribe(observer)
    }

    fun dd(str:String,d:D):String = d.d(str)

    interface D{
        fun d(str:String):String
    }

    private fun retry() {
//        observable.retryWhen {
//            //当此处返回的Observable调用 onError 或者 onComplete 原Observable不再重试，否则原Observable会重试直到该Observable
//            //调用onError 或者 onComplete其中一个
////            Observable.just(1)
//            //该流通过查看源码最先调用的是当前代码块
//            //并且只有从此表达式中的参数转换后的返回值才能达到预期效果，如果不是由该表达式中的Observable参数通过flatMap转换则达不到上述效果
//             it.flatMap { t->
//                if (t is Exception) {
//                    return@flatMap Observable.just(1,2,3)
//                }
//                Observable.error<Throwable>(t)
//            }
////            printExecute("retryWhen lam")
////           return@retryWhen Observable.just(1)
////               .doOnNext {
////                   ObservableFactory.time = it
////                   printExecute("doOnNext $it")
////               }
//        }.subscribe(observer)
////        Test.testRetryWhen()
////        testRetryWhen()

        //retry与repeat retry需要出现错误即发送了onError事件时会重试 而repeat不会在意是否有onError事件被调用了
        observable.retry(3).subscribe(observer)
    }

    private fun testRetryWhen() {
//        Observable.create(ObservableOnSubscribe<Int> { e ->
//            e.onNext(1)
//            e.onNext(2)
//            e.onError(Exception("发生错误了"))
//            e.onNext(3)
//        })
            // 遇到error事件才会回调
            Observable.create<Int> { e ->
                e.onNext(1)
                e.onNext(2)
                e.onError(Exception("发生错误了"))
                e.onNext(3)
            }.retryWhen { throwableObservable ->
                // 参数Observable<Throwable>中的泛型 = 上游操作符抛出的异常，可通过该条件来判断异常的类型
                // 返回Observable<?> = 新的被观察者 Observable（任意类型）
                // 此处有两种情况：
                // 1. 若 新的被观察者 Observable发送的事件 = Error事件，那么 原始Observable则不重新发送事件：
                // 2. 若 新的被观察者 Observable发送的事件 = Next事件 ，那么原始的Observable则重新发送事件：
                throwableObservable.flatMap<Any> {
                    // 1. 若返回的Observable发送的事件 = Error事件，则原始的Observable不重新发送事件
                    // 该异常错误信息可在观察者中的onError（）中获得
                    Observable.error( Throwable("retryWhen终止啦"));

                    // 2. 若返回的Observable发送的事件 = Next事件，则原始的Observable重新发送事件（若持续遇到错误，则持续重试）
//                    Observable.just(1)
                }
            }
            .subscribe(observer)
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
