package ru.doubr.ft_hangouts.data.contactsDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ru.doubr.ft_hangouts.data.Contact;

@Dao
public interface ContactDAO {

    // Добавление Contact в бд
    @Insert
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    // Удаление Contact из бд
    @Delete
    void delete(Contact contact);

    //Получить все контакты
    @Query("SELECT * FROM contact")
    List<Contact> getAllContacts();


}
