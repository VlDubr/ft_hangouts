package ru.doubr.ft_hangouts.data.messagesDB;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTypeConverter {


    @TypeConverter
    public static String fromDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd '-' HH:mm:ss");
        return dateFormat.format(date);
    }

    @TypeConverter
    public static Date toDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd '-' HH:mm:ss");
            return dateFormat.parse(date);
        }
        catch (ParseException e) {
            return new Date();
        }
    }

}