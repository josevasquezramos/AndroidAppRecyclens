package com.episi.recyclens.network

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.content.Context
import java.io.File
import java.io.FileOutputStream

class FotoRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // En FotoRepository.kt
    fun subirFoto(context: Context, uriOriginal: Uri, callback: (String?) -> Unit) {
        val fileComprimido = try {
            comprimirImagen(context, uriOriginal)
        } catch (e: Exception) {
            null
        }

        if (fileComprimido == null) {
            callback(null)
            return
        }

        val fotoRef = storageRef.child("reciclajes/${UUID.randomUUID()}.jpg")
        fotoRef.putFile(Uri.fromFile(fileComprimido))
            .addOnSuccessListener {
                // 1. Obtener URL de descarga
                fotoRef.downloadUrl.addOnSuccessListener { url ->
                    // 2. ¡Eliminar el archivo temporal ANTES de llamar al callback!
                    fileComprimido.delete()
                    callback(url.toString())
                }
            }
            .addOnFailureListener { e ->
                // 3. Asegurarse de borrar el archivo incluso si falla la subida
                fileComprimido.delete()
                callback(null)
            }
    }

    fun comprimirImagen(context: Context, uri: Uri, calidad: Int = 70): File {
        // 1. Decodificar el Uri a Bitmap
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // 2. Redimensionar (opcional, ajusta según necesidad)
        val maxWidth = 1024 // Ancho máximo en píxeles
        val scale = maxWidth.toFloat() / bitmap.width
        val matrix = Matrix()
        matrix.postScale(scale, scale)

        val bitmapRedimensionado = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        // 3. Comprimir y guardar en un archivo temporal
        val file = File.createTempFile("compressed_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(file)
        bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, calidad, outputStream)
        outputStream.close()

        return file // Devuelve el archivo comprimido
    }
}