package ru.doubr.ft_hangouts.data;

import android.os.SystemClock;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Random;

@Entity
public class Contact {

    @PrimaryKey
    private Integer id;

    @ColumnInfo(name = "last_name")
    private String lastName;

    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "phone")
    private String phone;

    private String imageSrc;

    public Contact(Integer id, String lastName, String firstName, String phone, String imageSrc) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.phone = phone;
        this.imageSrc = imageSrc;
    }

    @Ignore
    public Contact(String firstName, String lastName, String phone, String imageSrc) {
        Random rand = new Random(SystemClock.uptimeMillis());
        id = rand.nextInt(2147483647);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.imageSrc = imageSrc;
    }


    @Ignore
    public Contact(String firstName, String lastName, String phone) {
        Random rand = new Random(SystemClock.uptimeMillis());
        id = rand.nextInt(2147483647);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(Integer newId) {
        this.id = newId;
    }

    public void setFirstName(String str) {
        this.firstName = str;
    }

    public void setLastName(String str) {
        this.lastName = str;
    }

    public void setPhone(String str) {
        this.phone = str;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }
}
