package com.example.mapsetup.other


import android.widget.Toast
import com.example.mapsetup.managers.FileManager

object RadarDataProcessor {
    private var dataList: MutableList<List<Pair<Int, Int>>> = mutableListOf()
    private var save100SweepsToFiles = false
    private var categoryLabel = "1"
    private var fileName = "test"

    fun collect100Sweeps(int16Values: List<Pair<Int, Int>>, fileManager: FileManager) {
        if (save100SweepsToFiles) {
            if (dataList.size == 100) {
                saveData(fileManager)
            } else {
                dataList.add(int16Values)
            }
        }
    }

    fun saveDataToFile(fileName: String, categoryLabel:String) {
        this.fileName = fileName
        this.categoryLabel = categoryLabel
        save100SweepsToFiles = true
    }

    private fun saveData(fileManager: FileManager) {
        if(fileManager.savePairsToCsvFile(fileName +".csv", dataList, categoryLabel)){
            Toast.makeText(fileManager.context, "${categoryLabel} Data saved to file ${fileName}", Toast.LENGTH_SHORT).show()
        }
        dataList.clear()
        save100SweepsToFiles = false
    }
}
