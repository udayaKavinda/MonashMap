import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.FileOutputStream
import java.io.OutputStream

class FileManager(private val context: Context) {

    // Function to save string content to external storage
    fun saveFileToExternalStorage(fileName: String, fileContent: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }

            val contentResolver: ContentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let {
                return try {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(fileContent.toByteArray())
                    }
                    Toast.makeText(context, "File saved successfully!", Toast.LENGTH_SHORT).show()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
        return false
    }

    // Function to save byte array content to external storage
    fun saveFileToExternalStorage(fileName: String, fileContent: ByteArray): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }

            val contentResolver: ContentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let {
                return try {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(fileContent)
                    }
                    Toast.makeText(context, "File saved successfully!", Toast.LENGTH_SHORT).show()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
        return false
    }

    fun saveByteArraysToFile(fileName: String, byteArrays: Array<ByteArray>): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val directory = Environment.DIRECTORY_DOCUMENTS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check if the file already exists
            val uri = getExistingFileUri(fileName, directory)

            val finalUri = uri ?: run {
                // If the file does not exist, create a new one
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                }
                contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            }

            finalUri?.let {
                return try {
                    // Open the file for appending if it exists or was newly created
                    contentResolver.openOutputStream(it, "wa")?.use { outputStream ->
                        byteArrays.forEach { byteArray ->
                            outputStream.write(byteArray)
                        }
                    }

//                    (context as Activity).runOnUiThread {
//                        Toast.makeText(context, "Data saved to file successfully!", Toast.LENGTH_SHORT).show()
//
//                    }
//                    Log.i("ScanCallback", "saved")
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
//                    Log.i("ScanCallback", "saving failed")
//                    Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
        return false
    }

    // Helper function to check if the file exists and get its URI
//    private fun getExistingFileUri(fileName: String, directory: String): Uri? {
//        val contentResolver: ContentResolver = context.contentResolver
//        val projection = arrayOf(MediaStore.MediaColumns._ID)
//        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
//        val selectionArgs = arrayOf(fileName, "$directory/")
//
//        contentResolver.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null)?.use { cursor ->
//            if (cursor.moveToFirst()) {
//                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
//                return Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
//            }
//        }
//        return null
//    }

    fun savePairsToCsvFile(fileName: String, data: MutableList<List<Pair<Int, Int>>>): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val directory = Environment.DIRECTORY_DOCUMENTS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check if the file already exists
            val uri = getExistingFileUri(fileName, directory)

            val finalUri = uri ?: run {
                // If the file does not exist, create a new one
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                }
                contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            }

            finalUri?.let {
                return try {
                    // Open the file for appending if it exists or was newly created
                    contentResolver.openOutputStream(it, "wa")?.use { outputStream ->
                        // Convert the data to CSV format and write it
                        val csvData = convertPairsToCsv(data)
                        outputStream.write(csvData.toByteArray())
                    }

                    // Optionally, show a Toast message or log
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
        return false
    }

    // Helper function to check if the file exists and get its URI
    private fun getExistingFileUri(fileName: String, directory: String): Uri? {
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(fileName, "$directory/")

        contentResolver.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                return Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
            }
        }
        return null
    }

    // Function to convert List<Pair<Int, Int>> array to CSV format
    private fun convertPairsToCsv(data: MutableList<List<Pair<Int, Int>>>): String {
        val stringBuilder = StringBuilder()

        // Iterate through each row (i.e., each element in the array)
        for (row in data) {
            // For each row, iterate through each Pair and append real and imaginary values
            row.forEach { pair ->
                stringBuilder.append("${pair.first},${pair.second},") // Add real and imaginary as CSV columns
            }
            stringBuilder.setLength(stringBuilder.length - 1) // Remove the last comma
            stringBuilder.append("\n") // End the row
        }

        return stringBuilder.toString()
    }

}
