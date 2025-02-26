package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import java.util.Random

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var drawingView: DrawingView

    private lateinit var brushButton: ImageButton

    private lateinit var purpleButton: ImageButton

    private lateinit var redButton: ImageButton
    private lateinit var greenButton: ImageButton
    private lateinit var blueButton: ImageButton
    private lateinit var orangeButton: ImageButton

    private lateinit var undoButton: ImageButton

    private lateinit var colorPickerButton: ImageButton

    private lateinit var galleryButton: ImageButton

    private lateinit var saveButton: ImageButton

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            findViewById<ImageView>(R.id.gallery_image).setImageURI(result.data?.data)
        }

    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted && (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Permission $permissionName granted", Toast.LENGTH_LONG)
                        .show()
                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else if (isGranted && (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Permission $permissionName granted", Toast.LENGTH_LONG)
                        .show()
                    CoroutineScope(IO).launch {
                        saveDrawing(getBitmapFromView(findViewById(R.id.constraint_l3)))
                    }
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE || permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this,
                            "Permission $permissionName denied",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        brushButton = findViewById(R.id.brush_button)


        drawingView = findViewById(R.id.drawing_view)
        drawingView.changeBrushSize(23.toFloat())

        brushButton.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        purpleButton = findViewById<ImageButton>(R.id.purple_button)
        redButton = findViewById<ImageButton>(R.id.red_button)
        greenButton = findViewById<ImageButton>(R.id.green_button)
        blueButton = findViewById<ImageButton>(R.id.blue_button)
        orangeButton = findViewById<ImageButton>(R.id.orange_button)

        undoButton = findViewById(R.id.undo_button)

        colorPickerButton = findViewById(R.id.color_picker_button)

        galleryButton = findViewById(R.id.button_gallery)

        saveButton = findViewById(R.id.button_save)

        purpleButton.setOnClickListener(this)

        redButton.setOnClickListener(this)
        greenButton.setOnClickListener(this)
        blueButton.setOnClickListener(this)
        orangeButton.setOnClickListener(this)

        undoButton.setOnClickListener(this)

        colorPickerButton.setOnClickListener(this)

        galleryButton.setOnClickListener(this)

        saveButton.setOnClickListener(this)


    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this@MainActivity)
        brushDialog.setContentView(R.layout.dialog_brush)
        val seekBarProgress = brushDialog.findViewById<SeekBar>(R.id.dialog_seek_bar)
        val showProgressTV = brushDialog.findViewById<TextView>(R.id.dialog_text_view_progress)

        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, p1: Int, p2: Boolean) {
                drawingView.changeBrushSize(seekBar.progress.toFloat())
                showProgressTV.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        brushDialog.show()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.purple_button -> {
                drawingView.setColor("#D14EF6")
            }

            R.id.red_button -> {
                drawingView.setColor("#F64E4E")
            }

            R.id.green_button -> {
                drawingView.setColor("#7BF64E")
            }

            R.id.blue_button -> {
                drawingView.setColor("#4E78F6")
            }

            R.id.orange_button -> {
                drawingView.setColor("#F6894E")
            }

            R.id.undo_button -> {
                drawingView.undoPath()
            }

            R.id.color_picker_button -> {
                showColorPickerDialog()
            }

            R.id.button_gallery -> {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE

                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestStoragePermission()
                } else {

                    val pickIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    openGalleryLauncher.launch(pickIntent)
                }

            }

            R.id.button_save -> {
                //save the image
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "IF called", Toast.LENGTH_SHORT).show()
                    requestStoragePermission()
                } else {
                    val layout = findViewById<ConstraintLayout>(R.id.constraint_l3)
                    CoroutineScope(IO).launch {
                        saveDrawing(getBitmapFromView(layout))
                    }
                }
            }
        }
    }

    private fun showColorPickerDialog() {
        val colorPicker =
            AmbilWarnaDialog(this, Color.GREEN, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawingView.setColor(color)
                }

                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }
            })
        colorPicker.show()
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog()
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            )
        }
    }

    private fun showRationaleDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Storage Permission Required")
            .setMessage("Drawing App needs to Access Your External Storage")
            .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
        builder.create().show()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)


        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private suspend fun saveDrawing(bitmap: Bitmap) {
        val root =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                .toString()
        val myDir = File("$root/saved_images")
        myDir.mkdir()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val outPutFile = File(myDir, "Images-$n.jpg")
        if (outPutFile.exists()) {
            outPutFile.delete()
        } else {
            try {
                val out = FileOutputStream(outPutFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.stackTrace
            }

            withContext(Main) {
                Toast.makeText(
                    this@MainActivity,
                    "${outPutFile.absolutePath} saved!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}

