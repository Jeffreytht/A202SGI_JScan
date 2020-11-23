package com.example.jScanner.utility;

import androidx.annotation.NonNull;

import com.example.jScanner.Callback.CommonResultListener;
import com.example.jScanner.Callback.ProgressDialogListener;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

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

    public static void insertNewUser(FirebaseUser firebaseUser) {
        String uuid = firebaseUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put(FLD_EMAIL, firebaseUser.getEmail());
        instance.mDb.collection(COL_USER).document(uuid).set(user, SetOptions.merge());
    }

    private static Task<Void> insertImages(final FirebaseUser user, final ScannedDocument scannedDocument) {
        final CollectionReference ref = instance.mDb.collection(COL_USER).document(user.getUid())
                .collection(COL_DOCUMENT).document(scannedDocument.getId())
                .collection(COL_IMAGES);

        final WriteBatch writeBatch = instance.mDb.batch();
        final List<ScannedImage> scannedImageLinkedList = scannedDocument.getScannedImageList();

        Map<String, Object> imageData = new HashMap<>();

        int seq = 1;
        // Upload cover for document list in dashboard
        Storage.uploadImage(scannedDocument.getId() + "/cover.bmp", scannedImageLinkedList.get(0).getFinalImage());

        for (ScannedImage si : scannedImageLinkedList) {
            String path = scannedDocument.getId() + "/" + seq + ".bmp";
            imageData.put(FLD_SEQ, seq);
            imageData.put(FLD_BITMAP, path);
            imageData.put(FLD_CONTOUR, Arrays.asList(si.getContour()));
            imageData.put(FLD_FILTER, si.getFilter());
            writeBatch.set(ref.document(), imageData);
            seq++;
        }

        return writeBatch.commit();
    }

    public static void insertNewDocument(final FirebaseUser firebaseUser, final ScannedDocument scannedDocument, final ProgressDialogListener progressDialogListener) {
        progressDialogListener.onShowProgressDialog("Creating document");
        String uuid = firebaseUser.getUid();

        final CollectionReference collectionReference = instance.mDb.collection(COL_USER).document(uuid).collection(COL_DOCUMENT);

        Map<String, Object> documentData = new HashMap<>();
        documentData.put(FLD_FILE_NAME, scannedDocument.getName());
        documentData.put(FLD_DATE_ADDED, Calendar.getInstance().getTimeInMillis());

        // Check if this document is new
        Continuation<Task<Void>, Object> uploadImages = insertImagesTask -> {
            if (insertImagesTask.isSuccessful()) {
                Storage.uploadImages(scannedDocument);
                Storage.uploadPDF(scannedDocument.getId() + "/" + scannedDocument.getName() + ".pdf", PDFBuilder.PDFToBytes(scannedDocument.getScannedImageList()));
            }
            progressDialogListener.onDismissProgressDialog();
            return null;
        };

        if (scannedDocument.getId() == null || scannedDocument.getId().isEmpty()) {
            collectionReference.add(documentData).continueWith(taskAddDocument -> {
                if (!taskAddDocument.isSuccessful()) {
                    progressDialogListener.onDismissProgressDialog();
                    return null;
                } else {
                    scannedDocument.setId(Objects.requireNonNull(taskAddDocument.getResult()).getId());
                    return insertImages(firebaseUser, scannedDocument);
                }
            }).continueWith(uploadImages);

        } else {
            collectionReference.document(scannedDocument.getId()).set(documentData).continueWith(taskSetDocument -> {
                if (!taskSetDocument.isSuccessful()) {
                    progressDialogListener.onDismissProgressDialog();
                    return null;
                } else {
                    return insertImages(firebaseUser, scannedDocument);
                }
            }).continueWith(uploadImages);
        }
    }

    public static void getDocument(@NonNull FirebaseUser firebaseUser, @NonNull final CommonResultListener<ArrayList<ScannedDocument>> dbGetDocumentCallback) {
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
