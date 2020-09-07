package com.aek.visualnote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_save_data.*
import java.io.ByteArrayOutputStream
import java.lang.Exception


class SaveDataFragment : Fragment() {

    var selectedImage: Uri? = null
    var selectedBitmap: Bitmap? = null
    val MANIFEST_STORAGE_CODE = 0
    val GALLERY_REQUEST_CODE = 1
    var  enumActionNeed: ActionNeed? = null
    var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
             enumActionNeed = SaveDataFragmentArgs.fromBundle(it). enumActionNeed
            id = SaveDataFragmentArgs.fromBundle(it).id


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_save_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSave.setOnClickListener {
            save(it)
        }
        btnUpdate.setOnClickListener {
            update(it)
        }
        btnDelete.setOnClickListener {
            deleteData(it)
        }

        imageView.setOnClickListener {
            imageSelect(it)
        }


        if ( enumActionNeed == ActionNeed.EditData) {
            btnSave.visibility = View.GONE
            btnUpdate.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE

            loadOtherData()
        }

    }

    fun loadOtherData() {
        try {

            activity?.let {
                val database = it.openOrCreateDatabase("localnotes", Context.MODE_PRIVATE, null)

                val cursor = database.rawQuery("SELECT * FROM notes WHERE id = $id", null)
                val textIndex = cursor.getColumnIndex("notetext")
                val imageIndex = cursor.getColumnIndex("imagesrc")

                while (cursor.moveToNext()) {

                    val imgData = cursor.getBlob(imageIndex)

                    etNoteTitle.text =
                        Editable.Factory.getInstance().newEditable(cursor.getString(textIndex))

                    selectedBitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.size)

                    imageView.setImageBitmap(selectedBitmap)

                }

                cursor.close()

            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun imageSelect(view: View) {

        activity?.let {
            val permissionStatus = ContextCompat.checkSelfPermission(
                it.applicationContext,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                //izin verilmedi
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    MANIFEST_STORAGE_CODE
                )

            } else {
                //galeriye git
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)

            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == MANIFEST_STORAGE_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //izin aldık
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImage = data.data

            try {

                context?.let {
                    if (selectedImage != null) {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source =
                                ImageDecoder.createSource(it.contentResolver, selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap =
                                MediaStore.Images.Media.getBitmap(it.contentResolver, selectedImage)
                            imageView.setImageBitmap(selectedBitmap)
                        }
                    }
                }

            } catch (e: Exception) {

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun bitmapDownResizer(otherBitmap: Bitmap, maxSize: Int): Bitmap {
        var width = otherBitmap.width
        var height = otherBitmap.height

        var bitmapRatio: Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            //image is landspace
            width = maxSize
            val downSizeHeight = width / bitmapRatio
            height = downSizeHeight.toInt()
        } else {
            //image is portrait
            height = maxSize
            val downSizeWidth = height * bitmapRatio
            width = downSizeWidth.toInt()
        }

        return Bitmap.createScaledBitmap(otherBitmap, width, height, true)
    }

    fun save(view: View) {

        val noteText = etNoteTitle.text.toString()
        if (selectedBitmap != null) {
            var smallBitmap = bitmapDownResizer(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArrayImage = outputStream.toByteArray()

            try {

                context?.let {
                    val database = it.openOrCreateDatabase("localnotes", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, notetext VARCHAR,imagesrc BLOB)")

                    val sql = "INSERT INTO notes (notetext,imagesrc) VALUES (?,?)"
                    val statement = database.compileStatement(sql)
                    statement.bindString(1, noteText)
                    statement.bindBlob(2, byteArrayImage)

                    statement.execute()

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val action = SaveDataFragmentDirections.actionSaveDataFragmentToListFragment()
        Navigation.findNavController(view).navigate(action)

    }

    fun update(view: View) {

        val noteText = etNoteTitle.text.toString()
        if (selectedBitmap != null) {
            var smallBitmap = bitmapDownResizer(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArrayImage = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("localnotes", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, notetext VARCHAR,imagesrc BLOB)")

                    val sql = "UPDATE notes SET notetext = ?, imagesrc = ? WHERE id = ?"
                    val statement = database.compileStatement(sql)
                    statement.bindString(1, noteText)
                    statement.bindBlob(2, byteArrayImage)
                    statement.bindLong(3, id!!.toLong())

                    statement.execute()

                }

            } catch (e: Exception) {
                println(e.printStackTrace())
            }
        }

        val action = SaveDataFragmentDirections.actionSaveDataFragmentToListFragment()
        Navigation.findNavController(view).navigate(action)

    }

    fun deleteData(view: View) {
        try {
            context?.let {
                val database = it.openOrCreateDatabase("localnotes", Context.MODE_PRIVATE, null)
                database.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, notetext VARCHAR,imagesrc BLOB)")


                database.execSQL(
                    "DELETE FROM notes WHERE id = ?",
                    arrayOf(id)
                )

                val action = SaveDataFragmentDirections.actionSaveDataFragmentToListFragment()
                Navigation.findNavController(view).navigate(action)
            }

        } catch (e: Exception) {
            println(e.printStackTrace())
        }
    }
}