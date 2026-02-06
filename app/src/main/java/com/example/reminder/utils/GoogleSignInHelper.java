package com.example.reminder.utils;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.example.reminder.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import javax.inject.Inject;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class GoogleSignInHelper {

    private final GoogleSignInClient googleSignInClient;

    @Inject
    public GoogleSignInHelper(@ApplicationContext Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA)) // Use AppData folder for safety
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public GoogleSignInAccount getLastSignedInAccount(Context context) {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

    public void signOut(@NonNull com.google.android.gms.tasks.OnCompleteListener<Void> listener) {
        googleSignInClient.signOut().addOnCompleteListener(listener);
    }

    public boolean isSignedIn(Context context) {
        return GoogleSignIn.getLastSignedInAccount(context) != null;
    }
}
