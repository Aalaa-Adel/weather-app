package com.example.breez.presentation.location

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.breez.R
import com.example.breez.presentation.theme.DarkPrimary
import com.example.breez.presentation.theme.DarkSurfaceVariant
import com.example.breez.presentation.theme.LightPrimary
import com.example.breez.presentation.theme.LightSurface
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val searchFieldContainerColor = if (isDark) {
        DarkSurfaceVariant
    } else {
        LightSurface.copy(alpha = 0.96f)
    }

    val searchFieldBorderColor = if (isDark) {
        DarkSurfaceVariant
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
    }

    val suggestionContainerColor = if (isDark) {
        DarkSurfaceVariant
    } else {
        LightSurface
    }

    val suggestionItemColor = if (isDark) {
        DarkSurfaceVariant
    } else {
        LightSurface
    }

    val confirmButtonColor = if (isDark) {
        DarkSurfaceVariant
    } else {
        LightPrimary
    }

    val confirmContentColor = Color.White

    val backButtonContainerColor = if (isDark) {
        DarkSurfaceVariant.copy(alpha = 0.92f)
    } else {
        LightSurface.copy(alpha = 0.96f)
    }

    val backButtonContentColor = if (isDark) {
        Color.White
    } else {
        DarkPrimary
    }

    val defaultPoint = remember {
        Point.fromLngLat(31.708733, 26.571800)
    }

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
            } else {
                defaultPoint
            }
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

    val pinBitmap = remember(isDark) {
        createLocationMarkerBitmap(
            primaryColor = if (isDark) {
                android.graphics.Color.parseColor("#A07EFF")
            } else {
                android.graphics.Color.parseColor("#7C52D6")
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState
        ) {
            MapEffect(selectedPoint) { mapView ->
                mapView.mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                    mapView.annotations.cleanup()

                    val pointAnnotationManager =
                        mapView.annotations.createPointAnnotationManager()

                    pointAnnotationManager.create(
                        PointAnnotationOptions()
                            .withPoint(selectedPoint)
                            .withIconImage(pinBitmap)
                    )
                }

                val listener = OnMapClickListener { point ->
                    selectedPoint = point

                    onLocationChanged?.invoke(
                        point.latitude(),
                        point.longitude()
                    )

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
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasCoarseLocation = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
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
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clickable { onCancel() },
                    shape = RoundedCornerShape(18.dp),
                    color = backButtonContainerColor,
                    tonalElevation = 0.dp,
                    shadowElevation = if (isDark) 4.dp else 8.dp,
                    border = BorderStroke(1.dp, searchFieldBorderColor)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = backButtonContentColor
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = searchFieldContainerColor,
                    tonalElevation = 0.dp,
                    shadowElevation = if (isDark) 4.dp else 8.dp,
                    border = BorderStroke(1.dp, searchFieldBorderColor)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            currentSearchTask?.cancel()

                            if (query.isBlank()) {
                                searchSuggestions = emptyList()
                                isSuggestionsExpanded = false
                            } else {
                                val options = SearchOptions.Builder()
                                    .limit(5)
                                    .build()

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

                                        override fun onError(e: Exception) = Unit
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_location),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = isSuggestionsExpanded
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 64.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = suggestionContainerColor,
                    tonalElevation = 0.dp,
                    shadowElevation = if (isDark) 3.dp else 6.dp,
                    border = BorderStroke(1.dp, searchFieldBorderColor)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(searchSuggestions) { suggestion ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(suggestionItemColor)
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

                                                        onLocationChanged?.invoke(
                                                            pt.latitude(),
                                                            pt.longitude()
                                                        )

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
                                                ) = Unit

                                                override fun onResults(
                                                    suggestion: SearchSuggestion,
                                                    results: List<SearchResult>,
                                                    info: ResponseInfo
                                                ) = Unit

                                                override fun onError(e: Exception) = Unit
                                            }
                                        )
                                    }
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                suggestion.address?.formattedAddress()?.let { address ->
                                    Text(
                                        text = address,
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

        Button(
            onClick = {
                onConfirm(
                    selectedPoint.latitude(),
                    selectedPoint.longitude()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 54.dp)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = confirmButtonColor,
                contentColor = confirmContentColor
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 5.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = stringResource(R.string.confirm_location),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun createLocationMarkerBitmap(
    size: Int = 78,
    primaryColor: Int
): Bitmap {
    val bmp = Bitmap.createBitmap(
        size,
        size,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val centerX = size / 2f
    val centerY = size / 2f
    val radius = size / 3f

    paint.apply {
        color = primaryColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius, paint)

    paint.apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius * 0.6f, paint)

    paint.apply {
        color = primaryColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, radius * 0.3f, paint)

    return bmp
}