package com.wanggang.rxjavademo.kotlinExt

abstract class ObservableKTX<T> {

    private var doOnEach:(() -> Unit)? = null
    private var doOnNext:(() -> Unit)? = null

    private var subscribes: ArrayList<ObservableKTX<T>> = arrayListOf()

    fun doOnEach(lambdaDoOnEach:() -> Unit):ObservableKTX<T> {
        this.doOnEach = lambdaDoOnEach
        return this
    }

    fun doOnNext(lambdaDoOnNext:() -> Unit): ObservableKTX<T> {
        this.doOnNext = lambdaDoOnNext
        return this
    }

    fun subscribe(observerKTX:ObserverKTX<T>) {
        observerKTX.onSubscribe()
        onSubscribe(observerKTX)
    }

    fun onSubscribe(observerKTX:ObserverKTX<T>) {
        subscribeAtual(ObserverEmmiter(observerKTX,this))
    }

    abstract fun subscribeAtual(observerKTX:ObserverKTX<T>)

    inner class ObserverEmmiter<T>(val observerKTX:ObserverKTX<T>,val observableKTX: ObservableKTX<T>): ObserverKTX<T> {

        override fun onNext(param: T) {
            observableKTX.doOnEach?.invoke()
            observableKTX.doOnNext?.invoke()
            observerKTX.onNext(param)
        }

        override fun onComplete() {
            observableKTX.doOnEach?.invoke()
            observerKTX.onComplete()
        }

        override fun onErro(throwable: Throwable) {
            observableKTX.doOnEach?.invoke()
            observerKTX.onErro(throwable)
        }

        override fun onSubscribe() {
            observerKTX.onSubscribe()
        }
    }


}



interface  ObserverKTX<T> {

     fun onNext(param:T)

     fun onComplete()

     fun onErro(throwable: Throwable)

     fun onSubscribe()

}