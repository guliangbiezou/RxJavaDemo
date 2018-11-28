package com.wanggang.rxjavademo

import com.wanggang.rxjavademo.util.LogUtil
import io.reactivex.Observable
import io.reactivex.Observer
import kotlin.Error
import kotlin.Exception

fun printExecute(funName: String, param: Any? = null) {
//    LogUtil.e( "observer $funName paramClass = ${if (param != null) param::class.java.name else null} param = $param threadName = ${Thread.currentThread().name} time = ${System.currentTimeMillis()}")
    LogUtil.e("observer $funName param = $param threadName = ${Thread.currentThread().name} time = ${System.currentTimeMillis()}")
}

object ObservableFactory {

    private val TAG = "RxJavaDemo"

    val observableNames = arrayListOf("observableNNNNC", "observableNNENNC","observabaleNNTNNC")

    fun getObservableByName(name: String): Observable<Int> {
        return when (name) {
            "observableNNNNC" -> observableNNNNC
            "observableNNENNC" -> observableNNENNC
            "observabaleNNTNNC" -> observabaleNNTNNC
            else -> observableNNNNC
        }
    }

    val observableNNNNC: Observable<Int> = object : Observable<Int>() {
        override fun subscribeActual(observer: Observer<in Int>?) {
            LogUtil.e(
                TAG,
                "observableNNNNC subscribeActual start threadName = ${Thread.currentThread().name} time = ${System.currentTimeMillis()}"
            )
            observer?.onNext(1)
            observer?.onNext(2)
            observer?.onNext(3)
            observer?.onNext(4)
            observer?.onComplete()
            LogUtil.e(
                TAG,
                "observableNNNNC subscribeActual end threadName = ${Thread.currentThread().name} time = ${System.currentTimeMillis()}"
            )
        }
    }

    val observableNNENNC: Observable<Int> = object : Observable<Int>() {
        override fun subscribeActual(observer: Observer<in Int>?) {
            LogUtil.e(
                    TAG,
            "observableNNNNC subscribeActual start threadName = ${Thread.currentThread().name} time = ${System.currentTimeMillis()}"
            )
            observer?.onNext(5)
            observer?.onNext(6)
//            observer?.onError(Throwable())
            observer?.onError(Exception())
            observer?.onNext(7)
            observer?.onNext(8)
            observer?.onComplete()
            LogUtil.e(
                TAG,
                "observableNNNNC subscribeActual end threadName = ${Thread.currentThread().name} time = ${System.currentTimeMillis()}"
            )
        }
    }

    val observabaleNNTNNC: Observable<Int> = object: Observable<Int>() {
        override fun subscribeActual(observer: Observer<in Int>?) {
            try {
                observer?.onNext(9)
                observer?.onNext(10)
                throw Throwable()
                observer?.onNext(11)
                observer?.onNext(12)
                observer?.onComplete()
            } catch (e: Throwable) {
                observer?.onError(e)
            }
        }

    }

}