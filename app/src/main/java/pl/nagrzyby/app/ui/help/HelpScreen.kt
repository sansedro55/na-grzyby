package pl.nagrzyby.app.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.nagrzyby.app.ui.components.TopBarIconBtn
import pl.nagrzyby.app.ui.theme.TopBarBg
import pl.nagrzyby.app.ui.theme.TopBarButton

private data class HelpSection(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val sections = listOf(
    HelpSection(
        icon = Icons.Default.Home,
        title = "Ekran główny",
        description = "Lista nadleśnictw w Polsce. Użyj wyszukiwarki, aby znaleźć konkretne nadleśnictwo " +
            "lub miejscowość. Kliknij „Znajdź najbliższe”, aby posortować według odległości. " +
            "Kliknij „Szukaj na mapie”, aby otworzyć mapę i wyszukać nadleśnictwa w dowolnym punkcie. " +
            "W pasku górnym: pomoc (?), historia, powiadomienia i przełącznik motywu (księżyc/słońce).",
    ),
    HelpSection(
        icon = Icons.Default.Search,
        title = "Wyszukiwanie",
        description = "Wpisz nazwę nadleśnictwa (np. „Żołędnica”) lub miejscowości " +
            "(np. „Balczewo”). Wyniki pojawiają się automatycznie po wpisaniu min. 3 znaków.",
    ),
    HelpSection(
        icon = Icons.Default.MyLocation,
        title = "Znajdź najbliższe",
        description = "Po włączeniu lokalizacji lista sortuje się od najbliższego nadleśnictwa. " +
            "Wyniki przewijają się automatycznie na górę.",
    ),
    HelpSection(
        icon = Icons.Default.Star,
        title = "Ulubione",
        description = "Kliknij gwiazdkę przy nadleśnictwie, aby dodać je do ulubionych. " +
            "Ulubione wyświetlają się na górze listy.",
    ),
    HelpSection(
        icon = Icons.Default.DeviceThermostat,
        title = "Szczegóły i analiza",
        description = "Po wybraniu nadleśnictwa zobaczysz dane pogodowe (temperatura, opady, wilgotność), " +
            "skład drzewostanu oraz automatyczną ocenę szans na grzyby. " +
            "Wynik od 0% do 100% – im wyższy, tym lepsze warunki.",
    ),
    HelpSection(
        icon = Icons.Default.Forest,
        title = "Przewidywane grzyby",
        description = "Aplikacja dopasowuje gatunki grzybów do aktualnych warunków: " +
            "temperatury, wilgotności, składu drzewostanu i pory roku. " +
            "Im więcej danych, tym trafniejsza prognoza.",
    ),
    HelpSection(
        icon = Icons.Default.Map,
        title = "Mapa",
        description = "Dwa tryby: (1) ze szczegółów nadleśnictwa – pokazuje jego położenie " +
            "i Twoją lokalizację; (2) z ekranu głównego „Szukaj na mapie” – otwiera mapę " +
            "z Twoją pozycją GPS. Przytrzymaj palec w dowolnym punkcie, aby postawić znacznik, " +
            "następnie kliknij „Szukaj nadleśnictw w pobliżu” – lista wyników pokaże jako " +
            "pierwsze nadleśnictwo, na którym znajduje się znacznik, a pozostałe posortuje " +
            "według odległości.",
    ),
    HelpSection(
        icon = Icons.Default.History,
        title = "Ostatnie analizy",
        description = "Historia sprawdzonych nadleśnictw. Kliknij, aby szybko wrócić " +
            "do szczegółów. Każde nadleśnictwo pojawia się tylko raz (najnowszy wynik).",
    ),
    HelpSection(
        icon = Icons.Default.Notifications,
        title = "Powiadomienia",
        description = "Włącz powiadomienia, a aplikacja będzie automatycznie sprawdzać pogodę " +
            "w ulubionych nadleśnictwach co 12 godzin. Dostaniesz alert, gdy szanse przekroczą 70%.",
    ),
    HelpSection(
        icon = Icons.Default.Coffee,
        title = "Wsparcie twórcy",
        description = "Kliknij ikonę kawy w lewym górnym rogu, aby postawić wirtualną kawę " +
            "twórcy aplikacji. Link otwiera się w przeglądarce.",
    ),
    HelpSection(
        icon = Icons.Default.DarkMode,
        title = "Motyw (jasny/ciemny)",
        description = "Przełącznik w prawym górnym rogu ekranu głównego. Klikaj, aby cyklicznie " +
            "zmieniać: tryb ciemny → jasny → systemowy. Motyw zmienia się natychmiast.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateToHome: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = TopBarButton.copy(alpha = 0.1f),
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "NaGrzyby",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TopBarButton,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sprawdź szanse na grzybobranie",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            sections.forEach { section ->
                HelpCard(section)
            }
        }
    }
}

@Composable
private fun HelpCard(section: HelpSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = TopBarButton,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    section.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
