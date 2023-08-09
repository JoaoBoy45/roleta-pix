package com.tecbr.spinwheel;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class db {
    public Context contextT;
    public String SHARED_PREFS = "sharedPrefs";
    public String KEY = "pontos";

    public db(Context context){
        this.contextT = context;
    }


}
