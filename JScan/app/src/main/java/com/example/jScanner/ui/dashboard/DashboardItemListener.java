package com.example.jScanner.ui.dashboard;

import com.example.jScanner.Model.ScannedDocument;

public interface DashboardItemListener {
    void onShareClicked(ScannedDocument scannedDocument);

    void onRenameClicked(ScannedDocument scannedDocument);

    void onModifyClicked(ScannedDocument scannedDocument);

    void onDeleteClicked(ScannedDocument scannedDocument);

    void onImageClicked(ScannedDocument scannedDocument);

    void onShareableLinkClicked(ScannedDocument scannedDocument);
}
