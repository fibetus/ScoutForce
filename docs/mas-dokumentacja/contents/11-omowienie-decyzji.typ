= Omówienie decyzji projektowych i skutków analizy dynamicznej

Niniejszy rozdział opisuje *planowane* decyzje implementacyjne wynikające z analizy dynamicznej - formułowany jest w czasie przyszłym jako dokumentacja projektowa poprzedzająca (lub równoległa względem) fazę kodowania systemu.

== Wykorzystane technologie

- *Backend:* Spring (Spring Boot + Spring Data JPA) z Hibernate jako ORM.
- *Frontend:* React (SPA komunikujące się z backendem przez REST).
- *Baza danych:* H2.
- *Modelowanie UML:* Lucidchart (diagramy przypadków użycia, klas, stanu, aktywności).
- *Projekt interfejsu (mockupy GUI):* Figma.

== Ograniczenia atrybutów - enumy

W wyniku analizy dynamicznej wszystkie atrybuty o ograniczonej dziedzinie wartości zostaną zamienione na *enumy Javy*. Zgodnie z konwencją nazewnictwa (rozdz. 1) stałe enumów będą zapisywane w UPPERCASE.

*`PlayerStatus`* (atrybut `player_status` klasy `Player`): `NEW`, `OBSERVED`, `MEDICAL_VERIFICATION`, `INVITED_TO_WORKOUT`, `INVITED_TO_BIG_BOARD`, `DELISTED`.

*`PositionType`* (skrót i numer pozycji 1-5): `POINT_GUARD("PG", 1)` … `CENTER("C", 5)`.

*`ClassType`* (`UniversityExperience`): `FRESHMAN`, `SOPHOMORE`, `JUNIOR`, `SENIOR`.

*`RecommendationType`* (`Scouting Report`): `STRONG_BUY`, `BUY`, `NEUTRAL`, `PASS`.

*`DelegationStatus`* (`Delegation`): `PLANNED`, `APPROVED`, `IN_PROGRESS`, `FINISHED`, `CANCELLED`.

W mapowaniu Hibernate wszystkie powyższe enumy będą oznaczane `@Enumerated(EnumType.STRING)` - w H2 zapisana zostanie nazwa stałej (np. `"INVITED_TO_BIG_BOARD"`), a nie indeks porządkowy.

== Mapowanie asocjacji i agregacji

Wszystkie asocjacje z diagramu projektowego będą realizowane adnotacjami JPA/Hibernate:

- *1 - \** $arrow$ `@OneToMany` (właściciel kolekcji) oraz `@ManyToOne` (element), np. `Scout` $arrow$ `Scouting Report`, `Delegation` $arrow$ `Match`.
- *\* - \** $arrow$ `@ManyToMany`, np. `Scout` $arrow$ `Match` (mecze obserwowane).
- *Agregacja* - ten sam mechanizm co asocjacja; różnica pozostanie semantyką modelu UML, nie osobną adnotacją Hibernate.

*Nawigacja* - w encjach pojawią się pola `List` / referencje z adnotacjami JPA (np. `detailedRatings` w `ScoutingReport`, `matchStats` w `Player`). Logika przypadków użycia będzie w miarę możliwości korzystać z nawigacji po tych polach zamiast z rozproszonych zapytań SQL.

*Ekstensja klas* - na diagramie projektowym może być oznaczona metodami klasowymi; w implementacji ze Spring + Hibernate *nie będzie* utrzymywana jako `static Set<T>` w encjach. Stan trafi do bazy H2, a dostęp do zbiorów instancji zapewnią repozytoria oraz ładowane asocjacje (patrz sekcja o warstwie Spring).

== Implementacja kompozycji

Kompozycje z diagramu projektowego zostaną zmapowane wspólnym wzorcem: właściciel cyklu życia, `cascade = CascadeType.ALL`, `orphanRemoval = true` tam, gdzie element nie będzie miał sensu bez rodzica.

=== `Scouting Report` i `Detailed Rating`

