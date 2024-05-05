package com.chukwuma.scanner2

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    // UI Views
    private lateinit var cameraBtn: MaterialButton;
    private lateinit var galleryBtn: MaterialButton;
    private lateinit var imageIv: ImageView;
    private lateinit var scanBtn: MaterialButton;
    private lateinit var resultTv: TextView;

    companion object {
        private const val CAMERA_REQUEST_CODE = 100;
        private const val STORAGE_REQUEST_CODE = 101;
        private const val TAG = "MAIN_TAG";
    }

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    private var imageUri: Uri? = null;
    private var barCodeScannerOptions: BarcodeScannerOptions? = null
    private var barCodeScanner: BarcodeScanner? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // Init UI Views
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        imageIv = findViewById(R.id.imageIv);
        scanBtn = findViewById(R.id.scanBtn);
        resultTv = findViewById(R.id.resultTv);

        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        barCodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build();
        barCodeScanner = BarcodeScanning.getClient(barCodeScannerOptions!!)


        cameraBtn.setOnClickListener{
            if (checkCameraPermission()) pickImageCamera();
            else requestCameraPermission();
        }
        galleryBtn.setOnClickListener{
            if (checkStoragePermission()) pickImageGallery()
            else requestStoragePermission()
        }
        scanBtn.setOnClickListener{
            if (imageUri == null) showToast("Pick Image First");
            else detectImageFromImage();
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun detectImageFromImage() {
        Log.d(TAG, "detectRsultFromImage: ")
        try {
            val inputImage = InputImage.fromFilePath(this, imageUri!!);
            val barcodeResult = barCodeScanner!!.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    extractBarCodeQrCodeInfo(barcodes);

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

    private fun extractBarCodeQrCodeInfo(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            val bound = barcode.boundingBox;
            val corners = barcode.cornerPoints;

            val rawValue = barcode.rawValue;
            Log.d(TAG, "extractBarCodeQrCodeInfo: rawValue: $rawValue");

            val valueType = barcode.valueType;

            when (valueType) {
                Barcode.TYPE_WIFI -> handleTypeWifi(barcode, rawValue);
                Barcode.TYPE_URL -> handleTypeUrl(barcode, rawValue);
                Barcode.TYPE_EMAIL -> handleTypeEmail(barcode, rawValue);
                Barcode.TYPE_CONTACT_INFO -> handleTypeContactInfo(barcode, rawValue);
                else -> resultTv.text = "rawValue: $rawValue";
            }
        }
    }

    private fun handleTypeContactInfo(barcode: Barcode, rawValue: String?) {
        val typeContactInfo = barcode.contactInfo

        val title = "${typeContactInfo?.title}"
        val organization = "${typeContactInfo?.organization}"
        val name = "${typeContactInfo?.name?.first} ${typeContactInfo?.name?.last} "
        val phone = "${typeContactInfo?.name?.first} ${typeContactInfo?.phones?.get(0)?.number}"

        Log.d(TAG, "extractBarCodeQrCodeInfo: TYPE_CONTACT_INFO");
        Log.d(TAG, "extractBarCodeQrCodeInfo: title: $title");
        Log.d(TAG, "extractBarCodeQrCodeInfo: organization: $organization");
        Log.d(TAG, "extractBarCodeQrCodeInfo: name: $name");
        Log.d(TAG, "extractBarCodeQrCodeInfo: phone: $phone");

        resultTv.text =
            "TYPE_CONTACT_INFO \ntitle: $title \norganization: $organization \nname: $name \n\nphone: $phone \n\nrawValue: $rawValue"

    }

    private fun handleTypeEmail(barcode: Barcode, rawValue: String?) {
        val typeEmail = barcode.email

        val address =  "${typeEmail?.address}"
        val body =  "${typeEmail?.body}"
        val subject =  "${typeEmail?.subject}"

        Log.d(TAG, "extractBarCodeQrCodeInfo: TYPE_EMAIL")
        Log.d(TAG, "extractBarCodeQrCodeInfo: address: $address")
        Log.d(TAG, "extractBarCodeQrCodeInfo: body: $body")
        Log.d(TAG, "extractBarCodeQrCodeInfo: subject: $subject")

        resultTv.text =
            "TYPE_EMAIL \nemail: $address \nsubject: $subject \n\nbody: $body \n\nrawValue: $rawValue"
    }

    private fun handleTypeUrl(barcode: Barcode, rawValue: String?) {
        val typeUrl = barcode.url;

        val title = "${typeUrl?.title}"
        val url = "${typeUrl?.url}"

        Log.d(TAG, "extractBarCodeQrCodeInfo: TYPE_URL")
        Log.d(TAG, "extractBarCodeQrCodeInfo: title: $title")
        Log.d(TAG, "extractBarCodeQrCodeInfo: url: $url")

        resultTv.text =
            "TYPE_URL \ntitle: $title \nurl: $url \n\nrawValue: $rawValue"
    }

    private fun handleTypeWifi(
        barcode: Barcode,
        rawValue: String?
    ) {
        val typeWifi = barcode.wifi

        val ssid = "${typeWifi?.ssid}"
        val password = "${typeWifi?.password}"
        var encryptionType = "${typeWifi?.encryptionType}"

        if (encryptionType == "1") {
            encryptionType = "OPEN"
        } else if (encryptionType == "2") {
            encryptionType = "WPA"
        } else if (encryptionType == "3") {
            encryptionType = "WEP"
        }

        Log.d(TAG, "extractBarCodeQrCodeInfo: TYPE_WIFI")
        Log.d(TAG, "extractBarCodeQrCodeInfo: :ssid $ssid")
        Log.d(TAG, "extractBarCodeQrCodeInfo: :password $password")
        Log.d(TAG, "extractBarCodeQrCodeInfo: :encryptionType $encryptionType")

        resultTv.text =
            "TYPE_WIFI \nssid: $ssid \npassword: $password \nencryptionType: $encryptionType\n\nrawValue: $rawValue"
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent);
    }

    private val galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        result -> if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data;
                imageUri = data?.data
                Log.d(TAG, ": imageUri: $imageUri");
                imageIv.setImageURI(imageUri);
        } else showToast("Canceled!!!")
    }

    private fun pickImageCamera () {
        val contentValues = ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Image");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
    }

    private val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data;
                Log.d(TAG, "cameraActivityResultLauncher :imageUri: $imageUri");
                imageIv.setImageURI(imageUri)
            }
    }

    private fun checkStoragePermission (): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result
    }

    private fun requestStoragePermission () {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
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
        return resultCamera && resultStorage;
    }

    private fun requestCameraPermission () {
        ActivityCompat.requestPermissions(this, storagePermissions, CAMERA_REQUEST_CODE);
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
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) pickImageCamera();
                } else showToast("Camera & Storage permissions are required")
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    grantResults.forEach { x -> println(x) }
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) pickImageCamera();
                } else showToast("Storage permission are required...")
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}