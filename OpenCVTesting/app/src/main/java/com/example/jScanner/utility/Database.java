package com.example.jScanner.utility;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.jScanner.Callback.DbGetDocumentCallback;
import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
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

    private static void insertImages(final FirebaseUser user, final String id, final ScannedDocument scannedDocument) {
        final CollectionReference ref = instance.mDb.collection(COL_USER).document(user.getUid())
                .collection(COL_DOCUMENT).document(scannedDocument.getId())
                .collection(COL_IMAGES);

        final LinkedList<ScannedImage> scannedImageLinkedList = scannedDocument.getScannedImageList();

        ref.get().continueWith(new Continuation<QuerySnapshot, Object>() {
            @Override
            public Object then(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> snapshots = Objects.requireNonNull(task.getResult()).getDocuments();
                for (DocumentSnapshot s : snapshots)
                    s.getReference().delete();
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(@NonNull Task<Object> task) {
                Map<String, Object> imageData = new HashMap<>();

                int seq = 1;
                // Upload cover for document list in dashboard
                Storage.uploadImage(id + "/cover.bmp", scannedImageLinkedList.getFirst().getFinalImage());

                for (ScannedImage si : scannedImageLinkedList) {
                    String path = id + "/" + seq + ".bmp";
                    Storage.uploadImage(path, si.getOriImage());
                    imageData.put(FLD_SEQ, seq);
                    imageData.put(FLD_BITMAP, path);
                    imageData.put(FLD_CONTOUR, Arrays.asList(si.getContour()));
                    imageData.put(FLD_FILTER, si.getFilter());
                    ref.add(imageData);
                    seq++;
                }
                return null;
            }
        });
    }

    public static void insertNewDocument(final FirebaseUser firebaseUser, final ScannedDocument scannedDocument) {
        String uuid = firebaseUser.getUid();

        // Init file name, if file name is empty, default is current date
        final StringBuilder fileName = new StringBuilder(scannedDocument.getName());
        if (fileName.length() == 0)
            fileName.append(DateFormat.getDateInstance().format(Calendar.getInstance().getTime()));

        final CollectionReference collectionReference = instance.mDb.collection(COL_USER).document(uuid).collection(COL_DOCUMENT);

        // Check if this document is new
        if (scannedDocument.getId() == null || scannedDocument.getId().isEmpty()) {
            Map<String, Object> documentData = new HashMap<>();
            documentData.put(FLD_FILE_NAME, fileName.toString());

            collectionReference.add(documentData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference ref) {
                            scannedDocument.setId(ref.getId());
                            insertImages(firebaseUser, scannedDocument.getId(), scannedDocument);
                            Storage.uploadPDF(scannedDocument.getId() + "/" + fileName + ".pdf", PDFBuilder.PDFToBytes(scannedDocument.getScannedImageList()));
                        }
                    });
        } else {
            insertImages(firebaseUser, scannedDocument.getId(), scannedDocument);
            Storage.uploadPDF(scannedDocument.getId() + "/" + fileName + ".pdf", PDFBuilder.PDFToBytes(scannedDocument.getScannedImageList()));
        }
    }

    public static void getDocument(@NonNull FirebaseUser firebaseUser, @NonNull DbGetDocumentCallback dbGetDocumentCallback) {
        final DbGetDocumentCallback callback = dbGetDocumentCallback;
        String uuid = firebaseUser.getUid();

        final CollectionReference collectionReference = instance.mDb.collection(COL_USER).document(uuid).collection(COL_DOCUMENT);
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<ScannedDocument> scannedDocuments = new ArrayList<>();
                List<DocumentSnapshot> documentSnapshotList = Objects.requireNonNull(task.getResult()).getDocuments();
                for (DocumentSnapshot s : documentSnapshotList) {
                    scannedDocuments.add(new ScannedDocument(s.getId(), Objects.requireNonNull(s.get(FLD_FILE_NAME)).toString()));
                    Storage.getImage(s.getId() + "/cover.bmp");
                }

                //callback.onDocumentReceived(scannedDocuments);
            }
        });
    }


}
