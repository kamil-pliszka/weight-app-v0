package com.pl.myweightapp.core.data.networking

import com.pl.myweightapp.core.domain.util.NetworkError
import com.pl.myweightapp.core.domain.util.Result
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException


suspend inline fun <reified T> safeNetworkCall(
    execute: () -> HttpResponse
): Result<T, NetworkError> {
    val response = try {
        execute()
    } catch(e: UnresolvedAddressException) {
        return Result.Error(NetworkError.NO_INTERNET)
    } catch(e: SerializationException) {
        return Result.Error(NetworkError.SERIALIZATION)
    } catch(e: Exception) {
        currentCoroutineContext().ensureActive()
        return Result.Error(NetworkError.UNKNOWN)
    }

    return responseToResult(response)
}