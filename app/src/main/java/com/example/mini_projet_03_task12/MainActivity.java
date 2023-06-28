package com.example.mini_projet_03_task12;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.Manifest;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Spinner spinner_contacts;
    SharedPreferences sharedPreferences;
    private static final int CONTACTS_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner_contacts = findViewById(R.id.spinner_contacts);
        sharedPreferences = getSharedPreferences("mySP", MODE_PRIVATE);

        boolean isPermissionNotAllowed = sharedPreferences.getBoolean("isPermissionNotAllowed", false);

        if (isPermissionNotAllowed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Request for permissions")
                    .setMessage("The app needs permissions !!!\nDo you want to grant them ?")
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_PERMISSION_REQUEST);

                        //region disable The dialog that appears every time the user avoid allowing the permissions
                        editor.putBoolean("isPermissionNotAllowed", false).apply();
                        //endregion
                    }))
                    .setNegativeButton("No", ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .show();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_PERMISSION_REQUEST);
            } else {
                getContacts();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (requestCode == CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //region disable The dialog that appears every time the user avoid allowing the permissions
                editor.putBoolean("isPermissionNotAllowed", false).apply();
                //endregion

                getContacts();

                Toast.makeText(this, "Contacts permission granted", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Request for permission")
                        .setMessage("The app wont work properly because you did not grant permissions")
                        .show();

                // Save the permission denied status for future visits
                editor.putBoolean("isPermissionNotAllowed", true).apply();
            }
        }
    }

    private void getContacts() {
        List<String> contactsList = new ArrayList<>();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                contactsList.add(name);
            }

            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                contactsList
        );
        spinner_contacts.setAdapter(adapter);
    }
}
