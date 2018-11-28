package com.wanggang.rxjavademo

import com.wanggang.rxjavademo.kotlinExt.ObservableKTX
import com.wanggang.rxjavademo.kotlinExt.ObserverKTX
import org.junit.Test

import org.junit.Assert.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        for (i in 0..1000) {
            println(randomNums())
        }
    }

    private fun randomNums():ArrayList<Int> {
        val arr = arrayListOf<Int>()
        for (i in 0 until 5) {
            arr.add(Random.nextInt(10))
        }
        return arr
    }




    private fun test() {
        val observable = object : ObservableKTX<Int>() {
            override fun subscribeAtual(observerKTX: ObserverKTX<Int>) {
                observerKTX.onNext(1)
                observerKTX.onNext(2)
                observerKTX.onNext(3)
                observerKTX.onComplete()
            }
        }
        val observer = object : ObserverKTX<Int> {
            override fun onNext(param: Int) {
                println("onNext param = $param time = ${System.currentTimeMillis()}")
            }

            override fun onComplete() {
                println("onComplete time = ${System.currentTimeMillis()}")
            }

            override fun onErro(throwable: Throwable) {
                println("onErro time = ${System.currentTimeMillis()}")
            }

            override fun onSubscribe() {
                println("onSubscribe time = ${System.currentTimeMillis()}")
            }
        }
        observable.doOnEach {
            println("doOnEach time = ${System.currentTimeMillis()}")
        }.doOnNext {
            println("doOnNext time = ${System.currentTimeMillis()}")
        }.subscribe(observer)
    }
}
