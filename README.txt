wygenerowane z groka

1. Ogólny opis aplikacji

Temat: Aplikacja do śledzenia wagi ciała (weight tracking).
Użytkownik może dodawać pomiary wagi (z jednostkami KG/LB), przeglądać historię, ustawiać cele (target weight),
generować wykresy z moving averages, edytować profil (wiek, wzrost, płeć, zdjęcie), importować/eksportować dane CSV,
zmieniać język (PL/EN) i resetować dane.

Technologie:
UI: Jetpack Compose (cały interfejs, bez XML).
Architektura: MVVM z podziałem na layers (domain, data, feature).
Dependency Injection: Hilt.
Baza danych: Room (dla pomiarów wagi i profilu użytkownika).
Asynchroniczność: Kotlin Coroutines + Flow (obserwacja danych, loading states).
Wykresy: MPAndroidChart (integracja z Compose via AndroidView) + custom renderer do bitmap (dla obrazków wykresów).
Inne: DataStore dla ustawień, CSV parsing/import/export, zdjęcia profilowe (galeria/aparat), lokalizacja (PL/EN).


2. Struktura projektu
Projekt jest modularny i czysty, z podziałem na pakiety:

app: Główna nawigacja (NavHost, BottomNav, NavigationRail dla landscape), root composable (AppRoot).
core: Wspólne utils (daty, exceptions, UI events, network – choć network nie jest używany), domain errors, results.
data: Repozytoria (WeightMeasureRepository, UserProfileRepository), mappers (entity <-> domain), lokalne (Room DAO), preferences (DataStore), CSV import/export, backup (pliki lokalne).
di: Hilt moduły (AppModule).
domain: Modele (WeightMeasure, UserProfile), repo interfejsy, use cases (GenerateWeightChartDataUseCase), enums (WeightUnit, Gender).
feature: Moduły ekranów:
addedit: Dialogi dodawania/edycji pomiarów (waga, data, jednostka).
history: Lista historii pomiarów z edycją/usuwaniem.
home: Ekran główny z wykresem (embedded lub image), legendą, moving averages.
settings: Profil (zdjęcie, dane), język, embedded chart toggle, CSV, reset danych.

ui/theme: Material3 theme (light/dark, dynamic colors na Android 12+).

Plusy struktury:

Dobry separation of concerns (domain czysto logiczny, data z bazą, feature z UI).
Użycie Flow/StateFlow do reaktywnego UI (np. observeWeightMeasureHistory).
Hilt wszędzie – łatwe testowanie/iniekcja.

Minusy:

Brak testów jednostkowych/integracyjnych (np. dla repo/use cases).
Niektóre utils w core mogłyby być w oddzielnym module (np. chart utils).
Brak error handling w niektórych miejscach (np. brak fallback dla błędów CSV poza exception).

3. Kluczowe funkcjonalności

Pomiary wagi: Dodawanie/edycja z dialogami (data picker, jednostka toggle). Historia z listą, usuwaniem (confirm dialog).
Wykresy: Generowanie danych (GenerateWeightChartDataUseCase – moving averages, target line). Renderowanie: embedded (AndroidView z LineChart) lub bitmap (MpChartBitmapRenderer + export do pliku). Obsługuje landscape/portrait (różne layouty).
Profil: Edycja danych (wiek, wzrost, cel wagi, płeć, zdjęcie z galerii/aparatu). Zdjęcia zapisywane w filesDir/cacheDir.
Ustawienia: Zmiana języka (AppCompatDelegate), toggle embedded chart, CSV import/export (custom parser), reset danych (z backup/restore).
Backup: Automatyczny backup historii do CSV + foto. Restore jeśli baza pusta.
Nawigacja: NavHost z BottomNav/Rail, badges (fałszywe, ale z repo).
Lokalizacja: PL/EN (strings.xml), dynamiczna zmiana.

4. Wydajność i optymalizacje

Plusy:
Użycie Flow do obserwacji DB – reaktywne, bez pollingu.
LazyColumn w historii – dobre dla długich list.
Bitmapy wykresów cache'owane w pliku – unika re-renderingu.
Coroutines z launchSafely (obsługa błędów).
BigDecimal dla wagi – precyzja.

Potencjalne problemy:
Generowanie bitmap wykresów: Dla długich historii (dużo punktów) może być ciężkie (duży bitmap w pamięci). Sugestia: Limituj dane na wykresie lub optymalizuj renderer (np. downsampling).
Zdjęcia: Ładowanie bitmap w UI thread? Nie, ale w viewModelScope – OK, ale dla dużych foto może blokować (użyj Coil/Glide dla async loading).
CSV: Parsing/import w IO, ale dla dużych plików (tysiące linii) może trwać długo – brak cancelowania.
Recomposition: W HomeScreen – dużo update'ów state (chartData, bitmap) – może powodować redraw. Użyj derivedStateOf dla obliczeń.
Brak paging dla historii (jeśli dużo danych).

Sugestie:
Dodaj Paging3 dla historii (jeśli dane rosną).
Dla wykresów: Użyj Canvas w Compose zamiast MPChart (lepsza integracja, lżejsze).
Wydajność: Profiluj z Profilerem (memory leaks w bitmapach?).
Backup: Dodaj cloud backup (Google Drive).


5. Potencjalne błędy/bugi

Błędy w kodzie:
W WeightMeasureComponent: Brak obsługi overflow/underflow w pickerze (np. int > 200 lub <1 – crash? Nie, ale UI może być niestabilne).
CSV: Parser prosty, ale nie obsługuje escaped commas/quotes – może failować na złożonych plikach.
EditMeasureViewModel: Brak walidacji wagi (np. ujemna waga – zapisuje).
ProfileViewModel: Brak czyszczenia tmp plików po anulowaniu (leak cache?).
HomeViewModel: W generateChartAsImage – brak obsługi błędów renderera (np. OOM dla dużego chartu).
Brak obsługi rotacji (choć Compose radzi sobie, ale bitmapy mogą się reloadować niepotrzebnie).

Bezpieczeństwo:
Dane w Room – nie szyfrowane (jeśli sensitive, dodaj encryption).
Zdjęcia/CSV w filesDir – OK, ale brak permisji (np. na export – używasz SAF, dobrze).
Brak crashlytics (np. Firebase) – błędy nie raportowane.

Inne:
Deprecated: Toast w showErrorToast – lepiej Snackbar.
Brak accessibility (np. contentDescription w ikonach wykresów).
Tłumaczenia: Tylko PL/EN – brak fallback dla innych języków.


6. Sugestie ulepszeń

UI/UX: Dodaj animacje (np. w pickerze wagi), dark mode tweaks (wykresy mogą być nieczytelne).
Funkcje: Trendy (np. predykcja wagi), notyfikacje (przypomnienia o pomiarze), integracja z Google Fit.
Kod: Dodaj sealed classes dla state/actions wszędzie (jak w niektórych VM). Użyj Koin zamiast Hilt jeśli lżejsze.
Testy: Unit testy dla use cases (GenerateWeightChartDataUseCase), UI testy dla Compose (Screenshot tests).
Build: Dodaj Proguard/R8 dla minifikacji, multi-module dla skalowalności.
