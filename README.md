# Na grzyby

Aplikacja Android do oceny szans na grzybobranie w wybranym nadleśnictwie.

## Funkcje

- Wyszukiwanie nadleśnictw (cache + **BDL**) oraz **po miejscowości** (Nominatim + BDL, np. „Balczewo” → Gniewkowo)
- Sortowanie po GPS z danymi BDL w okolicy
- **Open-Meteo** — pogoda (cache 30 min)
- **Ulubione** nadleśnictwa
- **Mapa OpenStreetMap** (osmdroid) — nadleśnictwo i Twoja lokalizacja
- **Powiadomienia** — alert gdy w ulubionym szansa ≥ 70% (WorkManager, ~12 h)
- **Historia analiz** na ekranie szczegółów
- Algorytm v1 (pogoda + sezon + drzewostan)
- Udostępnianie werdyktu
- **Ostatnie analizy** na ekranie głównym
- Mapa z lokalizacją użytkownika (GPS)
- Leśnictwa z BDL w szczegółach nadleśnictwa

## Źródła danych

| Dane | Źródło |
|------|--------|
| Nadleśnictwa | [OGC API BDL](https://ogcapi.bdl.lasy.gov.pl) |
| Pogoda | [Open-Meteo](https://open-meteo.com) |
| Mapa | [OpenStreetMap](https://www.openstreetmap.org) |
| Drzewostan | Mock (wydzielenia BDL — plan) |

## Log błędów

`Download/NaGrzyby/nagrzyby_errors.log`

## Uruchomienie

Android Studio → Sync Gradle → uruchom na urządzeniu z internetem.

**Powiadomienia:** ikona dzwonka na liście głównej → włącz → dodaj ulubione nadleśnictwa.

## Kolejny etap

- Widget na ekran główny
- Eksport GPX / nawigacja piesza
- Wykres pogody 4-dniowej
"# na-grzyby" 
