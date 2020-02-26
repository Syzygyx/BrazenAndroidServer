package com.doozycod.brazenserver.twilio_chat_unsused;

public interface TaskCompletionListener<T, U> {
    void onSuccess(T t);
    void onError(U u);
}
