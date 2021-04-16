package com.er453r.vector

import Anki.Vector.external_interface.Behavior
import Anki.Vector.external_interface.ExternalInterfaceGrpc
import Anki.Vector.external_interface.Messages
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyChannelBuilder
import java.io.File

class VectorAPI(
    ip:String,
    name:String,
    guid:String,
    certPath:String
) {
    private var blocked = false
    private val queue = ArrayList<VectorCommand>()
    private val vectorCallCredentials = VectorCallCredentials(guid)

    private val stub: ExternalInterfaceGrpc.ExternalInterfaceStub

    private fun processQueue(){
        println("[Queue size - ${queue.size}, blocked - ${blocked}]")

        if(queue.size == 0 || blocked)
            return

        val command = queue.removeFirst()

        if(command.blocking)
            lockQueue()

        command.closure.invoke()
    }

    private fun addToQueue(command: VectorCommand){
        println("ADding to queue...")

        queue.add(command)
    }

    private fun lockQueue(){
        println("Locking queue")

        blocked = true
    }

    private fun unlockQueue(){
        println("Unlocking queue")

        blocked = false

        processQueue()
    }

    private fun initSDK(){
        addToQueue(
            VectorCommand(
                blocking = true,
            ){
                println("Init SDK")

                stub.withCallCredentials(vectorCallCredentials)
                    .sDKInitialization(
                        Messages.SDKInitializationRequest.newBuilder()
                            .setSdkModuleVersion("0.6.1.dev0")
                            .setPythonVersion("3.8.0")
                            .setPythonImplementation("PyPy")
                            .setCpuVersion("x86")
                            .setOsVersion("linux")
                            .build(), VectorStreamObserver<Messages.SDKInitializationResponse>(
                            name = "sdk init",
                            onCompleted = {
                                unlockQueue()
                            },
                        )
                    )
            }
        )

        processQueue()
    }

    private fun assumeControl(){
        addToQueue(
            VectorCommand(
                blocking = true,
            ){
                println("Assumoing control...")

                stub.withCallCredentials(vectorCallCredentials).assumeBehaviorControl(
                    Behavior.BehaviorControlRequest.newBuilder()
                        .setControlRequest(
                            Behavior.ControlRequest.newBuilder()
                                .setPriority(Behavior.ControlRequest.Priority.DEFAULT)
                        ).build(), VectorStreamObserver<Behavior.BehaviorControlResponse>(
                            name = "assume control",
                            onNext = {
                                if(it.responseTypeCase == Behavior.BehaviorControlResponse.ResponseTypeCase.CONTROL_GRANTED_RESPONSE)
                                    unlockQueue()
                            }
                        )
                )
            }
        )

        processQueue()
    }


    private fun forward(){
        addToQueue(
            VectorCommand(
                blocking = false,
            ){
                println("Say text")

                stub.withCallCredentials(vectorCallCredentials)
                    .driveStraight(
                        Messages.DriveStraightRequest.newBuilder()
                            .setDistMm(100.0f)
                            .setNumRetries(1)
                            .setSpeedMmps(10.0f)
                            .setShouldPlayAnimation(true)
                            .setIdTag(0)
                            .build(), VectorStreamObserver<Messages.DriveStraightResponse>(
                            name = "drive",
                            onCompleted = {
                                print("Done driving")
                            }
                        )
                    )
            }
        )

        processQueue()
    }


    private fun say(){
        addToQueue(
            VectorCommand(
                blocking = true,
            ){
                println("Say text")

                stub.withCallCredentials(vectorCallCredentials)
                    .sayText(
                        Messages.SayTextRequest.newBuilder()
                            .setText("Hello Vector!")
                            .setUseVectorVoice(true)
                            .setDurationScalar(1.0f)
                            .build(), VectorStreamObserver<Messages.SayTextResponse>(
                                name = "say test",
                                onCompleted = {
                                    unlockQueue()
                                }
                        )
                    )
            }
        )

        processQueue()
    }

    private fun releaseControl(){
        addToQueue(
            VectorCommand(
                blocking = false,
            ){
                println("Releasing control...")

                stub.withCallCredentials(vectorCallCredentials).assumeBehaviorControl(
                    Behavior.BehaviorControlRequest.newBuilder()
                        .setControlRelease(
                            Behavior.ControlRelease.newBuilder()
                        ).build(), VectorStreamObserver<Behavior.BehaviorControlResponse>("relaase")
                )
            }
        )

        processQueue()
    }

    init {
        val channel = NettyChannelBuilder.forAddress(ip, 443)
            .sslContext(
                GrpcSslContexts
                    .forClient()
                    .trustManager(File(certPath))
                    .build()
            )
            .overrideAuthority(name)
            .build()

        stub = ExternalInterfaceGrpc.newStub(channel)

        initSDK()

        assumeControl()
        forward()
        say()
//        releaseControl()

        while (queue.size != 0){
            println("holding off shutdown...")

            Thread.sleep(1000)
        }
    }
}