Na diagramie UML: kompozycja `Scouting Report` ◆─ `Detailed Rating`. W Javie: `ScoutingReport` będzie posiadać `List<DetailedRating> detailedRatings`, a każda ocena - obowiązkowe `@ManyToOne` do raportu. Po stronie raportu zastosowane zostanie `@OneToMany(mappedBy = "scoutingReport", cascade = CascadeType.ALL, orphanRemoval = true)`. Przy zapisie raportu serwis ustawi `detailedRating.setScoutingReport(report)` na każdej pozycji (właściciel relacji w ORM). Usunięcie raportu usunie wszystkie jego oceny szczegółowe.

=== `Player` i doświadczenie zawodnicze (`PlayerExperience`)

Na diagramie: kompozycja `Player` z `UniversityExperience` lub `ProfessionalExperience` (co najwyżej jedna aktywna implementacja). W Javie zamiast jednego pola interfejsu powstaną dwa `@OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)` na encji `Player` - w danym momencie tylko jedno będzie niepuste. Przełączanie typu doświadczenia zapewnią metody `becomeUniversityPlayer` i `becomeProfessionalPlayer`.

=== `Player` i `ShootingAnalysis`

Kompozycja `Player` ◆─ `ShootingAnalysis`: `@OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)` na liście analiz strzeleckich zawodnika. Usunięcie zawodnika usunie powiązane wpisy analizy.

=== Uwaga: `Match Stats` i agregacje

*`Match Stats`* nie jest kompozycją w sensie raportu - to *klasa asocjacyjna* `Player`-`Match` (osobna encja, patrz kolejna sekcja). *Agregacje* (np. `Delegation` $arrow$ `Match`) będą mapowane jak zwykłe `@OneToMany` / `@ManyToOne` - Hibernate nie rozróżni ich od zwykłej asocjacji; różnica pozostanie na diagramie UML.

#pagebreak()

== Klasa pośrednicząca `Match Stats`

#figure(
  image("../assets/ScoutForce_adr_stats.svg", width: 90%),
  caption: [Fragment projektowego diagramu klas - klasa pośrednicząca `Match Stats` oraz powiązany `Scouting Report` z kompozycją `Detailed Rating`],
) <fig:adr-stats>

W modelu analitycznym `Match Stats` reprezentowało asocjację wiele-do-wielu między `Player` a `Match` z atrybutami na łuku (`minutes_played`, `points`, `rebounds`, `assists`, `steals`, `blocks`, `turnovers`, `fouls`, statystyki rzutowe). W Javie zostanie to zastąpione *klasą pośredniczącą*:

- `Player` `1` - `*` `Match Stats`,
- `Match` `1` - `*` `Match Stats`.

Każda instancja będzie opisywać jeden występ zawodnika w jednym meczu. Para `(player_id, match_id)` będzie unikatowa (`@UniqueConstraint`). Atrybuty pochodne procentów skuteczności (`/fieldGoalPercentage`, `/threePointPercentage`, `/freeThrowPercentage`) zostaną zaimplementowane jako gettery `@Transient` na podstawie pól rzutów.

== Zawodnik (`Player`) - doświadczenie dynamiczne

=== Dynamiczne dziedziczenie (`<<dynamic>>`)

#figure(
  image("../assets/ScoutForce_adr_player.svg", width: 90%),
  caption: [Fragment projektowego diagramu klas - `Player`, interfejs `PlayerExperience` oraz implementacje],
) <fig:adr-player>

Z wymagania nr 9 (rozdz. 2.3) wynika podział zawodników na *uniwersyteckich* i *profesjonalnych* - kompletny, rozłączny, lecz *dynamiczny* (zawodnik może zmienić typ bez utraty tożsamości obiektu). Statyczne podklasy `Player` w Javie tego nie obsłużą.

Zostanie zastosowany wzorzec Strategy / State:

