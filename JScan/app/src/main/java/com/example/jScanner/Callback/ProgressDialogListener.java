package com.example.jScanner.Callback;

public interface ProgressDialogListener {
    void onShowProgressDialog(String message);

    void onUpdateProgressDialog(String message);

    void onDismissProgressDialog();
}
