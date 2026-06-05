package pl.nagrzyby.app.ui.map

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.nagrzyby.app.data.model.ForestDistrict
import pl.nagrzyby.app.ui.components.TopBarIconBtn
import pl.nagrzyby.app.ui.theme.ScoreGreen
import pl.nagrzyby.app.ui.theme.ScoreYellow
import pl.nagrzyby.app.ui.theme.TopBarBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToDistrict: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserLocationIfPermitted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TopBarIconBtn(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Wróć",
                        onClick = onBack,
                    )
                },
                actions = {
                    TopBarIconBtn(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Strona główna",
                        onClick = onNavigateToHome,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBg,
                ),
            )
        },
    ) { padding ->
        when {
            uiState.isLoading || uiState.district == null && uiState.isLoadingUserLocation -> {
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
            else -> {
                MapContent(
                    padding = padding,
                    district = uiState.district,
                    region = uiState.district?.region,
                    viewModel = viewModel,
                    uiState = uiState,
                    onNavigateToDistrict = onNavigateToDistrict,
                )
            }
        }
    }
}

@Composable
private fun MapContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    district: ForestDistrict?,
    region: String?,
    viewModel: MapViewModel,
    uiState: MapUiState,
    onNavigateToDistrict: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (region != null) "OpenStreetMap · $region" else "Wybierz punkt na mapie",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.tappedLatitude != null) {
                TextButton(onClick = { viewModel.clearTappedPoint() }) {
                    Icon(Icons.Default.Close, contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp))
                    Text("Anuluj")
                }
            }
        }
        if (uiState.userLatitude == null && !uiState.isLoadingUserLocation) {
            TextButton(
                onClick = { viewModel.loadUserLocationIfPermitted() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
            districtLatitude = district?.latitude ?: (uiState.userLatitude ?: 52.0),
            districtLongitude = district?.longitude ?: (uiState.userLongitude ?: 19.0),
            districtName = district?.name ?: "Polska",
            userLatitude = uiState.userLatitude,
            userLongitude = uiState.userLongitude,
            tappedLatitude = uiState.tappedLatitude,
            tappedLongitude = uiState.tappedLongitude,
            onLongPress = { lat, lon -> viewModel.onMapTapped(lat, lon) },
            modifier = Modifier
                .fillMaxWidth()
                .let { mod ->
                    if (uiState.nearbyDistricts.isEmpty()) mod.weight(1f)
                    else mod.height(0.dp)
                },
        )

        if (uiState.tappedLatitude != null && uiState.nearbyDistricts.isEmpty()) {
            Button(
                onClick = { viewModel.searchNearby() },
                enabled = !uiState.isSearchingNearby,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
            ) {
                if (uiState.isSearchingNearby) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Szukaj nadleśnictw w pobliżu")
            }
        }

        if (uiState.nearbyDistricts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        "Nadleśnictwa w pobliżu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(uiState.nearbyDistricts) { d ->
                    NearbyDistrictCard(
                        district = d,
                        onClick = { onNavigateToDistrict(d.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyDistrictCard(district: ForestDistrict, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = district.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                district.distanceKm?.let { km ->
                    Text(
                        text = "$km km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (km <= 5) ScoreGreen else ScoreYellow,
                    )
                }
            }
            Text(
                text = "Szczegóły >",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
