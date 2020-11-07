package ru.doubr.ft_hangouts.data.messagesDB;

import androidx.room.RoomDatabase;

import ru.doubr.ft_hangouts.data.Message;

@androidx.room.Database(entities = {Message.class}, version = 2)
public abstract class MessagesDatabase extends RoomDatabase {

    public abstract MessagesDAO messagesDAO();
}