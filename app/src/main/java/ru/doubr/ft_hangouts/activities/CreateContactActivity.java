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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import ru.doubr.ft_hangouts.R;
import ru.doubr.ft_hangouts.data.Contact;
import ru.doubr.ft_hangouts.data.Message;
import ru.doubr.ft_hangouts.data.messagesDB.DateTypeConverter;

import static ru.doubr.ft_hangouts.activities.MainActivity.contactDAO;
import static ru.doubr.ft_hangouts.activities.MainActivity.messagesDAO;

public class CreateContactActivity extends AppCompatActivity {

    private EditText editFirstName;
    private EditText editLastName;
    private EditText editPhone;
    private ImageView avatarImage;

    private String imgURI;


    private final int RESULT_LOAD_IMAGE = 1;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(MainActivity.COLOR));
        setTitle(R.string.createContactTitle);

        editFirstName = findViewById(R.id.editTextPersonName);
        editLastName = findViewById(R.id.editTextPersonLastName);
        editPhone = findViewById(R.id.editTextPhone);
        avatarImage = findViewById(R.id.avatarImageViewCC);
        avatarImage.setImageResource(R.drawable.cat);

        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVED_ACTION");
    }


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.onPause = false;
        DisplayContactActivity.onPause = false;
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

    public void createContact(View view) {
        Contact temp = MainActivity.getContactByNumber(editPhone.getText().toString());
        String phoneNum = editPhone.getText().toString();
        if (!phoneNum.contains("+7") && phoneNum.indexOf("8") == 0) {
            phoneNum = phoneNum.replaceFirst("8", "+7");
        }
        if (temp == null) {
            Contact contact = new Contact(editFirstName.getText().toString(),
                    editLastName.getText().toString(), phoneNum, imgURI);
            contactDAO.insert(contact);
            MainActivity.contactsArrayList.add(contact);
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.phone_exist, Toast.LENGTH_LONG).show();
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
                imgURI = imageUri.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}