package com.example.jScanner.Callback;

import com.example.jScanner.Model.ScannedDocument;

import java.util.List;
import java.util.Map;

public interface DbGetDocumentCallback {
    void onDocumentReceived(List<ScannedDocument> scannedDocumentList);
}
