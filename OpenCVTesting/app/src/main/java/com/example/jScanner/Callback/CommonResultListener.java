package com.example.jScanner.Callback;
import com.google.android.gms.tasks.Task;

public interface CommonResultListener<T> {
    void onResultReceived(T result);
}
