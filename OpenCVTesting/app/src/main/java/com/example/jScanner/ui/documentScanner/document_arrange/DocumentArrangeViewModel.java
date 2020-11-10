package com.example.jScanner.ui.documentScanner.document_arrange;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

import com.example.jScanner.Model.ScannedImage;
import com.example.jScanner.ui.documentScanner.document_reader.DocumentReaderFragmentDirections;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DocumentArrangeViewModel extends ViewModel {

    private LinkedList<ScannedImage> mScannedImageLinkedList;

    public void init(Bundle bundle){
        DocumentArrangeFragmentArgs args = DocumentArrangeFragmentArgs.fromBundle(bundle);
        mScannedImageLinkedList =  new LinkedList<>(Arrays.asList(args.getScannedImageList()));
    }

    public LinkedList<ScannedImage> getScannedImageList(){
        return mScannedImageLinkedList;
    }
}