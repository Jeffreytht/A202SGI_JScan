package com.example.jScanner.utility;

import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.example.jScanner.Callback.BiResultListener;
import com.example.jScanner.Callback.CommonResultListener;
import com.example.jScanner.Callback.ProgressDialogListener;
import com.example.jScanner.Callback.StatusResultListener;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.util.Executors;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Database {
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private final static Database instance = new Database();
    private final static String COL_USER = "users";
    private final static String FLD_EMAIL = "email";

    private final static String COL_DOCUMENT = "document";
    private final static String FLD_FILE_NAME = "filename";
    private final static String FLD_DATE_ADDED = "date_added";

    private final static String COL_IMAGES = "images";
    private final static String FLD_SEQ = "seq";
    private final static String FLD_BITMAP = "bitmap";
    private final static String FLD_CONTOUR = "contour";
    private final static String FLD_FILTER = "filter";

    private Database(){}

    public static void insertNewUser(FirebaseUser firebaseUser) {
        String uuid = firebaseUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put(FLD_EMAIL, firebaseUser.getEmail());
        instance.mDb.collection(COL_USER).document(uuid).set(user, SetOptions.merge());
    }

    @NonNull
    private static Task<Void> insertImages(@NonNull final FirebaseUser user, @NonNull final ScannedDocument scannedDocument) {
        final CollectionReference ref = instance.mDb.collection(COL_USER).document(user.getUid())
                .collection(COL_DOCUMENT).document(scannedDocument.getId())
                .collection(COL_IMAGES);

        final WriteBatch writeBatch = instance.mDb.batch();
        final List<ScannedImage> scannedImageLinkedList = scannedDocument.getScannedImageList();

        Map<String, Object> imageData = new HashMap<>();

        int seq = 1;

        for (ScannedImage si : scannedImageLinkedList) {
            String path = scannedDocument.getId() + "/" + seq + ".bmp";
            imageData.put(FLD_SEQ, seq);
            imageData.put(FLD_BITMAP, path);
            imageData.put(FLD_CONTOUR, Arrays.asList(si.getContour()));
            imageData.put(FLD_FILTER, si.getFilter());
            writeBatch.set(ref.document(), imageData);
            seq++;
        }

        return writeBatch.commit().continueWith(Task::getResult);
    }

    private static Task<QuerySnapshot> getImagesSnapshot(@NonNull final FirebaseUser user, @NonNull final ScannedDocument scannedDocument){
        try {
            return instance.mDb.collection(COL_USER).document(user.getUid())
                    .collection(COL_DOCUMENT).document(scannedDocument.getId())
                    .collection(COL_IMAGES).get().continueWith(Task::getResult);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private static Task<Object> removeImages(@NonNull QuerySnapshot imageQuerySnapshots){
        try {
            WriteBatch writeBatch = instance.mDb.batch();
            for (DocumentSnapshot s : imageQuerySnapshots)
                writeBatch.delete(s.getReference());
            return writeBatch.commit().continueWith(Task::getResult);
        }catch (Exception ex){
            ex.printStackTrace();
            return  null;
        }
    }

    public static void saveDocument(@NonNull FirebaseUser firebaseUser, @NonNull ScannedDocument scannedDocument){
        String uuid = firebaseUser.getUid();

        HashMap<String, String> data = new HashMap<>();
        data.put(FLD_FILE_NAME, scannedDocument.getName());

        instance.mDb.collection(COL_USER)
                .document(uuid)
                .collection(COL_DOCUMENT)
                .document(scannedDocument.getId())
                .set(data, SetOptions.merge());
    }

    public static void removeDocument(@NonNull FirebaseUser firebaseUser, @NonNull ScannedDocument scannedDocument){
        String uuid = firebaseUser.getUid();
        instance.mDb.collection(COL_USER)
                .document(uuid)
                .collection(COL_DOCUMENT)
                .document(scannedDocument.getId())
                .delete();
    }

    private static void insertNewDocument(@NonNull final FirebaseUser firebaseUser, @NonNull final ScannedDocument scannedDocument, @NonNull final ProgressDialogListener progressDialogListener){
        String uuid = firebaseUser.getUid();

        final CollectionReference collectionReference = instance.mDb.collection(COL_USER).document(uuid).collection(COL_DOCUMENT);

        Map<String, Object> documentData = new HashMap<>();
        documentData.put(FLD_FILE_NAME, scannedDocument.getName());
        documentData.put(FLD_DATE_ADDED, Calendar.getInstance().getTimeInMillis());

        collectionReference.add(documentData).continueWith(Executors.DIRECT_EXECUTOR, taskAddDocument -> {
            scannedDocument.setId(Objects.requireNonNull(taskAddDocument.getResult()).getId());
            return getImagesSnapshot(firebaseUser, scannedDocument);
        })      .continueWith(Executors.DIRECT_EXECUTOR, taskAddDocument        -> getImagesSnapshot(firebaseUser, scannedDocument)
                .continueWith(Executors.DIRECT_EXECUTOR, taskGetImagesSnapshot  -> removeImages(taskGetImagesSnapshot.getResult())
                .continueWith(Executors.DIRECT_EXECUTOR, taskRemoveImages       -> insertImages(firebaseUser, scannedDocument))
                .continueWith(Executors.DIRECT_EXECUTOR, taskInsertImages       -> {
                    Storage.uploadImages(scannedDocument, result -> Storage.uploadPDF(scannedDocument.getId() + ".pdf", PDFBuilder.PDFToBytes(scannedDocument.getScannedImageList())));
                    return null;
                }).continueWith(Task::getResult)
        )).continueWith(nothing -> {progressDialogListener.onDismissProgressDialog(); return null;} );
    }

    private static void updateDocument(@NonNull final FirebaseUser firebaseUser, @NonNull final ScannedDocument scannedDocument, @NonNull final ProgressDialogListener progressDialogListener){
        String uuid = firebaseUser.getUid();

        final CollectionReference collectionReference = instance.mDb.collection(COL_USER).document(uuid).collection(COL_DOCUMENT);
        Map<String, Object> documentData = new HashMap<>();
        documentData.put(FLD_FILE_NAME, scannedDocument.getName());
        documentData.put(FLD_DATE_ADDED, Calendar.getInstance().getTimeInMillis());

        collectionReference.document(scannedDocument.getId()).set(documentData)
                .continueWith(Executors.DIRECT_EXECUTOR, taskAddDocument        -> getImagesSnapshot(firebaseUser, scannedDocument))
                .continueWith(Executors.DIRECT_EXECUTOR, taskAddDocument        -> getImagesSnapshot(firebaseUser, scannedDocument)
                .continueWith(Executors.DIRECT_EXECUTOR, taskGetImagesSnapshot  -> removeImages(taskGetImagesSnapshot.getResult())
                .continueWith(Executors.DIRECT_EXECUTOR, taskRemoveImages       -> insertImages(firebaseUser, scannedDocument))
                .continueWith(Executors.DIRECT_EXECUTOR, taskInsertImages       -> {
                        Storage.uploadImages(scannedDocument, result -> Storage.uploadPDF(scannedDocument.getId() + ".pdf", PDFBuilder.PDFToBytes(scannedDocument.getScannedImageList())));
                        return null;
                    }).continueWith(Task::getResult)
                )).continueWith(nothing -> {progressDialogListener.onDismissProgressDialog(); return null;});

    }


    public static void saveDocument(@NonNull final FirebaseUser firebaseUser, @NonNull final ScannedDocument scannedDocument, @NonNull final ProgressDialogListener progressDialogListener) {
        progressDialogListener.onShowProgressDialog("Saving document");

        if (scannedDocument.getId().isEmpty()) {
            insertNewDocument(firebaseUser, scannedDocument, progressDialogListener);
        } else {
            updateDocument(firebaseUser, scannedDocument, progressDialogListener);
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private static Point[] listOfMapToPoints(@NonNull List<?> maps){
        final int TOTAL_POINTS = maps.size();
        Point[] points = new Point[TOTAL_POINTS];
        for(int i = 0 ; i < TOTAL_POINTS; i++){
            HashMap<String, Double> data = (HashMap<String, Double>) maps.get(i);
            points[i] = new Point(data.get("x"), data.get("y"));
        }
        return points;
    }

    public static void getFullDocument(@NonNull FirebaseUser firebaseUser, @NonNull ScannedDocument scannedDocument, @NonNull BiResultListener<StatusResultListener, ScannedDocument> resultListener, @NonNull ProgressDialogListener progressDialogListener){
        progressDialogListener.onShowProgressDialog("Retrieving documents");
        String uuid = firebaseUser.getUid();

        final CommonResultListener<StatusResultListener> commonResultListener = new CommonResultListener<StatusResultListener>() {
            private final ArrayList<StatusResultListener> resultList = new ArrayList<>();

            @Override
            public void onResultReceived(StatusResultListener result) {
                resultList.add(result);

                if(resultList.size() >= 2){
                    StatusResultListener finalResult = null;

                    for(StatusResultListener s: resultList){
                        finalResult = s;
                        if(!s.isSuccess())
                            break;
                    }

                    progressDialogListener.onDismissProgressDialog();
                    resultListener.onResultReceived(finalResult, scannedDocument);
                }
            }
        };

        // Retrieve email and date added
        final DocumentReference documentReference = instance.mDb.collection(COL_USER).document(uuid).collection(COL_DOCUMENT).document(scannedDocument.getId());
        documentReference.get().continueWith(result ->{
            if(result.isSuccessful()){
                final DocumentSnapshot snapshot = result.getResult();
                scannedDocument.setName(snapshot.getString(FLD_FILE_NAME));
                scannedDocument.setDate(snapshot.getLong(FLD_DATE_ADDED));
            }

            commonResultListener.onResultReceived(new StatusResultListener() {
                    @Override
                    public boolean isSuccess() { return result.isSuccessful(); }

                    @Override
                    public String getErrorMessage() { return result.isSuccessful() ? "" : result.getException().getMessage(); }
                }
            );

            return result.isSuccessful();
        });

        // Retrieve image, contour
        documentReference.collection(COL_IMAGES).get().continueWith(imagesResult ->{
            if(imagesResult.isSuccessful()){
                final QuerySnapshot images              = imagesResult.getResult();
                ScannedImage[] scannedImages            = new ScannedImage[images.getDocuments().size()];
                ArrayList<Boolean> completeFlags        = new ArrayList<>();

                for (DocumentSnapshot image : images) {
                    List<?> contour = (List<?>) image.get(FLD_CONTOUR);
                    Storage.downloadImage(image.getString(FLD_BITMAP), imageByteArr ->{

                        // When receive image, update the flags
                        completeFlags.add(imageByteArr != null);

                        // If image retrieve successfully, add to array list
                        if(imageByteArr != null) {
                            ScannedImage scannedImage = new ScannedImage(BitmapFactory.decodeByteArray(imageByteArr, 0, imageByteArr.length), listOfMapToPoints(contour));
                            scannedImage.setFilter(image.getLong(FLD_FILTER).intValue());
                            scannedImages[image.getLong(FLD_SEQ).intValue() - 1] = scannedImage;
                        }

                        // When all files downloaded, trigger parents
                        if(completeFlags.size() == images.size()){
                            scannedDocument.clearScannedImage();
                            for (ScannedImage scannedImage : scannedImages) {
                                scannedDocument.addScannedImage(scannedImage);
                            }

                            commonResultListener.onResultReceived(new StatusResultListener() {
                                @Override
                                public boolean isSuccess() {
                                    return !completeFlags.contains(false);
                                }

                                @Override
                                public String getErrorMessage() {
                                    return (completeFlags.contains(false)) ? "Fail to retrieve image" : "";
                                }
                            });
                        }
                    });
                }
            } else {
                commonResultListener.onResultReceived(new StatusResultListener() {
                    @Override
                    public boolean isSuccess() {
                        return false;
                    }

                    @Override
                    public String getErrorMessage() {
                        return "Fail to retrieve image";
                    }
                });
            }
            return null;
        });
    }

    public static void getBriefDocument(@NonNull FirebaseUser firebaseUser, @NonNull final CommonResultListener<ArrayList<ScannedDocument>> dbGetDocumentCallback) {
        String uuid = firebaseUser.getUid();

        final CollectionReference collectionReference = instance.mDb.collection(COL_USER).document(uuid).collection(COL_DOCUMENT);
        collectionReference.get().addOnCompleteListener(task -> {
            final ArrayList<ScannedDocument> scannedDocuments = new ArrayList<>();
            final List<DocumentSnapshot> documentSnapshotList = Objects.requireNonNull(task.getResult()).getDocuments();

            if (documentSnapshotList.isEmpty())
                dbGetDocumentCallback.onResultReceived(scannedDocuments);

            for (final DocumentSnapshot s : documentSnapshotList) {
                Storage.getImage(s.getId() + "/cover.bmp", uriTask -> {
                    if (uriTask.isSuccessful()) {
                        ScannedDocument scannedDocument = new ScannedDocument(s.getId(), Objects.requireNonNull(s.get(FLD_FILE_NAME)).toString(), new LinkedList<>(), uriTask.getResult(), (Long) Objects.requireNonNull(s.get(FLD_DATE_ADDED)));
                        scannedDocuments.add(scannedDocument);

                        // Check if all documents received
                        if (scannedDocuments.size() == documentSnapshotList.size())
                            dbGetDocumentCallback.onResultReceived(scannedDocuments);
                    } else {
                        dbGetDocumentCallback.onResultReceived(scannedDocuments);
                    }
                });
            }
        });
    }
}
