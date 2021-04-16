package com.er453r.vector

import io.grpc.Attributes
import io.grpc.CallCredentials
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import java.util.concurrent.Executor

class VectorCallCredentials(val token: String) : CallCredentials {
    override fun applyRequestMetadata(method: MethodDescriptor<*, *>, attrs: Attributes, appExecutor: Executor, applier: CallCredentials.MetadataApplier) {
        appExecutor.execute {
            val headers = io.grpc.Metadata()
            headers.put(io.grpc.Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), String.format("%s %s", "Bearer", token));

            applier.apply(headers)
        }
    }

    override fun thisUsesUnstableApi() {
        throw Exception("This should never happen")
    }
}
