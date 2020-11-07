package ru.doubr.ft_hangouts.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.Random;

import ru.doubr.ft_hangouts.data.messagesDB.DateTypeConverter;

@Entity
public class Message implements Comparable<Message> {

    @PrimaryKey
    private Integer messageId;

    private Integer contactId;

    private String message;

    @TypeConverters({DateTypeConverter.class})
    private Date date;

    private int isFrom; //1 - От контакта, 2 - от меня

    @Ignore
    public Message(Integer contactId, String message, Date date, int isFrom) {
        Random rand = new Random(System.currentTimeMillis());
        messageId = rand.nextInt(2147483647);
        this.contactId = contactId;
        this.message = message;
        this.date = date;
        this.isFrom = isFrom;
    }

    public Message(Integer messageId, Integer contactId, String message, Date date, int isFrom) {
        this.messageId = messageId;
        this.contactId = contactId;
        this.message = message;
        this.date = date;
        this.isFrom = isFrom;
    }

    @Override
    public int compareTo(Message message) {
        Date compareDate = message.getDate();
        return this.date.compareTo(compareDate);
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getContactId() {
        return contactId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getIsFrom() {
        return isFrom;
    }

    public void setIsFrom(int isFrom) {
        this.isFrom = isFrom;
    }
}
