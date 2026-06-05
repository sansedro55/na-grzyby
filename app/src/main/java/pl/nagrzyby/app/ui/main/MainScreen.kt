package pl.nagrzyby.app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.platform.LocalContext

import pl.nagrzyby.app.R
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import android.net.Uri
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import pl.nagrzyby.app.ui.ForestViewModel
import pl.nagrzyby.app.ui.components.ForestDistrictCard
import pl.nagrzyby.app.ui.components.TopBarIconBtn
import pl.nagrzyby.app.ui.theme.TopBarBg
import pl.nagrzyby.app.util.hasLocationPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToDistrict: (districtId: String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToMapSearch: () -> Unit,
    viewModel: ForestViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val listState = rememberLazyListState()

    LaunchedEffect(uiState.isSortedByDistance) {
        if (uiState.isSortedByDistance) {
            listState.animateScrollToItem(0)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAllHistory()
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

    // Prośba o pozwolenie GPS przy pierwszym uruchomieniu (osobny launcher – nie wywołuje findNearest)
    val initialPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    LaunchedEffect(Unit) {
        if (!context.hasLocationPermission()) {
            initialPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TopBarIconBtn(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = "Postaw kawę",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://cuplink.to/sansedro")),
                            )
                        },
                    )
                },
                actions = {
                    TopBarIconBtn(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Pomoc",
                        onClick = onNavigateToHelp,
                    )
                    TopBarIconBtn(
                        imageVector = Icons.Default.History,
                        contentDescription = "Ostatnie analizy",
                        onClick = onNavigateToHistory,
                    )
                    TopBarIconBtn(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Powiadomienia",
                        onClick = { showNotificationSettings = true },
                    )
                    TopBarIconBtn(
                        imageVector = if (uiState.currentThemeMode == "dark") Icons.Default.LightMode
                            else Icons.Default.DarkMode,
                        contentDescription = "Motyw",
                        onClick = { viewModel.toggleTheme() },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBg,
                ),
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
        if (showAboutDialog) {
            val annotated = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("pomysł ")
                }
                pushStringAnnotation("sansedro", "mailto:sansedro@gmail.com")
                withStyle(SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                )) {
                    append("Sansedro")
                }
                pop()
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("\nwykonanie ")
                }
                pushStringAnnotation("opencode", "https://docs.ollama.com/integrations/opencode")
                withStyle(SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                )) {
                    append("OpenCode")
                }
                pop()
            }
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Text("O aplikacji", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.nagrzyby_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(72.dp),
                        )
                        ClickableText(
                            text = annotated,
                            style = MaterialTheme.typography.bodyLarge,
                            onClick = { offset ->
                                annotated.getStringAnnotations("sansedro", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse(annotation.item)
                                            },
                                        )
                                    }
                                annotated.getStringAnnotations("opencode", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse(annotation.item)
                                            },
                                        )
                                    }
                            },
                        )
                        HorizontalDivider()
                        Text(
                            text = "Źródła danych",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "• Open-Meteo — dane pogodowe (temperatura, opady, wilgotność)",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "• BDL (Bank Danych o Lasach) — drzewostan i wydzielenia leśne",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "• OpenStreetMap — podkład map",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clickable { showAboutDialog = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nagrzyby_logo),
                    contentDescription = "Logo NaGrzyby",
                    modifier = Modifier.size(56.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "NaGrzyby",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Sprawdź szanse na grzyby",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Szukaj nadleśnictwa lub miejscowości…") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            if (uiState.isLoadingBdl) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
                        .weight(1f)
                        .height(48.dp),
                    enabled = !uiState.isLoadingLocation,
                ) {
                    if (uiState.isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(20.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp).size(20.dp),
                        )
                    }
                    Text(
                        "Znajdź najbliższe",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Button(
                    onClick = onNavigateToMapSearch,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp).size(20.dp),
                    )
                    Text(
                        "Szukaj na mapie",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
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

                if (uiState.favoriteDistricts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ulubione",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
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
