package com.example.jScanner.utility;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;

import com.example.jScanner.Model.ScannedImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFBuilder {
    public static byte[] PDFToBytes(List<ScannedImage> scannedImageList){

//        if(!fileName.endsWith(".pdf")) fileName += ".pdf";
//        final File file = new File(path, fileName);

        PdfDocument pdfDocument = new PdfDocument();
        int height = 1010;
        int width = 714;

        int reqH, reqW;
        reqW = width;

//        LinkedList<ScannedImage> scannedImageList = mScannedDocument.getValue().getScannedImageList();

        for (ScannedImage scannedImage: scannedImageList) {
            Bitmap bitmap = scannedImage.getFinalImage();

            reqW = Math.max(reqW, bitmap.getWidth());
            reqH = Math.max(width * bitmap.getHeight() / bitmap.getWidth(), height);

            if (reqH >= height) {
                reqH = height;
                reqW = height * bitmap.getWidth() / bitmap.getHeight();
            }

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(reqW, reqH, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            canvas.drawBitmap(bitmap, 0, 0, null);

            pdfDocument.finishPage(page);
        }


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] pdfByteArray = null;
//        FileOutputStream fos;
        try {
//            fos = new FileOutputStream(file);
            pdfDocument.writeTo(bos);
            pdfDocument.close();
            pdfByteArray = bos.toByteArray();
            bos.close();
//            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pdfByteArray;
    }
//
//    public PdfDocument bytesToPDF(byte[] bytes){
//
//        PdfDocument document;
//        document.r
//
//        FileOutputStream fos;
//        try {
//            fos = new FileOutputStream(file);
//            fos.write(bytes);
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
