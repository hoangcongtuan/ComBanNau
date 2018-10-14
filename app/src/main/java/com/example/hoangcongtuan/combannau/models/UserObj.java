package com.example.hoangcongtuan.combannau.models;

import android.graphics.Bitmap;
import android.media.Image;

/**
 * Created by hoangcongtuan on 3/21/18.
 */

public class UserObj {
    public String address;
    public String avatar_url;
    public String email;
    public boolean male;
    public String phone;
    public String user_name;
    public float latitude;
    public float longtitude;
    public String store_name;

    public UserObj() {

    }

    public UserObj(UserObj obj) {
        this.address = obj.address;
        this.avatar_url = obj.avatar_url;
        this.email = obj.email;
        this.male = obj.male;
        this.phone = obj.phone;
        this.user_name = obj.user_name;
        this.latitude = obj.latitude;
        this.longtitude = obj.longtitude;
        this.store_name = obj.store_name;
    }


}
