package com.example.breez.presentation.location

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.breez.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion


@Composable
fun MapboxPickLocationScreen(
    modifier: Modifier = Modifier,
    initialLat: Double? = null,
    initialLon: Double? = null,
    onConfirm: (lat: Double, lon: Double) -> Unit,
    onCancel: () -> Unit,
    onLocationChanged: ((lat: Double, lon: Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val defaultPoint = remember { Point.fromLngLat(31.708733, 26.571800) }

    val searchEngine = remember {
        SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings()
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<SearchSuggestion>>(emptyList()) }
    var currentSearchTask by remember { mutableStateOf<AsyncOperationTask?>(null) }
    var isSuggestionsExpanded by remember { mutableStateOf(false) }

    var selectedPoint by remember {
        mutableStateOf(
            if (initialLat != null && initialLon != null) {
                Point.fromLngLat(initialLon, initialLat)
            } else defaultPoint
        )
    }

    val viewportState = rememberMapViewportState {
        setCameraOptions(
            CameraOptions.Builder()
                .center(selectedPoint)
                .zoom(12.0)
                .build()
        )
    }

    val pinBitmap = remember { createLocationMarkerBitmap() }

    Box(modifier = modifier.fillMaxSize()) {

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState
        ) {
            MapEffect(selectedPoint) { mapView ->
                mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                    mapView.annotations.cleanup()

                    val pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
                    pointAnnotationManager.create(
                        PointAnnotationOptions()
                            .withPoint(selectedPoint)
                            .withIconImage(pinBitmap)
                    )
                }

                val listener = OnMapClickListener { point ->
                    selectedPoint = point
                    onLocationChanged?.invoke(point.latitude(), point.longitude())
                    viewportState.setCameraOptions(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(14.0)
                            .build()
                    )
                    true
                }
                mapView.gestures.addOnMapClickListener(listener)

                val hasFineLocation = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val hasCoarseLocation = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (hasFineLocation || hasCoarseLocation) {
                    mapView.location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        currentSearchTask?.cancel()
                        if (query.isBlank()) {
                            searchSuggestions = emptyList()
                            isSuggestionsExpanded = false
                        } else {
                            val options = SearchOptions.Builder().limit(5).build()
                            currentSearchTask = searchEngine.search(
                                query = query,
                                options = options,
                                callback = object : com.mapbox.search.SearchSuggestionsCallback {
                                    override fun onSuggestions(
                                        suggestions: List<SearchSuggestion>,
                                        responseInfo: ResponseInfo
                                    ) {
                                        searchSuggestions = suggestions
                                        isSuggestionsExpanded = suggestions.isNotEmpty()
                                    }

                                    override fun onError(e: Exception) {
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search location") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            AnimatedVisibility(visible = isSuggestionsExpanded) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(searchSuggestions) { suggestion ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        isSuggestionsExpanded = false
                                        searchQuery = suggestion.name
                                        searchEngine.select(
                                            suggestion,
                                            object : SearchSelectionCallback {
                                                override fun onResult(
                                                    suggestion: SearchSuggestion,
                                                    result: SearchResult,
                                                    info: ResponseInfo
                                                ) {
                                                    result.coordinate?.let { pt ->
                                                        selectedPoint = pt
                                                        // Notify about location change immediately
                                                        onLocationChanged?.invoke(pt.latitude(), pt.longitude())
                                                        viewportState.setCameraOptions(
                                                            CameraOptions.Builder()
                                                                .center(pt)
                                                                .zoom(14.0)
                                                                .build()
                                                        )
                                                    }
                                                }

                                                override fun onSuggestions(
                                                    suggestions: List<SearchSuggestion>,
                                                    info: ResponseInfo
                                                ) { }

                                                override fun onResults(
                                                    suggestion: SearchSuggestion,
                                                    results: List<SearchResult>,
                                                    info: ResponseInfo
                                                ) { }

                                                override fun onError(e: Exception) { }
                                            }
                                        )
                                    }
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                suggestion.address?.formattedAddress()?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp, bottom = 54.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onCancel) { Text("Cancel") }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { onConfirm(selectedPoint.latitude(), selectedPoint.longitude()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Confirm location")
            }
        }
        Spacer(modifier = Modifier.height(52.dp))
    }
}

private fun createLocationMarkerBitmap(size: Int = 78): Bitmap {
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val centerX = size / 2f
    val centerY = size / 2f
    val radius = size / 3f

    paint.apply {
        color = android.graphics.Color.parseColor("#4A90E2") // Nice blue color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, paint)

    paint.apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius * 0.6f, paint)

    paint.apply {
        color = android.graphics.Color.parseColor("#4A90E2")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius * 0.3f, paint)

    return bmp
}
