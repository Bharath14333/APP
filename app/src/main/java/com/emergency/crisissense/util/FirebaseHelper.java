package com.emergency.crisissense.util;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.emergency.crisissense.model.User;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.model.Notification;
import com.emergency.crisissense.model.LogEntry;
import com.emergency.crisissense.model.EmergencyContact;
import com.emergency.crisissense.model.EmergencyService;
import com.emergency.crisissense.model.FavoriteService;

import android.net.Uri;
import java.util.UUID;

public class FirebaseHelper {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    public FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    // AUTH ACTIONS
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public void register(final User user, String password, final OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(user.getEmail(), password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String uid = task.getResult().getUser().getUid();
                    user.setUserId(uid);
                    saveUserProfile(user, task1 -> listener.onComplete(task));
                } else {
                    listener.onComplete(task);
                }
            });
    }

    public void saveUserProfile(User user, OnCompleteListener<Void> listener) {
        db.collection("users").document(user.getUserId()).set(user).addOnCompleteListener(listener);
    }

    public void getUserProfile(String userId, OnSuccessListener<DocumentSnapshot> successListener, OnFailureListener failureListener) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }

    // INCIDENTS ACTIONS
    public void reportIncident(Incident incident, OnCompleteListener<Void> listener) {
        db.collection("incidents").document(incident.getIncidentId()).set(incident).addOnCompleteListener(listener);
    }

    public void getIncidents(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("incidents")
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }

    public void getIncidentsByUserId(String userId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("incidents")
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(listener);
    }

    public void updateIncidentStatus(String incidentId, String status, OnCompleteListener<Void> listener) {
        db.collection("incidents").document(incidentId).update("status", status).addOnCompleteListener(listener);
    }

    public void deleteIncident(String incidentId, OnCompleteListener<Void> listener) {
        db.collection("incidents").document(incidentId).delete().addOnCompleteListener(listener);
    }

    // NOTIFICATIONS
    public void sendNotification(Notification notification, OnCompleteListener<Void> listener) {
        db.collection("notifications").document(notification.getNotificationId()).set(notification).addOnCompleteListener(listener);
    }

    public void getNotifications(String userId, OnCompleteListener<QuerySnapshot> listener) {
        // Query both user-specific and broadcast ("all") notifications
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }

    // EMERGENCY CONTACTS
    public void getEmergencyContacts(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("emergency_contacts").get().addOnCompleteListener(listener);
    }

    public void addEmergencyContact(EmergencyContact contact, OnCompleteListener<Void> listener) {
        db.collection("emergency_contacts").document(contact.getContactId()).set(contact).addOnCompleteListener(listener);
    }

    // IMAGE STORAGE UPLOAD
    public void uploadEvidenceImage(Uri imageUri, OnSuccessListener<UploadTask.TaskSnapshot> successListener, OnFailureListener failureListener) {
        String filename = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("evidence/" + filename);
        ref.putFile(imageUri).addOnSuccessListener(successListener).addOnFailureListener(failureListener);
    }

    // LOGGING ACTIONS
    public void addSystemLog(String action, String details, String performedBy) {
        String logId = db.collection("system_logs").document().getId();
        LogEntry log = new LogEntry(logId, action, details, performedBy);
        db.collection("system_logs").document(logId).set(log);
    }

    public void getSystemLogs(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("system_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }

    // EMERGENCY SERVICES
    public void getEmergencyServices(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("emergency_services")
            .whereEqualTo("active", true)
            .get()
            .addOnCompleteListener(listener);
    }

    // FAVORITES (users/{userId}/favorite_services/{serviceId})
    public void getFavorites(String userId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users").document(userId).collection("favorite_services")
            .get()
            .addOnCompleteListener(listener);
    }

    public void addFavorite(String userId, FavoriteService favorite, OnCompleteListener<Void> listener) {
        db.collection("users").document(userId).collection("favorite_services")
            .document(favorite.getServiceId())
            .set(favorite)
            .addOnCompleteListener(listener);
    }

    public void deleteFavorite(String userId, String serviceId, OnCompleteListener<Void> listener) {
        db.collection("users").document(userId).collection("favorite_services")
            .document(serviceId)
            .delete()
            .addOnCompleteListener(listener);
    }

    // USER CONTACTS (users/{userId}/emergency_contacts/{contactId})
    public void getUserContacts(String userId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users").document(userId).collection("emergency_contacts")
            .get()
            .addOnCompleteListener(listener);
    }

    public void addUserContact(String userId, EmergencyContact contact, OnCompleteListener<Void> listener) {
        db.collection("users").document(userId).collection("emergency_contacts")
            .document(contact.getContactId())
            .set(contact)
            .addOnCompleteListener(listener);
    }

    public void updateUserContact(String userId, EmergencyContact contact, OnCompleteListener<Void> listener) {
        db.collection("users").document(userId).collection("emergency_contacts")
            .document(contact.getContactId())
            .set(contact)
            .addOnCompleteListener(listener);
    }

    public void deleteUserContact(String userId, String contactId, OnCompleteListener<Void> listener) {
        db.collection("users").document(userId).collection("emergency_contacts")
            .document(contactId)
            .delete()
            .addOnCompleteListener(listener);
    }
}
