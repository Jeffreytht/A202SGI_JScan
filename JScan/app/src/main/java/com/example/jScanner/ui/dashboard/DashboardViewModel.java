package com.example.jScanner.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.jScanner.Callback.CommonResultListener;
import com.example.jScanner.Callback.ProgressDialogListener;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.utility.Database;
import com.example.jScanner.utility.User;

import java.util.ArrayList;
import java.util.Collections;

public class DashboardViewModel extends ViewModel implements CommonResultListener<ArrayList<ScannedDocument>> {
    private final MutableLiveData<ArrayList<ScannedDocument>> mScannedDocument = new MutableLiveData<>(new ArrayList<>());
    private ProgressDialogListener mDocumentReceivedListener;

    public LiveData<ArrayList<ScannedDocument>> getScannedDocument() {
        return this.mScannedDocument;
    }

    public void initDocument(ProgressDialogListener listener) {
        mDocumentReceivedListener = listener;
        listener.onShowProgressDialog("Loading documents");
        Database.getBriefDocument(User.getUser(), this);
    }

    @Override
    public void onResultReceived(ArrayList<ScannedDocument> scannedDocumentList) {
        final ArrayList<ScannedDocument> scannedDocuments = mScannedDocument.getValue() == null ? new ArrayList<>() : mScannedDocument.getValue();
        scannedDocuments.clear();
        scannedDocumentList.sort(Collections.reverseOrder());
        scannedDocuments.addAll(scannedDocumentList);
        mScannedDocument.setValue(scannedDocuments);
        mDocumentReceivedListener.onDismissProgressDialog();
    }

    public void removeDocument(ScannedDocument scannedDocument) {
        mScannedDocument.getValue().remove(scannedDocument);
    }

    public int indexOfScannedDocument(ScannedDocument scannedDocument) {
        ArrayList<ScannedDocument> scannedDocuments = mScannedDocument.getValue();
        return scannedDocuments.indexOf(scannedDocument);
    }
}