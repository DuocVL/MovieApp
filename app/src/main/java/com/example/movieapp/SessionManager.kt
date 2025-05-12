package com.example.movieapp

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    companion object{
        private const val PREFS_NAME = "UserSessionPrefs" //Ten
        private const val KEY_USER_ID = "UID" //User ID
        private const val KEY_IS_LOGGED_IN = "isLoggedIn" //Trang thai dang nhap
    }

    private var isAnonymous = true

    // Tạo Master Key ma hóa
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    //Tao EncryptedSharedPreferences
    private val sharedPreferences : SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    //Luu thong tin dang nhap sau khi login thanh cong
    fun saveAuthToken( userId: String,isAnonymous : Boolean){
        this.isAnonymous = isAnonymous
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ID,userId)
        editor.putBoolean(KEY_IS_LOGGED_IN,true)
        editor.apply()
    }

    //Lay user ID
    fun getUserId(): String?{
        return sharedPreferences.getString(KEY_USER_ID,null)
    }
    //Kiem tra trang thai dang nhap
    fun isLoggedIn(): Boolean{
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN,false) && getUserId() != null
    }

    //Xoa thong tin dang nhap
    fun clearSession(){
        val editor = sharedPreferences.edit()
        editor.remove(KEY_USER_ID)
        editor.putBoolean(KEY_IS_LOGGED_IN,false)
        editor.apply()

    }

}