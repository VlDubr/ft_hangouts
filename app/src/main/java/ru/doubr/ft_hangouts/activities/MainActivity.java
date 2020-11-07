package ru.doubr.ft_hangouts.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import android.view.ContextMenu;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import ru.doubr.ft_hangouts.R;
import ru.doubr.ft_hangouts.data.Contact;
import ru.doubr.ft_hangouts.data.Message;
import ru.doubr.ft_hangouts.data.contactsDB.ContactDAO;
import ru.doubr.ft_hangouts.data.contactsDB.ContactsDatabase;
import ru.doubr.ft_hangouts.data.messagesDB.DateTypeConverter;
import ru.doubr.ft_hangouts.data.messagesDB.MessagesDAO;
import ru.doubr.ft_hangouts.data.messagesDB.MessagesDatabase;

public class MainActivity extends AppCompatActivity {

    public static ArrayList<Contact> contactsArrayList = new ArrayList<>();
    private LinearLayout contactsLL;
    public static int COLOR = Color.BLACK;

    public static ContactDAO contactDAO;
    public static MessagesDAO messagesDAO;

    private static final int PERMISSIONS = 1;
    public static final int CALL_CODE = 101;
    public static final int MESSAGE_CODE = 102;
    public static int chosenContactId = 0;

    private IntentFilter intentFilter;

    public static boolean onPause = false;
    public static String pauseDate = null;


    private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String number = extras.getString("number");
            Contact c = MainActivity.getContactByNumber(number);
            if (c == null) {
                c = new Contact(number, "", number);
                MainActivity.contactsArrayList.add(c);
                MainActivity.contactDAO.insert(c);
            }

            messagesDAO.insert(new Message(c.getId(), extras.getString("message"), new Date(), 1));
            onResume();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.myContactsTitle);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(COLOR));
        contactsLL = findViewById(R.id.contactsLayout);

        ContactsDatabase contactsDatabase = Room.databaseBuilder(getApplicationContext(), ContactsDatabase.class, "contacts-db").allowMainThreadQueries()
                .build();
        contactDAO = contactsDatabase.contactDao();

        MessagesDatabase messagesDatabase = Room.databaseBuilder(getApplicationContext(), MessagesDatabase.class, "dialogs-db").allowMainThreadQueries()
                .build();
        messagesDAO = messagesDatabase.messagesDAO();
        contactsArrayList = (ArrayList<Contact>) contactDAO.getAllContacts();
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS);

        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVED_ACTION");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingChangeCyan:
                COLOR = Color.CYAN;
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.CYAN));
                return true;
            case R.id.settingChangeGreen:
                COLOR = Color.GREEN;
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.GREEN));
                return true;
            case R.id.settingChangeGray:
                COLOR = Color.LTGRAY;
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
                return true;
            case R.id.settingChangeYellow:
                COLOR = Color.YELLOW;
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        CreateContactActivity.onPause = false;
        DisplayContactActivity.onPause = false;
        SendMessagesActivity.onPause = false;

        contactsLL.removeAllViews();
        registerReceiver(intentReceiver, intentFilter);
        updateContactsList();

        if (onPause) {
            Toast.makeText(getApplicationContext(), pauseDate, Toast.LENGTH_LONG).show();
            onPause = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        onPause = true;
        pauseDate = DateTypeConverter.fromDate(new Date());
        unregisterReceiver(intentReceiver);
    }

    public void updateContactsList() {
        for (Contact c : contactsArrayList) {
            LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout contactLinLayout = getContactLinearLayout(c);

            final float scale = getResources().getDisplayMetrics().density;
            int pixels = (int) (42 * scale + 0.5f); //42dp
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(pixels, pixels);
            imageParams.setMargins(50, 20, 0, 0);
            ImageView contactImage = new ImageView(this);////
            try {
                Uri image = Uri.parse(c.getImageSrc());
                final InputStream imageStream = getContentResolver().openInputStream(image);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                contactImage.setImageBitmap(selectedImage);
            } catch (FileNotFoundException | NullPointerException e) {
                contactImage.setImageResource(R.drawable.cat);
            }
            contactLinLayout.addView(contactImage, imageParams);

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            nameParams.setMargins(50, 20, 50, 0);
            TextView contactName = new TextView(this);//////
            String initials = c.getFirstName() + " " + c.getLastName();
            contactName.setText(initials.toCharArray(), 0, initials.length());
            contactName.setTextSize(24);
            contactLinLayout.addView(contactName, nameParams);

            contactsLL.addView(contactLinLayout, linLayoutParam);
        }

    }

    private LinearLayout getContactLinearLayout(Contact c) {
        LinearLayout contactLinLayout = new LinearLayout(this);
        contactLinLayout.setId(c.getId());
        contactLinLayout.setOrientation(LinearLayout.HORIZONTAL);
        contactLinLayout.setWeightSum(1);


        contactLinLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DisplayContactActivity.class);

                intent.putExtra("contactID", view.getId());
                startActivity(intent);
            }
        });

        contactLinLayout.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                MainActivity.super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
                contextMenu.add(Menu.NONE, CALL_CODE, Menu.NONE, R.string.call);
                contextMenu.add(Menu.NONE, MESSAGE_CODE, Menu.NONE, R.string.message);

                chosenContactId = view.getId();
            }
        });

        return contactLinLayout;
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        View view = findViewById(chosenContactId);
        Intent intent;
        switch (item.getItemId()) {
            case CALL_CODE:
                Contact c = getContactById(view.getId());
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + c.getPhone())));
                }
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS);
                break;
            case MESSAGE_CODE:
                intent = new Intent(view.getContext(), SendMessagesActivity.class);
                intent.putExtra("contactID", view.getId());
                startActivity(intent);
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }


    public void addNewContact(View view) {
        Intent intent = new Intent(this, CreateContactActivity.class);
        startActivity(intent);
    }

    public static Contact getContactById(Integer id) {
        for (Contact c: contactsArrayList) {
            if (c.getId().equals(id))
                return c;
        }
        return null;
    }

    public static Contact getContactByNumber(String number) {
        for (Contact c: contactsArrayList) {
            if (c.getPhone().equals(number))
                return c;
        }
        return null;
    }
}