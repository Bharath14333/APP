package com.emergency.crisissense.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emergency.crisissense.R;
import com.emergency.crisissense.model.EmergencyContact;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EmergencyContactsActivity extends AppCompatActivity {
    private static final int CALL_PERMISSION_REQUEST = 102;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private Button btnAddContact;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private ContactsAdapter adapter;
    private List<EmergencyContact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_contacts);
        progressBar = findViewById(R.id.contacts_progress);
        txtEmpty = findViewById(R.id.txt_empty_contacts);
        btnAddContact = findViewById(R.id.btn_add_contact);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);
        contactList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter();
        recyclerView.setAdapter(adapter);

        // Helpline dialers
        findViewById(R.id.btn_dial_police).setOnClickListener(v -> dialHelpline("112"));
        findViewById(R.id.btn_dial_ambulance).setOnClickListener(v -> dialHelpline("108"));
        findViewById(R.id.btn_dial_fire).setOnClickListener(v -> dialHelpline("101"));

        btnAddContact.setOnClickListener(v -> showContactDialog(null));

        loadContacts();
    }

    private void loadContacts() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        contactList.clear();

        String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "guest_user";
        
        firebaseHelper.getUserContacts(userId, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                // In demo offline fallback mode, parse from cache
                loadContactsFromCache();
            } else {
                loadContactsFromCache();
            }
        });
    }

    private void loadContactsFromCache() {
        progressBar.setVisibility(View.GONE);
        contactList.clear();
        
        // Fetch from sharedPreferences to support Offline Demo Mode
        Set<String> cachedContactStrings = getSharedPreferences("cs_app_contacts", MODE_PRIVATE)
                .getStringSet("contacts_data", new HashSet<>());

        for (String s : cachedContactStrings) {
            // String layout: id|name|number|relationship|type
            String[] parts = s.split("\\|");
            if (parts.length >= 4) {
                String id = parts[0];
                String name = parts[1];
                String num = parts[2];
                String rel = parts[3];
                String type = parts.length > 4 ? parts[4] : "General";
                contactList.add(new EmergencyContact(id, name, num, type, rel));
            }
        }

        adapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    private void toggleEmptyState() {
        if (contactList.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void showContactDialog(EmergencyContact contactToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_register, null); 
        // We will construct a clean simple dialog programmatically to avoid inflating activity_register
        
        View customView = getLayoutInflater().inflate(R.layout.bottom_sheet_service_details, null); 
        // Let's create an dialog programmatically with EditTexts for cleanliness
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        
        AlertDialog dialog = builder.create();
        dialog.setTitle(contactToEdit == null ? "Add Emergency Contact" : "Edit Emergency Contact");

        // Custom Layout programmatic builder
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 24);

        final EditText editName = new EditText(this);
        editName.setHint("Contact Name");
        editName.setText(contactToEdit != null ? contactToEdit.getName() : "");
        container.addView(editName);

        final EditText editPhone = new EditText(this);
        editPhone.setHint("Phone Number");
        editPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        editPhone.setText(contactToEdit != null ? contactToEdit.getNumber() : "");
        container.addView(editPhone);

        final Spinner spinnerRelationship = new Spinner(this);
        String[] relationships = {"Spouse", "Parent", "Sibling", "Child", "Friend", "Doctor", "Other"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, relationships);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(spinnerAdapter);
        
        if (contactToEdit != null) {
            for (int i = 0; i < relationships.length; i++) {
                if (relationships[i].equalsIgnoreCase(contactToEdit.getRelationship())) {
                    spinnerRelationship.setSelection(i);
                    break;
                }
            }
        }
        container.addView(spinnerRelationship);

        dialog.setView(container);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", (d, which) -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String rel = spinnerRelationship.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            saveContact(contactToEdit, name, phone, rel);
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (d, which) -> d.dismiss());
        dialog.show();
    }

    private void saveContact(EmergencyContact original, String name, String phone, String relationship) {
        String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "guest_user";
        String contactId = original != null ? original.getContactId() : UUID.randomUUID().toString();

        EmergencyContact newContact = new EmergencyContact(contactId, name, phone, "General", relationship);

        firebaseHelper.addUserContact(userId, newContact, task -> {});

        // Save locally to cache to support Offline Demo Mode
        Set<String> cachedContactStrings = getSharedPreferences("cs_app_contacts", MODE_PRIVATE)
                .getStringSet("contacts_data", new HashSet<>());
        
        // Remove old entry if editing
        if (original != null) {
            String oldKey = null;
            for (String s : cachedContactStrings) {
                if (s.startsWith(original.getContactId() + "|")) {
                    oldKey = s;
                    break;
                }
            }
            if (oldKey != null) {
                cachedContactStrings.remove(oldKey);
            }
        }

        // Add new entry: id|name|number|relationship|type
        String contactString = contactId + "|" + name + "|" + phone + "|" + relationship + "|General";
        cachedContactStrings.add(contactString);

        getSharedPreferences("cs_app_contacts", MODE_PRIVATE)
                .edit()
                .putStringSet("contacts_data", cachedContactStrings)
                .apply();

        firebaseHelper.addSystemLog(original == null ? "CONTACT_ADDED" : "CONTACT_UPDATED", 
                (original == null ? "Added contact: " : "Updated contact: ") + name, 
                sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");

        loadContacts();
    }

    private void deleteContact(EmergencyContact contact) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete " + contact.getName() + "?")
                .setPositiveButton("Delete", (d, which) -> {
                    String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "guest_user";
                    firebaseHelper.deleteUserContact(userId, contact.getContactId(), task -> {});

                    // Delete from cache
                    Set<String> cachedContactStrings = getSharedPreferences("cs_app_contacts", MODE_PRIVATE)
                            .getStringSet("contacts_data", new HashSet<>());
                    String deleteKey = null;
                    for (String s : cachedContactStrings) {
                        if (s.startsWith(contact.getContactId() + "|")) {
                            deleteKey = s;
                            break;
                        }
                    }
                    if (deleteKey != null) {
                        cachedContactStrings.remove(deleteKey);
                    }
                    getSharedPreferences("cs_app_contacts", MODE_PRIVATE)
                            .edit()
                            .putStringSet("contacts_data", cachedContactStrings)
                            .apply();

                    firebaseHelper.addSystemLog("CONTACT_DELETED", "Deleted contact: " + contact.getName(), 
                            sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");

                    loadContacts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dialHelpline(String num) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + num));
        startActivity(intent);
    }

    // RecyclerView adapter class for Emergency Contacts
    private class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(EmergencyContactsActivity.this)
                    .inflate(R.layout.item_emergency_contact, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EmergencyContact contact = contactList.get(position);
            holder.txtName.setText(contact.getName());
            holder.txtRelationship.setText("(" + contact.getRelationship() + ")");
            holder.txtPhone.setText("Ph: " + contact.getNumber());
            
            char initial = !TextUtils.isEmpty(contact.getName()) ? contact.getName().toUpperCase().charAt(0) : 'C';
            holder.txtInitial.setText(String.valueOf(initial));

            holder.btnCall.setOnClickListener(v -> dialHelpline(contact.getNumber()));
            holder.btnEdit.setOnClickListener(v -> showContactDialog(contact));
            holder.btnDelete.setOnClickListener(v -> deleteContact(contact));
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtName, txtRelationship, txtPhone, txtInitial;
            ImageButton btnEdit, btnDelete, btnCall;

            ViewHolder(View view) {
                super(view);
                txtName = view.findViewById(R.id.txt_contact_name);
                txtRelationship = view.findViewById(R.id.txt_contact_relationship);
                txtPhone = view.findViewById(R.id.txt_contact_phone);
                txtInitial = view.findViewById(R.id.txt_contact_initial);
                btnEdit = view.findViewById(R.id.btn_edit);
                btnDelete = view.findViewById(R.id.btn_delete);
                btnCall = view.findViewById(R.id.btn_call);
            }
        }
    }
}
