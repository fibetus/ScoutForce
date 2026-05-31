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

Na diagramie UML: kompozycja `Scouting Report` ◆- `Detailed Rating`. W Javie: `ScoutingReport` będzie posiadać `List<DetailedRating> detailedRatings`, a każda ocena - obowiązkowe `@ManyToOne` do raportu. Po stronie raportu zastosowane zostanie `@OneToMany(mappedBy = "scoutingReport", cascade = CascadeType.ALL, orphanRemoval = true)`. Przy zapisie raportu serwis ustawi `detailedRating.setScoutingReport(report)` na każdej pozycji (właściciel relacji w ORM). Usunięcie raportu usunie wszystkie jego oceny szczegółowe.

=== `Player` i doświadczenie zawodnicze (`PlayerExperience`)

Na diagramie: kompozycja `Player` z `UniversityExperience` lub `ProfessionalExperience` (co najwyżej jedna aktywna implementacja). W Javie zamiast jednego pola interfejsu powstaną dwa `@OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)` na encji `Player` - w danym momencie tylko jedno będzie niepuste. Mechanizm dynamicznego przełączania typu doświadczenia opisano w sekcji *Dynamiczne dziedziczenie*.

=== `Player` i `Shooting Analysis`

W modelu analitycznym powiązanie `Player` z `Shooting Analysis` (*Scoring Analysis*) było zwykłą *asocjacją*. Na diagramie projektowym zamieniono je na *kompozycję* (`Player` ◆- `Shooting Analysis`), ponieważ analiza strzelecka nie ma sensu bez zawodnika, którego dotyczy - po usunięciu `Player` nie chcemy utrzymywać osieroconych wpisów analizy jego rzutów.

W Javie: kompozycja `@OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)` na liście analiz strzeleckich zawodnika. Usunięcie zawodnika usunie powiązane wpisy analizy.

=== Uwaga: `Match Stats` i agregacje

*`Match Stats`* nie jest kompozycją w sensie raportu - to *klasa asocjacyjna* `Player`-`Match` (osobna encja, opisana w kolejnej sekcji). Sposób mapowania agregacji (np. `Delegation` $arrow$ `Match`) omówiono w sekcji *Mapowanie asocjacji i agregacji*.

#pagebreak()

== Klasa pośrednicząca `Match Stats`

#figure(
  image("../assets/ScoutForce_adr_stats.svg", width: 90%),
  caption: [Fragment zmiany klas z analitycznego na projektowy diagram klas - klasa pośrednicząca `Match Stats`],
) <fig:adr-stats>

W modelu analitycznym `Match Stats` reprezentowało asocjację wiele-do-wielu między `Player` a `Match` z atrybutami na łuku (`minutes_played`, `points`, `rebounds`, `assists`, `steals`, `blocks`, `turnovers`, `fouls`, statystyki rzutowe). W Javie zostanie to zastąpione *klasą pośredniczącą*:

- `Player` `1` - `*` `Match Stats`,
- `Match` `1` - `*` `Match Stats`.

Każda instancja będzie opisywać jeden występ zawodnika w jednym meczu. Para `(player_id, match_id)` będzie unikatowa (`@UniqueConstraint`). Atrybuty pochodne procentów skuteczności (`/fieldGoalPercentage`, `/threePointPercentage`, `/freeThrowPercentage`) zostaną zaimplementowane jako gettery `@Transient` na podstawie pól rzutów.

== Zawodnik (`Player`) - doświadczenie dynamiczne

=== Dynamiczne dziedziczenie (`<<dynamic>>`)

#figure(
  image("../assets/ScoutForce_adr_player.svg", width: 90%),
  caption: [Fragment zmiany klas z analitycznego na projektowy diagram klas - `Player`, interfejs `PlayerExperience` oraz implementacje],
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

Reguły biznesowe raportu skautingowego *pozostaną na encji* `ScoutingReport` i powiązanej `DetailedRating` (Rich Domain Model - orkiestrację przejmuje serwis, sekcja *Rich Domain Model*).

=== Kompozycja z `Detailed Rating`