- interfejs `PlayerExperience` jako kompozycja `Player` (szczegóły mapowania - sekcja kompozycji powyżej),
- implementacje `UniversityExperience` (`university`, `classType`) oraz `ProfessionalExperience` (`countryOfOrigin`),
- przepinanie przez `+becomeUniversityPlayer(...)`, `+becomeProfessionalPlayer(...)`.

Jeden obiekt `Player` zachowa powiązania z `Scouting Report`, `Match Stats` itd., zmieniając wyłącznie strategię opisu doświadczenia.

=== Status zawodnika - uproszczenie względem diagramu stanu

Diagram stanu (rozdz. 9) definiuje dozwolone przejścia `player_status`. Pełna implementacja wymagałaby osobnych operacji z `{guard}` dla każdego przejścia.

Wyróżniony przypadek użycia *Create Scouting Report* nie wymaga w scenariuszu głównym zarządzania maszyną stanów (status będzie tylko wyświetlany), dlatego zamiast wielu metod z diagramu stanu powstanie jedna operacja:

- `+changeStatus(playerStatus: PlayerStatus): void` - bez walidacji przejść.

Pełna maszyna stanów pozostanie dokumentem analitycznym pod przyszłą funkcjonalność aktualizacji statusu.

== Klasa `Scouting Report` - logika domenowa na encji

Reguły biznesowe raportu skautingowego *pozostaną na encji* `ScoutingReport` i powiązanej `DetailedRating` (Rich Domain Model - orkiestracja w serwisie, patrz dalsza część rozdziału). Na diagramie projektowym *nie pojawi się* fabrykująca metoda `createReport(...)` z modelu analitycznego na rzecz przeniesienia jej do serwisu.

=== Kompozycja z `Detailed Rating`

Zgodnie z wymaganiem 17 ocena szczegółowa nie będzie istnieć bez raportu - mapowanie Hibernate opisano w sekcji kompozycji. Kolekcja `List<DetailedRating>` będzie zarządzana przy zapisie encji nadrzędnej.

=== Walidacje i ograniczenia obiektowe

*Suma wag (wymaganie 18, wyjątek E3):*
- `+validateDetailedRatings(): void` - suma wag `BigDecimal` musi wynosić dokładnie `1.0`; przy braku ocen - wyjątek (E2).

*Podzbiór meczów `{subset}`:*
- `+validateMatchesObservedByScout(): void` - każdy mecz z `basedOnMatches` musi należeć do `createdBy.watchedMatches`.

*Pojedyncza ocena (E4):*
- `+validateRanges(): void` - `rating ∈ [1, 10]`, `weight ∈ [0.0, 1.0]`, niepuste `type` i `comment`.

Metody agregatu raportu będą wywoływane w serwisie aplikacyjnym tuż przed `save` (scenariusz `Create Scouting Report`, krok 13).

=== Atrybut pochodny `/finalRating`

`+getFinalRating(): BigDecimal` - średnia ważona `rating × weight` po ocenach szczegółowych; `@Transient`, bez kolumny w bazie.

== Pozostałe atrybuty pochodne i ograniczenia `{unique}`

*Gettery `@Transient` (prefiks `/` na diagramie):*
- `Player.getAverageRating()` - średnia `/finalRating` raportów zawodnika; przy braku raportów zwróci `0` (sortowanie listy).
- `Delegation.getCost()` - wzór z wymagania 21; atrybuty klasowe `DAILY_LIVING_COST = 250`, `FLIGHT_COST = 1500` na encji `Delegation`.
- `ShootingAnalysis.getPercentage()` - procent trafień w sezonie i strefie (wymaganie 14).

*Unikalność pojedynczych atrybutów:* `email` (`Person`), `license_number` (`Scout`) - `@Column(unique = true)`; naruszenie da `DataIntegrityViolationException`, obsłużone w warstwie aplikacji.

*Unikalność analizy strzeleckiej per zawodnik, sezon i strefa* - para `(season, range)` co najwyżej raz na zawodnika (ta sama strefa może wrócić w innym sezonie):

