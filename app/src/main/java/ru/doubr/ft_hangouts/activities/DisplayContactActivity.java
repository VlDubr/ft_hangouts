package ru.doubr.ft_hangouts.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import ru.doubr.ft_hangouts.R;
import ru.doubr.ft_hangouts.data.Contact;
import ru.doubr.ft_hangouts.data.Message;
import ru.doubr.ft_hangouts.data.messagesDB.DateTypeConverter;

import static ru.doubr.ft_hangouts.activities.MainActivity.messagesDAO;

public class DisplayContactActivity extends AppCompatActivity {

    private EditText firstName;
    private EditText lastName;
    private EditText phoneNumber;
    private Integer contactID;
    private Contact contact;
    private String imgURI;

    private final int RESULT_LOAD_IMAGE = 1;

    private ImageView avatarImage;

    public static boolean onPause = false;
    public static String pauseDate = null;

    private IntentFilter intentFilter;
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
    public void onResume() {
        super.onResume();
        MainActivity.onPause = false;
        CreateContactActivity.onPause = false;
        SendMessagesActivity.onPause = false;
        if (onPause) {
            Toast.makeText(getApplicationContext(), pauseDate, Toast.LENGTH_LONG).show();
            onPause = false;
        }

        registerReceiver(intentReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        onPause = true;
        pauseDate = DateTypeConverter.fromDate(new Date());
        unregisterReceiver(intentReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(MainActivity.COLOR));
        setTitle(R.string.displayContactTitle);

        firstName = findViewById(R.id.editTextPersonName);
        lastName = findViewById(R.id.editTextPersonLastName);
        phoneNumber = findViewById(R.id.editTextPhone);
        avatarImage = findViewById(R.id.avatarImageViewDC);

        Bundle extras = getIntent().getExtras();

        contactID = extras.getInt("contactID");
        contact = MainActivity.getContactById(contactID);
        if (contact == null) {
            finish();
        }

        setImageFromURI(contact.getImageSrc());
        firstName.setText(contact.getFirstName(), TextView.BufferType.EDITABLE);
        lastName.setText(contact.getLastName(), TextView.BufferType.EDITABLE);
        phoneNumber.setText(contact.getPhone(), TextView.BufferType.EDITABLE);

        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVED_ACTION");

    }

    public void deleteContact(View view) { //TODO БД!
        MainActivity.contactsArrayList.remove(contact);
        MainActivity.contactDAO.delete(contact);
        finish();
    }

    public void saveContact(View view) {
        int position = MainActivity.contactsArrayList.indexOf(contact);
        MainActivity.contactsArrayList.remove(position);
        MainActivity.contactDAO.delete(contact);

        String phoneNum = phoneNumber.getText().toString();
        if (!phoneNum.contains("+7") && phoneNum.indexOf("8") == 0) {
            phoneNum = phoneNum.replaceFirst("8", "+7");
        }

        Contact temp = MainActivity.getContactByNumber(phoneNumber.getText().toString());
        if (temp == null) {
            Contact newContact = new Contact(firstName.getText().toString(),
                    lastName.getText().toString(), phoneNum, contact.getImageSrc());
            MainActivity.contactDAO.insert(newContact);
            MainActivity.contactsArrayList.add(position, newContact);
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.phone_exist, Toast.LENGTH_LONG).show();
            MainActivity.contactDAO.insert(contact);
            MainActivity.contactsArrayList.add(position, contact);
        }
    }

    private void setImageFromURI(String uri) {
        try {
            Uri image = Uri.parse(uri);
            final InputStream imageStream = getContentResolver().openInputStream(image);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            avatarImage.setImageBitmap(selectedImage);
        } catch (FileNotFoundException | NullPointerException e) {
            avatarImage.setImageResource(R.drawable.cat);
        }
    }

    public void addAvatar(View view) {
        Intent takePicture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(takePicture, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = imageReturnedIntent.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                avatarImage.setImageBitmap(selectedImage);
                contact.setImageSrc(imageUri.toString());
                int x = 0;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}