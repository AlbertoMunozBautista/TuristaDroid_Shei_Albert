package com.example.turistadroid_shei_albert

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.net.toFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

object Utilidades {

    //Crear nombre del fichero
    public fun crearNombreFichero(): String {
        return "camara-" + UUID.randomUUID().toString() + ".jpg"
    }

    //Guarda una imagen en un directorio (publico)
    fun salvarImagen(path: String, nombre: String, context: Context): File? {
        // Directorio publico
        val dirFotos = File((context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath) + path)
        // Si no existe el directorio, lo creamos solo si es publico
        if (!dirFotos.exists()) {
            dirFotos.mkdirs()
        }
        try {
            val f = File(dirFotos, nombre)
            f.createNewFile()
            return f
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return null
    }

    //Comprimimos una imagen
    fun comprimirImagen(fichero: File, bitmap: Bitmap, compresion: Int) {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compresion, bytes)
        val fo = FileOutputStream(fichero)
        fo.write(bytes.toByteArray())
        fo.close()
    }

    //Añadimos una foto a la galeria con todos sus datos
    fun añadirImagenGaleria(foto: Uri, nombre: String, context: Context): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "imagen")
        values.put(MediaStore.Images.Media.DISPLAY_NAME, nombre)
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATA, foto.toFile().absolutePath)
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    }


    //Copiar imagen en una ruta
    fun copiarImagen(bitmap: Bitmap, path: String, compresion: Int, context: Context) {
        val nombre = crearNombreFichero()
        val fichero =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + path + File.separator + nombre
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compresion, bytes)
        val fo = FileOutputStream(fichero)
        fo.write(bytes.toByteArray())
        fo.close()
    }


   //Pasamos la imagen en base64 a Bitmap
    fun base64ToBitmap(b64: String?): Bitmap? {
        val imageAsBytes = Base64.decode(b64?.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

    //Pasamos una imagen de Bitmap a ByteArray
    fun toBiteArray(imagen: Bitmap?): ByteArray? {
        val baos = ByteArrayOutputStream()
        imagen?.compress(Bitmap.CompressFormat.JPEG, 100, baos) // bm is the bitmap object
        return baos.toByteArray()
    }



}
