package io.getstream.chat.android.client.poc.utils

import io.getstream.chat.android.client.Result
import io.getstream.chat.android.client.call.ChatCallImpl


class SuccessCall<T>(val result: T) : ChatCallImpl<T>() {
    override fun execute(): Result<T> {
        return Result(result, null)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(Result(result, null))
    }
}