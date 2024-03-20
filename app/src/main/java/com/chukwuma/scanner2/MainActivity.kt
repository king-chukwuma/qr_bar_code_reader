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


        cameraBtn.setOnClickListener{

        }
        cameraBtn.setOnClickListener{

        }
        cameraBtn.setOnClickListener{

        }
        cameraBtn.setOnClickListener{

        }





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
        ) == PackageManager.PERMISSION_DENIED
        return result
    }

    private fun requestStoragePermission () {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private fun checkCameraPermission (): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_DENIED
        return result
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}