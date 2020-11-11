package com.example.jScanner.utility;

import androidx.annotation.NonNull;

import com.example.jScanner.Model.ScannedDocument;
import com.example.jScanner.Model.ScannedImage;
import com.google.android.gms.tasks.Continuation;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Database {
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private final static Database instance = new Database();

    public static void insertNewUser(FirebaseUser firebaseUser) {
        String uuid = firebaseUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("email", firebaseUser.getEmail());
        instance.mDb.collection("users").document(uuid).set(user, SetOptions.merge());
    }

    private static void insertImages(final CollectionReference ref, final String id, final LinkedList<ScannedImage> scannedImageLinkedList) {
        final HashSet<String> scannedImageIdSet = new HashSet<>();
        for (ScannedImage s : scannedImageLinkedList)
            scannedImageIdSet.add(String.valueOf(s.getId()));

        ref.get().continueWith(new Continuation<QuerySnapshot, Object>() {
            @Override
            public Object then(@NonNull Task<QuerySnapshot> task)  {
                List<DocumentSnapshot> snapshots = Objects.requireNonNull(task.getResult()).getDocuments();
                for (DocumentSnapshot s : snapshots)
                    if (!scannedImageIdSet.contains(s.getId()))
                        s.getReference().delete();
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(@NonNull Task<Object> task)  {
                Map<String, Object> imageData = new HashMap<>();

                for (ScannedImage si : scannedImageLinkedList) {
                    String path = id + "/" + si.getId() + ".bmp";
                    Storage.uploadImage(path, si.getOriImage());
                    imageData.put("bitmap", path);
                    imageData.put("contour", Arrays.asList(si.getContour()));
                    imageData.put("filter", si.getFilter());
                    ref.document(String.valueOf(si.getId())).set(imageData);
                }
                return null;
            }
        });
    }

    public static void insertNewDocument(FirebaseUser firebaseUser, final ScannedDocument scannedDocument) {
        String uuid = firebaseUser.getUid();

        // Init file name
        String tempName = scannedDocument.getName();
        if (tempName == null || tempName.isEmpty())
            tempName = DateFormat.getDateInstance().format(Calendar.getInstance().getTime());

        final String fileName = tempName;
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("name", fileName);

        final CollectionReference collectionReference = instance.mDb.collection("users").document(uuid).collection("document");
        if (scannedDocument.getId() == null || scannedDocument.getId().isEmpty()) {
            collectionReference.add(documentData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference ref) {
                            scannedDocument.setId(ref.getId());
                            insertImages(collectionReference.document(scannedDocument.getId()).collection("images"), scannedDocument.getId(), scannedDocument.getScannedImageList());
                            Storage.uploadPDF(scannedDocument.getId() + "/" +  fileName + ".pdf", PDFBuilder.PDFToBytes(scannedDocument.getScannedImageList()));
                        }
                    });
        } else {
            insertImages(collectionReference.document(scannedDocument.getId()).collection("images"), scannedDocument.getId(), scannedDocument.getScannedImageList());
            Storage.uploadPDF(scannedDocument.getId() + "/" +  fileName + ".pdf", PDFBuilder.PDFToBytes(scannedDocument.getScannedImageList()));
        }
    }

//    public static void getDocument(FirebaseUser firebaseUser, final DatabaseCallback databaseCallback) {
//        String uuid = firebaseUser.getUid();
//
//        DocumentReference docRef = instance.mDb.collection("users").document(uuid);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot snapshot = task.getResult();
//                    Map<String, Object> data = Objects.requireNonNull(snapshot).getData();
//                    databaseCallback.onDataReceived(data);
//                }
//            }
//        });
//    }

}
