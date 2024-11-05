package com.shubhit.womensafetyapp.utills

import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.widget.Toast
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shubhit.womensafetyapp.Contact

object Preferences {

    private const val PREF_NAME = "UserDetails"
    private var sharedPreferences: SharedPreferences? = null


    fun setup(context: Context){
        sharedPreferences=context.getSharedPreferences("secret_shared_prefs",Context.MODE_PRIVATE)
    }

    fun clearAllPreferences(){
        sharedPreferences?.let {
            val editor=it.edit().clear()
            editor.apply()
        }

    }


    var userId:String?
        get()=Key.USER_ID.getString()
        set(value)=Key.USER_ID.setString(value)

    var phoneNumber:String?
        get() =Key.PHONE_NUMBER.getString()
        set(value) = Key.PHONE_NUMBER.setString(value)

    var name:String?
        get() =Key.NAME.getString()
        set(value) = Key.NAME.setString(value)

    var AlternatePhoneNumber:String?
        get() =Key.ALTERNATE_PHONE_NUMBER.getString()
        set(value) = Key.ALTERNATE_PHONE_NUMBER.setString(value)

    var gender:String?
        get() =Key.GENDER.getString()
        set(value) = Key.GENDER.setString(value)

    var age:String?
        get() =Key.AGE.getString()
        set(value) = Key.AGE.setString(value)

    var address:String?
        get() =Key.ADDRESS.getString()
        set(value) = Key.ADDRESS.setString(value)

    var state:String?
        get() =Key.STATE.getString()
        set(value) = Key.STATE.setString(value)

    var district:String?
        get() =Key.DISTRICT.getString()
        set(value) = Key.DISTRICT.setString(value)

    var emergencyContacts: List<Contact>?
        get() = Key.EMERGENCY_CONTACTS.getString()?.let {
            val type = object : TypeToken<List<Contact>>() {}.type
            Gson().fromJson(it, type)
        }
        set(value) = Key.EMERGENCY_CONTACTS.setString(value?.let {
            Gson().toJson(it)
        })

    var addressObject: Address?
        get() = Key.ADDRESS_OBJECT.getString()?.let {
            Gson().fromJson(it, Address::class.java)
        }
        set(value) = Key.ADDRESS_OBJECT.setString(value?.let {
            Gson().toJson(it)
        })

    var isLowBatteryAlertSent: Boolean
        get() = Key.LOW_BATTERY_ALERT_SENT.getBoolean()
        set(value) = Key.LOW_BATTERY_ALERT_SENT.setBoolean(value)








    private enum class Key{
        PHONE_NUMBER,
        USER_ID,
        NAME,
        ALTERNATE_PHONE_NUMBER,
        GENDER,
        AGE,
        ADDRESS,
        STATE,
        DISTRICT,
        EMERGENCY_CONTACTS,
        ADDRESS_OBJECT,
        LOW_BATTERY_ALERT_SENT;

        fun getInt(): Int? =
            if (sharedPreferences!!.contains(name)) sharedPreferences!!.getInt(name, 0) else null

        fun getLong(): Long? =
            if (sharedPreferences!!.contains(name)) sharedPreferences!!.getLong(name, 0) else null

        fun getString(): String? =
            if (sharedPreferences!!.contains(name)) sharedPreferences!!.getString(
                name,
                ""
            ) else null

        fun getBoolean(): Boolean =
            sharedPreferences!!.contains(name) && sharedPreferences!!.getBoolean(name, false)

        fun setBoolean(value: Boolean) =
            sharedPreferences!!.edit { putBoolean(name, value) }

        fun setInt(value: Int?) =
            value?.let { sharedPreferences!!.edit { putInt(name, value) } } ?: remove()

        fun setLong(value: Long?) =
            value?.let { sharedPreferences!!.edit { putLong(name, value) } } ?: remove()

        fun setString(value: String?) =
            value?.let { sharedPreferences!!.edit { putString(name, value) } } ?: remove()

        fun remove() = sharedPreferences!!.edit { remove(name) }



    }







}