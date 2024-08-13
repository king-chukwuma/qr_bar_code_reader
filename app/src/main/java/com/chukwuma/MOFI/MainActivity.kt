package com.chukwuma.MOFI

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chukwuma.MOFI.dto.CheckInResponse
import com.chukwuma.MOFI.service.InvitationService
import com.google.android.material.button.MaterialButton
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private val backendService = InvitationService.create();

    // UI Views
    private lateinit var checkInBtn: MaterialButton;
    private lateinit var checkOutBtn: MaterialButton;
    private lateinit var imageIv: ImageView;

    companion object {
        private const val CAMERA_REQUEST_CODE = 100;
    }

    private lateinit var cameraPermissions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Init UI Views
        checkInBtn = findViewById(R.id.checkInBtn);
        checkOutBtn = findViewById(R.id.checkOutBtn);
        imageIv = findViewById(R.id.imageIv);

        cameraPermissions = arrayOf(Manifest.permission.CAMERA);

//        barCodeScannerOptions = BarcodeScannerOptions.Builder()
//            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
//            .build();
//        barCodeScanner = BarcodeScanning.getClient(barCodeScannerOptions!!)

        checkInBtn.cornerRadius = 0
        checkOutBtn.cornerRadius = 0

        checkInBtn.setOnClickListener{
            if (checkCameraPermission()) {
                pickImageCameraCheckIn()
            };
            else {
                requestCameraPermission()
            };
        }

        checkOutBtn.setOnClickListener{
            if (checkCameraPermission()) {
                pickImageCameraCheckOut()
            };
            else {
                requestCameraPermission()
            };
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun call(id: String?, isCheckIn: Boolean) {

        runBlocking {
            val checkInResponse: CheckInResponse =
             if(isCheckIn)
                backendService.checkIn(id)
            else {
                backendService.checkOut(id)
            }

            if (checkInResponse.successful) (run {
                showCustomToastSuccess();
            }) else showCustomToastFailed(checkInResponse.message)
        }

    }

    private fun pickImageCameraCheckIn () {
        cameraActivityResultLauncher.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE))
    }

    private val cameraActivityResultLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(ScanContract()) {
            result ->

        if (result.contents == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        } else {
            val content = result.contents
            val x = content?.replace("\n", "\\n");
            val y = x?.split("\\n\\n")?.get(2)

            Log.d("UUID Invite", y.toString())

            call(y, true)
        }
    }

    private fun pickImageCameraCheckOut () {
        cameraActivityResultLauncher2.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE));
    }

    private val cameraActivityResultLauncher2 = registerForActivityResult<ScanOptions, ScanIntentResult>(ScanContract()) {
            result ->

        if (result.contents != null) {
            val content = result.contents
            val x = content?.replace("\n", "\\n");
            val y = x?.split("\\n\\n")?.get(2)

            Log.d("UUID Invite", y.toString())

            call(y, false)
        }
    }

    private fun checkCameraPermission (): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val resultStorage = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return resultCamera;
    }

    private fun requestCameraPermission () {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        when(requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    grantResults.forEach { x -> println(x) }
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) pickImageCameraCheckIn();
                } else showToast("Camera is required")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private fun showCustomToastSuccess() {
        // Inflate the custom layout
        val inflater: LayoutInflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custome_toast, findViewById(R.id.custom_toast_container))

        // Find the TextView in the custom layout and set the message
        val textView: TextView = layout.findViewById(R.id.toast_text)
        textView.text = getString(R.string.successful)
        textView.setTextColor(Color.GREEN)

        // Create and display the toast
        with (Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    private fun showCustomToastFailed(reason: String?) {
        // Inflate the custom layout
        val inflater: LayoutInflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custome_toast, findViewById(R.id.custom_toast_container))

        // Find the TextView in the custom layout and set the message
        val textView: TextView = layout.findViewById(R.id.toast_text)
        textView.text = reason
        textView.setTextColor(Color.RED)

        // Create and display the toast
        with (Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}