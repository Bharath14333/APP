package com.emergency.crisissense.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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

public class ContactsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private Button btnAddContact;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private ContactsAdapter adapter;
    private List<EmergencyContact> contactList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_emergency_contacts, container, false);

        // Hide toolbar because MainActivity already displays the main screen action bar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }

        recyclerView = view.findViewById(R.id.recycler_contacts);
        progressBar = view.findViewById(R.id.contacts_progress);
        txtEmpty = view.findViewById(R.id.txt_empty_contacts);
        btnAddContact = view.findViewById(R.id.btn_add_contact);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(requireContext());
        contactList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter();
        recyclerView.setAdapter(adapter);

        // Helplines
        view.findViewById(R.id.btn_dial_police).setOnClickListener(v -> dialHelpline("112"));
        view.findViewById(R.id.btn_dial_ambulance).setOnClickListener(v -> dialHelpline("108"));
        view.findViewById(R.id.btn_dial_fire).setOnClickListener(v -> dialHelpline("101"));

        btnAddContact.setOnClickListener(v -> showContactDialog(null));

        loadContacts();

        return view;
    }

    private void loadContacts() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (txtEmpty != null) txtEmpty.setVisibility(View.GONE);
        contactList.clear();

        String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "guest_user";
        
        firebaseHelper.getUserContacts(userId, task -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            loadContactsFromCache();
        });
    }

    private void loadContactsFromCache() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        contactList.clear();
        
        if (getActivity() == null) return;

        Set<String> cachedContactStrings = getActivity().getSharedPreferences("cs_app_contacts", Context.MODE_PRIVATE)
                .getStringSet("contacts_data", new HashSet<>());

        for (String s : cachedContactStrings) {
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
            if (txtEmpty != null) txtEmpty.setVisibility(View.VISIBLE);
        } else {
            if (txtEmpty != null) txtEmpty.setVisibility(View.GONE);
        }
    }

    private void showContactDialog(EmergencyContact contactToEdit) {
        if (getActivity() == null) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(contactToEdit == null ? "Add Emergency Contact" : "Edit Emergency Contact");

        android.widget.LinearLayout container = new android.widget.LinearLayout(getActivity());
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 24);

        final EditText editName = new EditText(getActivity());
        editName.setHint("Contact Name");
        editName.setText(contactToEdit != null ? contactToEdit.getName() : "");
        container.addView(editName);

        final EditText editPhone = new EditText(getActivity());
        editPhone.setHint("Phone Number");
        editPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        editPhone.setText(contactToEdit != null ? contactToEdit.getNumber() : "");
        container.addView(editPhone);

        final Spinner spinnerRelationship = new Spinner(getActivity());
        String[] relationships = {"Spouse", "Parent", "Sibling", "Child", "Friend", "Doctor", "Other"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, relationships);
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

        builder.setView(container);
        builder.setPositiveButton("Save", (d, which) -> {
            String name = editName.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String rel = spinnerRelationship.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(getActivity(), "Name and Phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            saveContact(contactToEdit, name, phone, rel);
        });

        builder.setNegativeButton("Cancel", (d, which) -> d.dismiss());
        builder.show();
    }

    private void saveContact(EmergencyContact original, String name, String phone, String relationship) {
        if (getActivity() == null) return;
        
        String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "guest_user";
        String contactId = original != null ? original.getContactId() : UUID.randomUUID().toString();

        EmergencyContact newContact = new EmergencyContact(contactId, name, phone, "General", relationship);

        firebaseHelper.addUserContact(userId, newContact, task -> {});

        // Save locally to cache to support Offline Demo Mode
        Set<String> cachedContactStrings = getActivity().getSharedPreferences("cs_app_contacts", Context.MODE_PRIVATE)
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

        // Add new entry
        String contactString = contactId + "|" + name + "|" + phone + "|" + relationship + "|General";
        cachedContactStrings.add(contactString);

        getActivity().getSharedPreferences("cs_app_contacts", Context.MODE_PRIVATE)
                .edit()
                .putStringSet("contacts_data", cachedContactStrings)
                .apply();

        firebaseHelper.addSystemLog(original == null ? "CONTACT_ADDED" : "CONTACT_UPDATED", 
                (original == null ? "Added contact: " : "Updated contact: ") + name, 
                sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");

        loadContacts();
    }

    private void deleteContact(EmergencyContact contact) {
        if (getActivity() == null) return;
        
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete " + contact.getName() + "?")
                .setPositiveButton("Delete", (d, which) -> {
                    String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "guest_user";
                    firebaseHelper.deleteUserContact(userId, contact.getContactId(), task -> {});

                    // Delete from cache
                    Set<String> cachedContactStrings = getActivity().getSharedPreferences("cs_app_contacts", Context.MODE_PRIVATE)
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
                    getActivity().getSharedPreferences("cs_app_contacts", Context.MODE_PRIVATE)
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
            View view = LayoutInflater.from(getContext())
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
