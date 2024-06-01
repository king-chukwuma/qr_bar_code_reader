package com.chukwuma.scanner2

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
import com.chukwuma.scanner2.dto.CheckInResponse
import com.chukwuma.scanner2.service.InvitationService
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
//    private lateinit var cameraBtn: MaterialButton;
    private lateinit var checkInBtn: MaterialButton;
    private lateinit var checkOutBtn: MaterialButton;
//    private lateinit var galleryBtn: MaterialButton;
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
//        cameraBtn = findViewById(R.id.cameraBtn);
//        galleryBtn = findViewById(R.id.galleryBtn);
        checkInBtn = findViewById(R.id.checkInBtn);
        checkOutBtn = findViewById(R.id.checkOutBtn);
        imageIv = findViewById(R.id.imageIv);
//        scanBtn = findViewById(R.id.scanBtn);
//        resultTv = findViewById(R.id.resultTv);

        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        barCodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build();
        barCodeScanner = BarcodeScanning.getClient(barCodeScannerOptions!!)

        checkInBtn.cornerRadius = 0
        checkOutBtn.cornerRadius = 0


//        cameraBtn.setOnClickListener{
//            if (checkCameraPermission()) pickImageCamera();
//            else requestCameraPermission();
//        }

        checkInBtn.setOnClickListener{
            if (checkCameraPermission()) {
                pickImageCameraCheckIn()
            };
            else {
                requestCameraPermission()
            };
        }

//        checkInBtn.afte

        checkOutBtn.setOnClickListener{
            if (checkCameraPermission()) {
                pickImageCameraCheckOut()
            };
            else {
                requestCameraPermission()
            };
        }
//        galleryBtn.setOnClickListener{
//            if (checkStoragePermission()) pickImageGallery()
//            else requestStoragePermission()
//        }
//        scanBtn.setOnClickListener{
//            scan();
//        }

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

//            val valueType = barcode.valueType;

            call(y, isCheckIn)

//            when (valueType) {
//                Barcode.TYPE_WIFI -> handleTypeWifi(barcode, rawValue);
//                Barcode.TYPE_URL -> handleTypeUrl(barcode, rawValue);
//                Barcode.TYPE_EMAIL -> handleTypeEmail(barcode, rawValue);
//                Barcode.TYPE_CONTACT_INFO -> handleTypeContactInfo(barcode, rawValue);
//                else -> call(y);
//            }
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

    private fun handleTypeContactInfo(barcode: Barcode, rawValue: String?) {
        val typeContactInfo = barcode.contactInfo

        val title = "${typeContactInfo?.title}"
        val organization = "${typeContactInfo?.organization}"
        val name = "${typeContactInfo?.name?.first} ${typeContactInfo?.name?.last} "
        val phone = "${typeContactInfo?.name?.first} ${typeContactInfo?.phones?.get(0)?.number}"

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
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) pickImageCameraCheckIn();
                } else showToast("Camera & Storage permissions are required")
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    grantResults.forEach { x -> println(x) }
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) pickImageCameraCheckIn();
                } else showToast("Storage permission are required...")
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
        textView.text = "Successful!!!"
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
        textView.text = "Failed!!!"
        textView.setTextColor(Color.RED)

        // Create and display the toast
        with (Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }


}