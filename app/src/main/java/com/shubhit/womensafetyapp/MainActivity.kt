package com.shubhit.womensafetyapp

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.shubhit.womensafetyapp.databinding.ActivityMainBinding
import com.shubhit.womensafetyapp.utills.BatteryReceiver
import com.shubhit.womensafetyapp.utills.LocationUtills
import com.shubhit.womensafetyapp.utills.Preferences
import com.shubhit.womensafetyapp.utills.VoiceCommandManager
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var isLiveTracking = false
    private var countDownTimer: CountDownTimer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var videoUri: Uri? = null
    private val isRecordingAudio get() = mediaRecorder != null
    private var recordAudioStatus = false
    private var isPanicModeActive = false
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var batteryReceiver: BatteryReceiver
    private var isVoiceRecognitionActive = false
    private lateinit var voiceCommandManager: VoiceCommandManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val contactList = Preferences.emergencyContacts
        voiceCommandManager = VoiceCommandManager(
            context = this,
            onCommandReceived = { command ->
                handleVoiceCommand(command)
            },
            stopBlinking = {
                stopVoiceRecognition()
            }

        )
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(voiceCommandManager)
        val lowBatteryAlert: () -> Unit = {
            sendLowBatteryAlert()
        }
        batteryReceiver = BatteryReceiver(this, lowBatteryAlert)
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, intentFilter)

        setupButtons()
        setupClickListeners()

        binding.voiceCommondBtn.imageBtnCointainer.setOnClickListener {
            if (!isVoiceRecognitionActive) {
                startVoiceRecognition(speechRecognizer)
            }
        }
    }

    private fun startVoiceRecognition(speechRecognizer: SpeechRecognizer?) {
        isVoiceRecognitionActive = true
        disableAllButtons(true)


        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        speechRecognizer!!.startListening(intent)
    }

    private fun stopVoiceRecognition() {
        isVoiceRecognitionActive = false
        disableAllButtons(false)
    }


    private fun setupClickListeners() {
        binding.policeCon.imageBtnCointainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:110"))
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
            } else {
                startActivity(intent)
            }
        }

        binding.emrgencyContact.imageBtnCointainer.setOnClickListener {
            startActivity(Intent(this, EmergencyContacsActivity::class.java))
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_contacts -> {
                    startActivity(Intent(this, EmergencyContacsActivity::class.java))
                    false
                }

                R.id.nav_profile -> {
                    val intent = Intent(this, DetailsActivity::class.java)
                    intent.putExtra("FROM_BOTTOM_NAV", true)  // Flag to indicate the source
                    startActivity(intent)
                    false
                }

                else -> false
            }
        }

        binding.sosBtn.imageBtnCointainer.setOnClickListener {
            sendSosMessage()
        }

        binding.liveTracking.imageBtnCointainer.setOnClickListener {
            if (!isLiveTracking) {
                showDurationDialog()
            } else {
                stopLiveTracking()
            }
        }
        binding.recordVideo.imageBtnCointainer.setOnClickListener {
            startVideoRecording()
        }
        binding.recordAudio.imageBtnCointainer.setOnClickListener {
            if (recordAudioStatus) {
                recordAudioStatus = false
                stopAudioRecording()
            } else {
                recordAudioStatus = true
                startAudioRecording()
            }

        }

        binding.panicMode.imageBtnCointainer.setOnClickListener {
            if (!isPanicModeActive) {
                startPanicMode()
            } else {
                stopPanicMode()
            }
        }

        binding.logOutBtn.imageBtnCointainer.setOnClickListener {

            // Navigate to AuthScreen
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Optionally, show a toast message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        }

    }

    private fun setupButtons() {
        binding.emrgencyContact.btnI.setImageResource(R.drawable.emer)
        binding.emrgencyContact.btnT.setText("Emergency Contacts")

        binding.policeCon.btnI.setImageResource(R.drawable.pol)
        binding.policeCon.btnT.text = "Police Contact"

        binding.sosBtn.btnI.setImageResource(R.drawable.sos)
        binding.sosBtn.btnT.text = "SOS"

        binding.liveTracking.btnI.setImageResource(R.drawable.liveloc)
        binding.liveTracking.btnT.text = "Live Tracking"

        binding.recordAudio.btnI.setImageResource(R.drawable.audio)
        binding.recordAudio.btnT.text = "Voice Recording"

        binding.recordVideo.btnI.setImageResource(R.drawable.video)
        binding.recordVideo.btnT.text = "Video Recording"


        binding.panicMode.btnI.setImageResource(R.drawable.panic)
        binding.panicMode.btnT.text = "Panic Mode"

        binding.voiceCommondBtn.btnI.setImageResource(R.drawable.voicereco)
        binding.voiceCommondBtn.btnT.text = "Voice Recognition"

        binding.logOutBtn.btnI.setImageResource(R.drawable.logout)
        binding.logOutBtn.btnT.text = "Logout"


    }

    private fun disableAllButtons(disable: Boolean) {
        val buttons = listOf(
            binding.emrgencyContact.imageBtnCointainer,
            binding.policeCon.imageBtnCointainer,
            binding.sosBtn.imageBtnCointainer,
            binding.liveTracking.imageBtnCointainer,
            binding.recordAudio.imageBtnCointainer,
            binding.recordVideo.imageBtnCointainer,
            binding.panicMode.imageBtnCointainer,
            binding.logOutBtn.imageBtnCointainer
        )
        buttons.forEach { it.isEnabled = !disable }
    }


    private fun handleVoiceCommand(command: String) {
        stopVoiceRecognition()
        when {
            command.contains("start recording", ignoreCase = true) -> {
                // Start audio recording
                startAudioRecording()

            }

            command.contains("send SOS", ignoreCase = true) -> {
                // Send SOS message
                sendSosMessage()
            }

            command.contains("share location", ignoreCase = true) -> {
                // Share location
                startLiveTracking(1 * 60 * 1000)

            }

            command.contains("panic mode", ignoreCase = true) -> {
                // Share location
                startPanicMode()

            }


            else -> {
                Toast.makeText(this, "Unknown command", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun sendLowBatteryAlert() {
        LocationUtills.getLastKnownLocation(this) { location ->
            if (location != null) {
                val locationMessage =
                    "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                sendSmsToEmergencyContacts("My Battery is too low. My last location is: $locationMessage")
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopPanicMode() {
        isPanicModeActive = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()

        stopLiveTracking()
        stopAudioRecording()
        Toast.makeText(this, "Panic mode deactivated", Toast.LENGTH_SHORT).show()

    }

    private fun startPanicMode() {
        isPanicModeActive = true
        Toast.makeText(this, "Panic mode activated", Toast.LENGTH_SHORT).show()

        // Start live tracking
        startLiveTracking(1 * 60 * 1000L) // 10 minutes

        startBeepingAndVibrating()

        // Start audio recording
        startAudioRecording()

        // Stop panic mode after 10 minutes and send audio file
        val handler = android.os.Handler()
        handler.postDelayed({
            stopAudioRecording()
            println("PanicMode Audio recording stopped, attempting to upload audio file")


            // Upload the audio file and send the link via SMS
            uploadAudioFileAndSendLink()
        }, 1 * 60 * 1000L)
    }

    private fun uploadAudioFileAndSendLink() {
        uploadFile(audioFile) { audioFileUrl ->
            if (audioFileUrl != null) {
                val message =
                    "Please help me, I am in trouble. Here is the audio recording: $audioFileUrl"
                sendSmsToEmergencyContacts(message)
            } else {
                Toast.makeText(this, "Failed to upload audio file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFile(file: File?, onComplete: (String?) -> Unit) {
        if (file == null) {
            onComplete(null)
            return
        }

        // Get Firebase Storage instance
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        // Create a reference for the file in Firebase Storage
        val audioRef = storageRef.child("audio/${file.name}")

        // Upload the file to Firebase Storage
        val uploadTask = audioRef.putFile(Uri.fromFile(file))

        uploadTask.addOnSuccessListener {
            // Get the download URL of the uploaded file
            audioRef.downloadUrl.addOnSuccessListener { uri ->
                onComplete(uri.toString())
            }.addOnFailureListener {
                onComplete(null)
            }
        }.addOnFailureListener {
            onComplete(null)
        }
    }


    private fun startBeepingAndVibrating() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGenerator.startTone(
            ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
            10000
        ) // Beep for 10 seconds

        // Code to start vibration
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    10000,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            ) // Vibrate for 10 seconds
        } else {
            vibrator.vibrate(10000) // Vibrate for 10 seconds
        }
    }


    private fun stopAudioRecording() {
        if (isRecordingAudio) {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Toast.makeText(
                this,
                "Audio recording saved to: ${audioFile?.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startAudioRecording() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                3
            )
            return
        }

        audioFile = File(
            getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            "audio_record_${System.currentTimeMillis()}.3gp"
        )
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile?.absolutePath)
            try {
                prepare()
                start()
                Toast.makeText(this@MainActivity, "Audio recording started", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun startVideoRecording() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/WomenSafetyApp")
        }
        videoUri =
            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        startActivityForResult(intent, VIDEO_CAPTURE_REQUEST_CODE)

    }

    private fun showDurationDialog() {
        val options = arrayOf("1 Minute", "5 Minutes", "10 Minutes")
        val durations = arrayOf(1 * 60 * 1000L, 5 * 60 * 1000L, 10 * 60 * 1000L)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Duration")
        builder.setItems(options) { _, which ->
            startLiveTracking(durations[which])
        }
        builder.show()
    }

    private fun sendSosMessage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION),
                2
            )
        } else {
            fetchLocationAndSendMessage()
        }
    }

    private fun fetchLocationAndSendMessage() {
        LocationUtills.getLastKnownLocation(this) { location ->
            if (location != null) {
                val locationMessage =
                    "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                sendSmsToEmergencyContacts("Please help me, I am in trouble. My location is: $locationMessage")
            } else {
                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendSmsToEmergencyContacts(message: String) {
        val smsManager = SmsManager.getDefault()
        val contacts = Preferences.emergencyContacts ?: listOf()

        if (contacts.isNotEmpty()) {
            for (contact in contacts) {
                smsManager.sendTextMessage(contact.number, null, message, null, null)
            }
            Toast.makeText(this, "SOS message sent!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No emergency contacts available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLiveTracking(duration: Long) {
        isLiveTracking = true
        Toast.makeText(
            this,
            "Live tracking started, click again to stop tracking",
            Toast.LENGTH_SHORT
        ).show()

        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.liveTracking.btnT.text =
                    "Live Tracking ($secondsRemaining sec), click again to stop tracking"
            }

            override fun onFinish() {
                stopLiveTracking()
            }
        }.start()

        val handler = android.os.Handler()
        val runnable = object : Runnable {
            override fun run() {
                if (isLiveTracking) {
                    fetchLocationAndSendMessage()
                    handler.postDelayed(this, 10000)
                }
            }
        }
        handler.post(runnable)
    }

    private fun stopLiveTracking() {
        isLiveTracking = false
        countDownTimer?.cancel()
        binding.liveTracking.btnT.text = "Live Tracking"
        Toast.makeText(this, "Live tracking stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                fetchLocationAndSendMessage()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Video recording saved to: $videoUri", Toast.LENGTH_SHORT)
                .show()
        }
    }

    companion object {
        const val VIDEO_CAPTURE_REQUEST_CODE = 1001
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVoiceRecognition()

    }
}
