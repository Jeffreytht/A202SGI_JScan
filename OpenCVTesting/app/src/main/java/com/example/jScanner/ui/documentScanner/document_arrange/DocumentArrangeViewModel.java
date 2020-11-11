package com.example.jScanner.ui.documentScanner.document_arrange;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;

import java.util.LinkedList;

public class DocumentArrangeViewModel extends ViewModel {

    private ScannedDocument mScannedDocument;

    public void init(Bundle bundle){
        DocumentArrangeFragmentArgs args = DocumentArrangeFragmentArgs.fromBundle(bundle);
        mScannedDocument =  args.getScannedDocument();
    }

    public ScannedDocument getScannedDocument(){
        return mScannedDocument;
    }
    public LinkedList<ScannedImage> getScannedImageList() {return mScannedDocument.getScannedImageList();}
}