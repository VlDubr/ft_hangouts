package ru.doubr.ft_hangouts.data.messagesDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ru.doubr.ft_hangouts.data.Message;

@Dao
public interface MessagesDAO {

    // Добавить диалог в бд
    @Insert
    void insert(Message message);

    //Обновить сообщения диалога в бд
    @Update
    void update(Message message);

    // Удаление диалога из бд
    @Delete
    void delete(Message message);

    //Получить все диалоги
    @Query("SELECT * FROM Message")
    List<Message> getAllMessages();

    @Query("SELECT * FROM Message WHERE contactId = :contactId")
    List<Message> getDialogByContactId(Integer contactId);
}
