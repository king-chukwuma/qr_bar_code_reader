package com.chukwuma.MOFI

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chukwuma.MOFI.dto.CheckInResponse
import com.chukwuma.MOFI.service.InvitationService
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val backendService = InvitationService.create();

    // UI Views
    private lateinit var checkInBtn: MaterialButton;
    private lateinit var checkOutBtn: MaterialButton;
    private lateinit var imageIv: ImageView;

    companion object {
        private const val CAMERA_REQUEST_CODE = 100;
        private const val TAG = "MAIN_TAG";
    }

    private lateinit var cameraPermissions: Array<String>

    private var imageUri: Uri? = null;
    private var barCodeScannerOptions: BarcodeScannerOptions? = null
    private var barCodeScanner: BarcodeScanner? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Init UI Views
        checkInBtn = findViewById(R.id.checkInBtn);
        checkOutBtn = findViewById(R.id.checkOutBtn);
        imageIv = findViewById(R.id.imageIv);

        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        barCodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build();
        barCodeScanner = BarcodeScanning.getClient(barCodeScannerOptions!!)

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

    private fun scan(isCheckIn: Boolean) {
        if (imageUri == null) showToast("Pick Image First");
        else detectImageFromImage(isCheckIn)
    }

    private fun detectImageFromImage(isCheckIn: Boolean) {
        Log.d(TAG, "detectResultFromImage: ")
        try {
            val inputImage = InputImage.fromFilePath(this, imageUri!!);
            barCodeScanner!!.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    extractInfoFromQRCode(barcodes, isCheckIn);

                }
                .addOnFailureListener {e ->
                    Log.e(TAG, "detectImageFromImage: ", e);
                    showToast("Failed scanning due to ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "detectImageFromImage: ", e );
            showToast("Failed due to ${e.message}")
        }
    }

    private fun extractInfoFromQRCode(barcodes: List<Barcode>, isCheckIn: Boolean) {
        for (barcode in barcodes) {
            val bound = barcode.boundingBox;
            val corners = barcode.cornerPoints;

            val rawValue = barcode.rawValue;
            Log.d(TAG, "extractBarCodeQrCodeInfo: rawValue: $rawValue");

            val x = rawValue?.replace("\n", "\\n");
            val y = x?.split("\\n\\n")?.get(2)

            call(y, isCheckIn)
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
            }) else showCustomToastFailed()
        }

    }

    private fun pickImageCameraCheckIn () {
        val contentValues = ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Image");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data;
                Log.d(TAG, "cameraActivityResultLauncher :imageUri: $imageUri");
                imageIv.setImageURI(imageUri)
                scan(true)
            }
    }

    private fun pickImageCameraCheckOut () {
        val contentValues = ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Image");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher2.launch(intent);
    }

    private val cameraActivityResultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data;
            Log.d(TAG, "cameraActivityResultLauncher :imageUri: $imageUri");
            imageIv.setImageURI(imageUri)
            scan(false)
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
                } else showToast("Camera & Storage permissions are required")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private fun showToastResult(message: String) {
        Toast
            .makeText(this, message, Toast.LENGTH_LONG)
            .show();
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

    private fun showCustomToastFailed() {
        // Inflate the custom layout
        val inflater: LayoutInflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custome_toast, findViewById(R.id.custom_toast_container))

        // Find the TextView in the custom layout and set the message
        val textView: TextView = layout.findViewById(R.id.toast_text)
        textView.text = getString(R.string.failed)
        textView.setTextColor(Color.RED)

        // Create and display the toast
        with (Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}