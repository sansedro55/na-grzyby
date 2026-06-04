package pl.nagrzyby.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import pl.nagrzyby.app.ui.ForestViewModel
import pl.nagrzyby.app.ui.components.ForestDistrictCard
import pl.nagrzyby.app.ui.components.RecentVerdictCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDistrict: (districtId: String) -> Unit,
    viewModel: ForestViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNotificationSettings by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshRecentVerdicts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setNotificationsEnabled(true)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Włącz powiadomienia w ustawieniach systemu")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.findNearestDistricts()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Wymagane uprawnienie do lokalizacji")
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Na grzyby") },
                actions = {
                    IconButton(onClick = { showNotificationSettings = true }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Powiadomienia")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (showNotificationSettings) {
            AlertDialog(
                onDismissRequest = { showNotificationSettings = false },
                title = { Text("Powiadomienia") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Co ok. 12 godzin sprawdzamy pogodę w ulubionych nadleśnictwach. " +
                                "Dostaniesz powiadomienie, gdy szansa na grzyby wyniesie co najmniej 70%.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (!enabled) {
                                        viewModel.setNotificationsEnabled(false)
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS,
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            viewModel.setNotificationsEnabled(true)
                                        } else {
                                            notificationPermissionLauncher.launch(
                                                Manifest.permission.POST_NOTIFICATIONS,
                                            )
                                        }
                                    } else {
                                        viewModel.setNotificationsEnabled(true)
                                    }
                                },
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Włączone")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationSettings = false }) {
                        Text("OK")
                    }
                },
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Nadleśnictwo lub miejscowość (np. Balczewo)…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
            )

            if (uiState.isLoadingBdl) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted || coarseGranted) {
                        viewModel.findNearestDistricts()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                enabled = !uiState.isLoadingLocation,
            ) {
                if (uiState.isLoadingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Text("Znajdź najbliższe leśnictwa")
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.placeSearchCaption?.let { caption ->
                    item {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }

                if (uiState.recentVerdicts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ostatnie analizy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(
                        uiState.recentVerdicts,
                        key = { "recent_${it.districtId}_${it.createdAtEpochMs}" },
                    ) { verdict ->
                        RecentVerdictCard(
                            verdict = verdict,
                            onClick = { onNavigateToDistrict(verdict.districtId) },
                        )
                    }
                }

                if (uiState.favoriteDistricts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ulubione",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(uiState.favoriteDistricts, key = { "fav_${it.id}" }) { district ->
                        ForestDistrictCard(
                            district = district,
                            onClick = { onNavigateToDistrict(district.id) },
                            isFavorite = true,
                            onFavoriteClick = { viewModel.toggleFavorite(district.id) },
                        )
                    }
                    item {
                        Text(
                            text = "Wszystkie wyniki",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                    }
                }

                if (uiState.districts.isEmpty()) {
                    item {
                        Text(
                            text = "Brak wyników. Wpisz min. 3 znaki (nadleśnictwo lub miejscowość).",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val listDistricts = if (uiState.favoriteDistricts.isEmpty()) {
                        uiState.districts
                    } else {
                        uiState.districts.filter { d ->
                            uiState.favoriteDistricts.none { it.id == d.id }
                        }
                    }
                    items(listDistricts, key = { it.id }) { district ->
                        ForestDistrictCard(
                            district = district,
                            onClick = { onNavigateToDistrict(district.id) },
                            isFavorite = uiState.favoriteIds.contains(district.id),
                            onFavoriteClick = { viewModel.toggleFavorite(district.id) },
                        )
                    }
                }
            }
        }
    }
}