```java
@Table(name = "shooting_analysis",
       uniqueConstraints = @UniqueConstraint(
           name = "uc_player_season_range",
           columnNames = {"player_id", "season", "range_label"}))
```

== Ograniczenie dwóch drużyn w `Match`

Wymaganie 12: mecz ma dokładnie dwie różne drużyny (`host`, `guest`). Powstanie metoda `+ validateBothTeams(): void` (wywołana przy tworzeniu / zapisie meczu), sprawdzająca m.in. `host != null`, `guest != null`, `host.id != guest.id`. Niespełnienie warunku przerwie operację wyjątkiem domenowym.

#pagebreak()

== Warstwa aplikacji - Spring, repozytoria i serwisy

=== Repozytoria

Zamiast ekstensji w postaci `static Set` w encjach powstaną interfejsy Spring Data JPA, np.:
- `PlayerRepository` - `findAll()`, `findByPlayerStatus`, `findByPosition`, `findByClubId`,
- `ScoutRepository` - `findByEmail`, `findByLicenseNumber`,
- `ScoutingReportRepository` - `findByCreatedById`, `findByPlayerId`,
- dalsze (`ClubRepository`, `DelegationRepository` …) według przypadków użycia.

Zapis i usuwanie: `repository.save(entity)`, `repository.delete(entity)`; kaskady kompozycji obsłuży Hibernate.

=== Serwisy przypadków użycia

Dla wyróżnionego scenariusza *Create Scouting Report* (oraz `<<include>>` / `<<extend>>`) powstaną m.in.:
- `ViewPlayersListService` - lista zawodników obserwowanych przez skauta,
- `ViewPlayerMatchesService` - przecięcie meczów zawodnika i meczów obserwowanych przez skauta,
- `ScoutingReportService` - utworzenie raportu (przepływ domyślny i A1 z wybranymi meczami).

Kontroler REST (`ScoutingReportController`) będzie delegował wywołania do tych serwisów.

=== Transakcje i komunikaty błędów

`ScoutingReportService` zostanie objęty `@Transactional`; metody mutujące - `@Transactional(rollbackFor = Exception.class)`. Każdy błąd walidacji lub brak encji przerwie transakcję - częściowy raport nie trafi do bazy. `GlobalExceptionHandler` zmapuje wyjątki na odpowiedzi HTTP (m.in. `422` dla reguł biznesowych E1-E5).

== Rich Domain Model - `createReport` w serwisie, reguły na encji

Zostanie przyjęty *Rich Domain Model*: inwarianty i wyliczenia na `ScoutingReport` / `DetailedRating`, *orkiestracja* utworzenia raportu w `ScoutingReportService`.

*W serwisie (nie na diagramie domenowym):*
- złożenie agregatu: `new ScoutingReport()` (konstruktor bezargumentowy JPA), settery (`setCreatedBy`, `setPlayer`, `setNote`, …), powiązanie ocen z raportem,
- wywołanie metod domenowych raportu,
- `scoutingReportRepository.save(report)`.

Podział wynika z wymogu `@NoArgsConstructor` w JPA oraz z reguł obejmujących wiele agregatów naraz (np. przecięcie zbiorów meczów). Fabryka `createReport` z diagramu analitycznego *nie trafi* na diagram projektowy - jej rolę przejmie `ScoutingReportService.createScoutingReport(...)`.

== Uwierzytelnianie - uproszczenie na potrzeby prezentacji

W systemie docelowym powstaną *logowanie i rejestracja* oraz powiązanie sesji ze `Scout` / `Director`. Na etapie pierwszej implementacji i demonstracji *Create Scouting Report* warstwa bezpieczeństwa zostanie pominięta:

- w ścieżce API pozostanie parametr `scoutId`, lecz frontend i testy będą używać *stałego identyfikatora* skauta testowego (obiekt zasilany przy starcie aplikacji),
- nie powstaną JWT, Spring Security ani endpointy `/login` / `/register`.