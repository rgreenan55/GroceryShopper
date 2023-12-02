package cs2063.groceryshopper

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.*
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.Date


class OCRActivity : AppCompatActivity() {
    // Attributes for storing the file photo path
    private lateinit var currentPhotoPath: String
    // private lateinit var imageFileName: String
    private var photoUri: Uri? = null

    // Activity listeners
    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ocr_activity)

        val actionbarString = "Receipt Capture"
        this.supportActionBar?.title = actionbarString
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cameraButton = findViewById<Button>(R.id.Camera_Button)
        cameraButton.setOnClickListener {
            photoUri = dispatchTakePictureIntent()
        }

        val emailButton = findViewById<Button>(R.id.Email_Button)
        emailButton.setOnClickListener {
            //dispatchSendEmailIntent()
            if (photoUri != null){
                runOCR(photoUri!!)
            }
            else{
                Toast.makeText(this, "Image Not Found", Toast.LENGTH_SHORT).show()
            }
        }

        // Register the activity listener
        setCameraActivityResultLauncher()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun dispatchSendEmailIntent(){
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            val addresses = arrayOf<String>("b.jacobs@unb.ca")
            data = Uri.parse("mailto:") // Only email apps handle this.
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, "CS2063 Lab 3")
            putExtra(Intent.EXTRA_TEXT, "This is a test email!")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun runOCR(photoUri: Uri){
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val img = InputImage.fromFilePath(this, photoUri)

        val result = recognizer.process(img)
            .addOnSuccessListener { visionText ->
                Toast.makeText(this, visionText.text, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun dispatchTakePictureIntent() : Uri? {
        var photoURI: Uri? = null
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            Log.d(TAG, "1")
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                Log.d(TAG, "2")
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                Log.d(TAG, "3")
                // Continue only if the File was successfully created
                photoFile?.also {
                    Log.d(TAG, "5")
                    photoURI = FileProvider.getUriForFile(
                        this,
                        "cs2063.groceryshopper.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    cameraActivityResultLauncher!!.launch(takePictureIntent)
                    Log.d(TAG, "6")
                }
            }
        }
        Log.d(TAG, "7")
        return photoURI
    }


    private fun setCameraActivityResultLauncher() {
        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                galleryAddPic()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun galleryAddPic() {
        Log.d(TAG, "Saving image to the gallery")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) and above
            mediaStoreAddPicToGallery()
        } else {
            // Pre Android 10
            mediaScannerAddPicToGallery()
        }
        Log.i(TAG, "Image saved!")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun mediaStoreAddPicToGallery() {
        // val name = imageFileName
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath)

        val contentValues = getContentValues()
        // contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        contentValues.put(MediaStore.Images.Media.IS_PENDING, true)

        val resolver = contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            saveImageToStream(bitmap, resolver.openOutputStream(imageUri))
            contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
            resolver.update(imageUri, contentValues, null, null)
        }
    }

    private fun getContentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        try {
            if (outputStream != null) {
                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error saving the file ", e)
        }
    }

    private fun mediaScannerAddPicToGallery() {
        val file = File(currentPhotoPath)
        MediaScannerConnection.scanFile(this@OCRActivity,
            arrayOf(file.toString()),
            arrayOf(file.name),
            null)
    }
    companion object {
        // String for LogCat documentation
        private const val TAG = "External Activity Calls"
    }
}