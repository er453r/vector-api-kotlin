package com.er453r.vector

import io.grpc.stub.StreamObserver

class VectorStreamObserver<T>(val name:String) : StreamObserver<T> {
    override fun onNext(p0: T) {
        println("[${name}] onNext ${p0}")
    }

    override fun onError(p0: Throwable?) {
        println("[${name}] onError ${p0}")
    }

    override fun onCompleted() {
        println("[${name}] onCompleted")
    }
}
