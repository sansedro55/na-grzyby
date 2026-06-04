package pl.nagrzyby.app.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserLocationIfPermitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.district?.name ?: "Mapa")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.errorMessage!!)
                }
            }
            uiState.district != null -> {
                val district = uiState.district!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    Text(
                        text = "OpenStreetMap · ${district.region}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    if (uiState.userLatitude == null && !uiState.isLoadingUserLocation) {
                        OutlinedButton(
                            onClick = { viewModel.loadUserLocationIfPermitted() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Text(
                                if (uiState.locationDenied) {
                                    "Brak GPS — włącz lokalizację i spróbuj ponownie"
                                } else {
                                    "Pokaż moją lokalizację"
                                },
                            )
                        }
                    }
                    if (uiState.isLoadingUserLocation) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    OsmMapView(
                        districtLatitude = district.latitude,
                        districtLongitude = district.longitude,
                        districtName = district.name,
                        userLatitude = uiState.userLatitude,
                        userLongitude = uiState.userLongitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    }
}
