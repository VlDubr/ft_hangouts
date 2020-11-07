package ru.doubr.ft_hangouts.data.contactsDB;

import androidx.room.RoomDatabase;

import ru.doubr.ft_hangouts.data.Contact;

@androidx.room.Database(entities = {Contact.class}, version = 1)
public abstract class ContactsDatabase extends RoomDatabase {

    public abstract ContactDAO contactDao();
}