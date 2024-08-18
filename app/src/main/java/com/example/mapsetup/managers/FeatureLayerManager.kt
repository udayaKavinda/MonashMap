package com.example.mapsetup.managers
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import com.example.mapsetup.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.DatasetFeature
import com.google.android.gms.maps.model.Feature
import com.google.android.gms.maps.model.FeatureLayer
import com.google.android.gms.maps.model.FeatureLayerOptions
import com.google.android.gms.maps.model.FeatureStyle
import com.google.android.gms.maps.model.FeatureType

class FeatureLayerManager(context: Context, private val rootView: View, private val map: GoogleMap) {
    private var datasetLayer: FeatureLayer? = null
    private val popupManager = PopupManager(context,rootView)

    init {
        datasetLayer = map.getFeatureLayer(
            FeatureLayerOptions.Builder()
                .featureType(FeatureType.DATASET)
                .datasetId(context.getString(R.string.DATASET_ID))
                .build()
        )
        styleDatasetsLayer()
        datasetLayer?.addOnFeatureClickListener { event ->
            val clickFeatures: MutableList<Feature> = event.features
//            lastGlobalId = null
            if (clickFeatures.get(0) is DatasetFeature) {
//                ((clickFeatures.get(0) as DatasetFeature).getDatasetAttributes().get("stroke"))
//            showPopupWindow()
                popupManager.showPopupWindow()

            }
        }
    }

    private fun styleDatasetsLayer() {

        // Create the style factory function.
        val styleFactory = FeatureLayer.StyleFactory { feature: Feature ->
            // Check if the feature is an instance of DatasetFeature.
            if (feature is DatasetFeature) {
//                Log.d("aaaaq", feature.getDatasetAttributes().toString())
                return@StyleFactory FeatureStyle.Builder()
                    // Define a style with green fill at 50% opacity and
                    // solid green border.
//                    .fillColor(Integer.parseUnsignedInt(feature.getDatasetAttributes().get("fillColor").toString().drop(2),16).toInt())
                    .strokeColor(Integer.parseUnsignedInt(feature.getDatasetAttributes().get("stroke").toString().drop(2),16).toInt())
                    .strokeWidth(feature.getDatasetAttributes().get("stroke-width").toString().toFloat()-1)
                    .build()
            }
            return@StyleFactory null
        }
        datasetLayer?.setFeatureStyle(styleFactory)

    }
}
