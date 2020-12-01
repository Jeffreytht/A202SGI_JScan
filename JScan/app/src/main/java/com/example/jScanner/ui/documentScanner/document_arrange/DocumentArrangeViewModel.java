package com.example.jScanner.ui.documentScanner.document_arrange;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;

import java.util.List;

public class DocumentArrangeViewModel extends ViewModel {

    private ScannedDocument mScannedDocument;

    public void init(Bundle bundle){
        DocumentArrangeFragmentArgs args = DocumentArrangeFragmentArgs.fromBundle(bundle);
        mScannedDocument =  args.getScannedDocument();
    }

    public List<ScannedImage> getScannedImageList() {return mScannedDocument.getScannedImageList();}
}