Zgodnie z wymaganiem 17 ocena szczegółowa nie będzie istnieć bez raportu - mapowanie Hibernate opisano w sekcji kompozycji. Kolekcja `List<DetailedRating>` będzie zarządzana przy zapisie encji nadrzędnej.

=== Walidacje i ograniczenia obiektowe

*Suma wag (wymaganie 18, wyjątek E3):*
- `+validateDetailedRatings(): void` - suma wag `BigDecimal` musi wynosić dokładnie `1.0`; przy braku ocen - wyjątek (E2).

*Podzbiór meczów `{subset}`:*
- `+validateMatchesObservedByScout(): void` - każdy mecz z `basedOnMatches` musi należeć do `createdBy.watchedMatches`.

*Pojedyncza ocena (E4) - metoda na encji `Detailed Rating`:*
- `+validateRanges(): void` - `rating: [1, 10]`, `weight: [0.0, 1.0]`, niepuste `type` i `comment`; naruszenie kończy się `IllegalStateException` z komunikatem scenariusza E4.

`validateRanges()` zostanie wywołana w `ScoutingReportService` *przed* zbudowaniem agregatu raportu i *przed* pierwszym odwołaniem do bazy - metoda prywatna `validateInput` iteruje po liście ocen przekazanej z API i wywołuje `dr.validateRanges()` na każdej pozycji (fail fast, wyjątek E4). Metody invariantu agregatu raportu (`validateDetailedRatings`, `validateMatchesObservedByScout` na `ScoutingReport`) będą wywoływane tuż przed `save` (scenariusz `Create Scouting Report`, krok 13).

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

Dostęp do zbiorów instancji (zamiast ekstensji w encjach, zgodnie z sekcją *Mapowanie asocjacji i agregacji*) zapewnią interfejsy Spring Data JPA, np.:
- `PlayerRepository` - `findAll()`, `findByPlayerStatus`, `findByPosition`, `findByClubId`,
- `ScoutRepository` - `findByEmail`, `findByLicenseNumber`,
- `ScoutingReportRepository` - `findByCreatedById`, `findByPlayerId`,
- dalsze (`ClubRepository`, `DelegationRepository` …) według przypadków użycia.

Zapis i usuwanie: `repository.save(entity)`, `repository.delete(entity)`; kaskady kompozycji obsłuży Hibernate.

=== Serwisy przypadków użycia

