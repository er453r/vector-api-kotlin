package com.er453r.vector

import io.grpc.stub.StreamObserver

class VectorStreamObserver<T>(
    val name: String,
    val onNext: ((T) -> Unit)? = null,
    val onError: ((Throwable) -> Unit)? = null,
    val onCompleted: (() -> Unit)? = null,
) : StreamObserver<T> {
    override fun onNext(event: T) {
        println("[${name}] onNext $event")

        onNext?.invoke(event)
    }

    override fun onError(error: Throwable) {
        println("[${name}] onError $error")

        onError?.invoke(error)
    }

    override fun onCompleted() {
        println("[${name}] onCompleted")

        onCompleted?.invoke()
    }
}
