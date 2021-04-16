package com.er453r

import Anki.Vector.external_interface.Behavior
import Anki.Vector.external_interface.ExternalInterfaceGrpc
import Anki.Vector.external_interface.Messages
import com.er453r.vector.VectorCallCredentials
import com.er453r.vector.VectorStreamObserver
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyChannelBuilder
import java.io.File

fun main() {
    println("Vector SDK test")

    val ip = "192.168.1.57"
    val name = "Vector-W5Z1"
    val guid = "Tk3G1xdBQg2odpGBdPjIhw=="
    val certFile = File("/home/mkotz/.anki_vector/Vector-W5Z1-00801b24.cert")

    val vectorCallCredentials = VectorCallCredentials(guid)

     val channel = NettyChannelBuilder.forAddress(ip, 443)
        .sslContext(
            GrpcSslContexts
                .forClient()
                .trustManager(certFile)
                .build()
        )
        .overrideAuthority(name)
        .build()

    val stub = ExternalInterfaceGrpc.newStub(channel)

    println("Test call...")

    stub.withCallCredentials(vectorCallCredentials)
        .sDKInitialization(
            Messages.SDKInitializationRequest.newBuilder()
                .setSdkModuleVersion("0.6.1.dev0")
                .setPythonVersion("3.8.0")
                .setPythonImplementation("PyPy")
                .setCpuVersion("x86")
                .setOsVersion("linux")
                .build(), VectorStreamObserver<Messages.SDKInitializationResponse>("sdk init")
        )

    Thread.sleep(1000)

    println("Test control...")

    stub.withCallCredentials(vectorCallCredentials).assumeBehaviorControl(
        Behavior.BehaviorControlRequest.newBuilder()
            .setControlRequest(
                Behavior.ControlRequest.newBuilder()
                    .setPriority(Behavior.ControlRequest.Priority.DEFAULT)
            ).build(), VectorStreamObserver<Behavior.BehaviorControlResponse>("assume control")
    )

    Thread.sleep(10000)
    println("Test say...")

    stub.withCallCredentials(vectorCallCredentials)
        .sayText(
            Messages.SayTextRequest.newBuilder()
                .setText("Hello Vector!")
                .setUseVectorVoice(true)
                .setDurationScalar(1.0f)
                .build(), VectorStreamObserver<Messages.SayTextResponse>("say test")
        )

    Thread.sleep(5000)
    println("Release control...")

    stub.withCallCredentials(vectorCallCredentials).assumeBehaviorControl(
        Behavior.BehaviorControlRequest.newBuilder()
            .setControlRelease(
                Behavior.ControlRelease.newBuilder()
            ).build(), VectorStreamObserver<Behavior.BehaviorControlResponse>("relaase")
    )

    Thread.sleep(10000)
    println("Bye...")
}
