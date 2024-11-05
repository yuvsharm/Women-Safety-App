package com.shubhit.womensafetyapp


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.shubhit.womensafetyapp.databinding.ActivityEmergencyContacsBinding
import com.shubhit.womensafetyapp.utills.Preferences

class EmergencyContacsActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmergencyContacsBinding
    lateinit var contactAdapter: ContactAdapter
    private val CONTACT_PICKER_REQUEST = 1001
    val selectedContacts = Preferences.emergencyContacts!!.toMutableList()
    private val READ_CONTACTS_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyContacsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: MaterialToolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.fabAddContact.setOnClickListener {
            if (selectedContacts.size < 5) {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                startActivityForResult(intent, CONTACT_PICKER_REQUEST)
            } else {
                Toast.makeText(this, "You can only add up to 5 contacts", Toast.LENGTH_SHORT).show()
            }

        }




        contactAdapter = ContactAdapter(selectedContacts, onCallClick = { contact ->
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${contact.number}"))
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
            } else {
                startActivity(intent)
            }

        }, onDeleteClick = { contact ->
            // Remove the contact from the list
            if (selectedContacts.size>2){
                selectedContacts.remove(contact)
                // Notify the adapter that the data set has changed
                contactAdapter.notifyDataSetChanged()
                // Save the updated list back to preferences
                Preferences.emergencyContacts = selectedContacts
            }else{
                Toast.makeText(this, "Minimum 2 contacts required", Toast.LENGTH_SHORT).show()
            }

        }
        )
        binding.recyclerViewContacts.apply {
            layoutManager = LinearLayoutManager(this@EmergencyContacsActivity)
            adapter = contactAdapter
        }


    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { contactUri ->
                val cursor = contentResolver.query(contactUri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val contactName = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                    val contactId = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )

                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        val contactNumber = phoneCursor.getString(
                            phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        )
                        val contact = Contact(contactName, contactNumber)

                        // Check if the contact is already added
                        if (selectedContacts.none { it.name == contactName && it.number == contactNumber }) {
                            selectedContacts.add(contact)
                            Preferences.emergencyContacts=selectedContacts
                            contactAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this, "Contact already added", Toast.LENGTH_SHORT).show()
                        }

                        phoneCursor.close()
                    }
                    cursor.close()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, so set up the UI
                Preferences.emergencyContacts = selectedContacts
            } else {
                // Permission was denied, show a message to the user
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }

}