Operacje przypisane na diagramie analitycznym do klas biznesowych zostaną zrealizowane w serwisach aplikacyjnych (uzasadnienie braku tych metod na diagramie projektowym - sekcja *Rich Domain Model*). W pierwszej implementacji powstaną m.in.:
- `ViewPlayersListService` - *View Players List*,
- `ViewPlayerMatchesService` - *View Player's Matches*,
- `ScoutingReportService` - *Create Scouting Report* wraz z dołączaniem ocen szczegółowych (*Add Detailed Ratings*),
- dalsze serwisy odczytu i mutacji według pozostałych przypadków użycia (*View Scoring Analysis*, *View Player's Scouting Reports*, *View Delegations*, *Create a New Delegation*, *View Scouts*, *View Big Board Ranking*).

Kontrolery REST będą delegowały wywołania do odpowiednich serwisów.

=== Transakcje i komunikaty błędów

`ScoutingReportService` zostanie objęty `@Transactional`; metody mutujące - `@Transactional(rollbackFor = Exception.class)`. Każdy błąd walidacji lub brak encji przerwie transakcję - częściowy raport nie trafi do bazy. `GlobalExceptionHandler` zmapuje wyjątki na odpowiedzi HTTP (m.in. `422` dla reguł biznesowych E1-E5).

== Rich Domain Model - serwisy, reguły na encji

Zostanie przyjęty *Rich Domain Model*: *walidacja i wyliczenia* pozostają na encjach domenowych, natomiast *orkiestracja* przypadków użycia - w serwisach aplikacyjnych Spring.

=== Przeniesienie operacji z modelu analitycznego do serwisów

Na diagramie analitycznym wiele operacji przypadków użycia było przypisanych bezpośrednio do klas biznesowych (fabrykujące metody, operacje odczytu list). W implementacji z Spring Data JPA zostaną one przeniesione do serwisów odpowiadających przypadkom użycia (lista serwisów i przypadków - sekcja *Serwisy przypadków użycia*).

Diagram projektowy obejmuje wyłącznie warstwę domenową (encje, asocjacje, kompozycje, atrybuty pochodne). *Serwisy i repozytoria nie są na nim umieszczane* - należą do warstwy aplikacji Spring, a nie do modelu obiektowego widocznego w UML. Gdyby zachować metody analityczne typu `+createReport(...)` czy `+viewPlayersList(...)` na diagramie projektowym, sugerowałyby one logikę należącą do encji, podczas gdy w kodzie realizuje ją serwis współpracujący z repozytoriami. Metody te *znikają więc z diagramu projektowego*.

Na encjach *pozostają* operacje wyrażające reguły wewnątrz agregatu: walidacje raportu (`validateDetailedRatings`, `validateMatchesObservedByScout`), walidacja pojedynczej oceny (`DetailedRating.validateRanges()`), gettery pochodne (`/finalRating`, `/averageRating`, `/cost`, `/percentage`), `changeStatus`, `becomeUniversityPlayer` / `becomeProfessionalPlayer` itd.

=== `Create Scouting Report` i dołączanie ocen szczegółowych

Wyróżniony przypadek użycia *Create Scouting Report* (wraz z `<<include>>` *Add Detailed Ratings* oraz opcjonalnym `<<extend>>` *View Player's Matches*) zostanie zaimplementowany w odpowiednich serwisach. Rolę analitycznej metody `createReport(...)` przejmie `ScoutingReportService.createScoutingReport(...)` (oraz wariant A1: `createScoutingReportFromSelectedMatches(...)`).

*W serwisie (nie na diagramie domenowym):*
- wstępna walidacja wejścia (E5, E2, E4): sprawdzenie notatki i rekomendacji, niepustej listy ocen oraz - dla każdej `DetailedRating` - wywołanie `validateRanges()` *zanim* nastąpi odwołanie do repozytorium,
- pobranie agregatów `Scout` i `Player` z repozytoriów,
- ustalenie zbioru `basedOnMatches` - domyślnie przez `ViewPlayerMatchesService` (przecięcie meczów zawodnika z meczami obserwowanymi przez skauta), w przepływie A1 - z ręcznie wybranych identyfikatorów meczów,
- złożenie agregatu raportu: `new ScoutingReport()` (konstruktor bezargumentowy JPA), settery (`setCreatedBy`, `setPlayer`, `setNote`, `setRecommendation`, `setBasedOnMatches`, …),
- *dołączenie ocen szczegółowych* (*Add Detailed Ratings*): dla każdej przekazanej `DetailedRating` ustawienie strony właścicielskiej kompozycji (`detailedRating.setScoutingReport(report)`) i dodanie do kolekcji `detailedRatings` - faktyczne utworzenie obiektów ocen następuje wyłącznie w ramach tworzenia raportu, zgodnie z kompozycją Scouting Report ◆- Detailed Rating,
- wywołanie metod domenowych raportu (`validateDetailedRatings`, `validateMatchesObservedByScout`),
- `scoutingReportRepository.save(report)` - Hibernate zapisze powiązane oceny szczegółowe.

Podział wynika m.in. z wymogu `@NoArgsConstructor` w JPA oraz z reguł obejmujących wiele agregatów naraz (np. walidacja podzbioru meczów względem `scout.watchedMatches`). Moment wywołania poszczególnych walidacji E1-E5 opisano w sekcji *Walidacje i ograniczenia obiektowe*.

== Uwierzytelnianie - uproszczenie na potrzeby prezentacji

W systemie docelowym powstaną *logowanie i rejestracja* oraz powiązanie sesji ze `Scout` / `Director`. Na etapie pierwszej implementacji i demonstracji *Create Scouting Report* warstwa bezpieczeństwa zostanie pominięta:

- w ścieżce API pozostanie parametr `scoutId`, lecz frontend i testy będą używać *stałego identyfikatora* skauta testowego (obiekt zasilany przy starcie aplikacji),
- nie powstaną JWT, Spring Security ani endpointy `/login` / `/register`.