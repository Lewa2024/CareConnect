package com.strathmore.CareConnect.ui.nurse

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import com.strathmore.CareConnect.data.repository.NurseWithDistance
import org.osmdroid.views.overlay.Marker

@Composable
fun NurseMapView(
    caregiverLat: Double,
    caregiverLng: Double,
    nurses: List<NurseWithDistance>,
    onNurseMarkerClick: (nurseId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        modifier = modifier.fillMaxWidth().height(260.dp),
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(caregiverLat, caregiverLng))
                mapViewRef = this
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            val selfMarker = Marker(mapView).apply {
                position = GeoPoint(caregiverLat, caregiverLng)
                title = "You"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(selfMarker)
            nurses.forEach { entry ->
                val nurse = entry.nurse
                val marker = Marker(mapView).apply {
                    position = GeoPoint(nurse.latitude, nurse.longitude)
                    title = "${nurse.firstName} ${nurse.lastName}"
                    snippet = "${nurse.qualification} — %.1f km".format(entry.distanceKm)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { clickedMarker, _ ->
                        clickedMarker.showInfoWindow()
                        onNurseMarkerClick(nurse.nurseId)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )

    // Keyed ONLY on lifecycleOwner now — not on mapViewRef.
    // Keying on mapViewRef caused this effect to re-run the instant the
    // MapView was first created (null -> MapView), which disposed the
    // OLD effect and called onDetach() on the brand-new map right away,
    // breaking its internal repository and crashing any later Marker().
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }
}