package com.sabrina.sintonia;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sabrina.sintonia.activities.MainActivity;

public class Redirect {
    public static void verifyAuthentication(AppCompatActivity actOrigem, Class<?> actDestino) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Redirect.changeScreen(actOrigem, MainActivity.class);
            actOrigem.finish();
        }
    }

    public static void changeScreen(Context actOrigem, Class<?> actDestino) {
        Intent intent = new Intent(actOrigem, actDestino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        actOrigem.startActivity(intent);
    }

}

