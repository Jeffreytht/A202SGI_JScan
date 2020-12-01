package com.example.jScanner.Callback;

public interface BiResultListener<T, U> {
    void onResultReceived(T result1, U result2);
}
