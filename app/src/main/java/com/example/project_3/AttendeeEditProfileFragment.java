package com.example.project_3;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
/**
 * A fragment for editing the attendee's profile, displaying name, contact info, and social links.
 */

public class AttendeeEditProfileFragment extends Fragment {
    private FirebaseFirestore db;
    private TextView nameTextView;
    private TextView contactInfoTextView;
    private TextView socialLinkTextView;

    private User user;

    /**
     * Inflates the layout for the fragment and initializes UI elements.
     * Retrieves and displays the user's profile information from Firestore.
     * @param inflater The layout inflater.
     * @param container The parent view group.
     * @param savedInstanceState The saved instance state bundle.
     * @return The inflated view for the fragment.
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_settings, container, false);

        db = FirebaseFirestore.getInstance();

        nameTextView = view.findViewById(R.id.profile_name_editText);
        socialLinkTextView= view.findViewById(R.id.homepage_editText);
        contactInfoTextView = view.findViewById(R.id.contact_info_editText);

        db.collection("Profiles").document("Test's Profile").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    final int firestore = Log.e("Firestore", error.toString());
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String contactInfo = documentSnapshot.getString("contact_info");
                    String socialLink = documentSnapshot.getString("social_link");


                    //user = new User(name, contactInfo, socialLink);
                    String profileType = documentSnapshot.getString("profile_type");
                    user = new User(new Profile(name, contactInfo, socialLink, profileType));


                    nameTextView.setText(name);
                    contactInfoTextView.setText(contactInfo);
                    socialLinkTextView.setText(socialLink);
                }
            }
        });

        return view;
    }
}
