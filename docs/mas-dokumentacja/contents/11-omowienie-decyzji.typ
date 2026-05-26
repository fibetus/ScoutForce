= Omówienie decyzji projektowych i skutków analizy dynamicznej

== Wykorzystane technologie

- *Backend:* Spring (Spring Boot + Spring Data JPA) z Hibernate jako dostawcą ORM.
- *Frontend:* React (SPA komunikujące się z backendem przez REST).
- *Baza danych:* H2 (wbudowana, prosta w konfiguracji do celów dydaktycznych; docelowo wymienna na dowolną relacyjną bazę zgodną z JDBC).
- *Modelowanie UML:* Lucidchart (diagramy przypadków użycia, klas, stanu, aktywności).
- *Projekt interfejsu (mockupy GUI):* Figma.

== Klasa `Player` - dziedziczenie dynamiczne i ograniczenia atrybutów

#figure(
  image("../assets/ScoutForce_adr_player.svg", width: 90%),
  caption: [Fragment projektowego diagramu klas - klasa `Player` wraz z interfejsem `PlayerExperience` oraz jego implementacjami],
) <fig:adr-player>

=== Dynamiczne dziedziczenie zawodnika (`<<dynamic>>`)

Z analizy wymagania nr 9 (rozdz. 2.3) wynika, że podział zawodników na *uniwersyteckich* (`UniversityExperience`) i *profesjonalnych* (`ProfessionalExperience`) jest *kompletny, rozłączny, ale dynamiczny* - zawodnik kończąc college przestaje być zawodnikiem uniwersyteckim i staje się profesjonalnym (lub odwrotnie, przy powrocie do NCAA). Klasyczne dziedziczenie statyczne w Javie tego nie obsłuży - obiekt nie zmienia swojej klasy w trakcie życia.

W projektowym diagramie klas zastosowano wzorzec State / Strategy:

- Utworzono interfejs `PlayerExperience`, który jest *kompozycją* klasy `Player` - referencja do obiektu implementującego interfejs jest przekazywana w konstruktorze klasy `Player`, a klasa `Player` zarządza jej cyklem życia.
- Powstały dwie konkretne klasy implementujące interfejs: `UniversityExperience` (atrybuty: `university`, `class`) oraz `ProfessionalExperience` (atrybut: `countryOfOrigin`).
- Przepinanie aktualnej implementacji odbywa się przez metody klasy `Player`:
  - `+ becomeUniversityPlayer(university: String, classType: ClassType): void`
  - `+ becomeProfessionalPlayer(countryOfOrigin: String): void`

Dzięki temu jeden obiekt `Player` zachowuje swoją tożsamość (i powiązania z `Scouting Report`, `Match Stats` itd.), a zmienia jedynie strategię opisującą jego doświadczenie zawodnicze.

== Ograniczenia atrybutów typu wyliczeniowego - enumy

W wyniku analizy dynamicznej wszystkie atrybuty o ograniczonej dziedzinie wartości zostały zamienione na *enumy Javy*. Zgodnie z konwencją nazewnictwa (rozdz. 1) stałe enumów są zapisywane w UPPERCASE.

*`PlayerStatus`* - status zawodnika w procesie skautingowym (atrybut `player_status` klasy `Player`):
- `NEW`
- `OBSERVED`
- `MEDICAL_VERIFICATION`
- `INVITED_TO_WORKOUT`
- `INVITED_TO_BIGBOARD`
- `DELISTED`

*`PositionType`* - pozycja zawodnika na boisku. Atrybut `position` (wymaganie 8) został w analizie dynamicznej dodatkowo ograniczony do skończonego zbioru wartości; każda pozycja ma przypisane wartości pomocnicze (skrót oraz numer pozycji 1-5), aby ułatwić skautom poruszanie się po systemie:
- `POINT_GUARD("PG", 1)`
- `SHOOTING_GUARD("SG", 2)`
- `SMALL_FORWARD("SF", 3)`
- `POWER_FORWARD("PF", 4)`
- `CENTER("C", 5)`

*`ClassType`* - rocznik zawodnika uniwersyteckiego. Atrybut `class` klasy `UniversityExperience` (wymaganie 10) został zamieniony na enum:
- `FRESHMAN`
- `SOPHOMORE`
- `JUNIOR`
- `SENIOR`

*`RecommendationType`* - rekomendacja draftowa raportu skautingowego. Atrybut `recommendation` klasy `Scouting Report` (wymaganie 16):
- `STRONG_BUY`
- `BUY`
- `NEUTRAL`
- `PASS`

*`DelegationStatus`* - status delegacji. Atrybut `status` klasy `Delegation` (wymaganie 20):
- `PLANNED`
- `APPROVED`
- `IN_PROGRESS`
- `FINISHED`
- `CANCELLED`

=== Sposób przechowywania enumów w bazie

W mapowaniu Hibernate wszystkie powyższe enumy są oznaczane adnotacją `@Enumerated(EnumType.STRING)` - w bazie H2 są przechowywane jako napis odpowiadający nazwie stałej (np. `"INVITED_TO_BIG_BOARD"`), a nie jako liczba porządkowa. Dzięki temu dodanie nowej wartości w przyszłości nie powoduje przesunięcia indeksów i nie psuje danych historycznych.

== Mapowanie asocjacji i agregacji w Hibernate

Wszystkie asocjacje z diagramu klas są realizowane za pomocą adnotacji JPA/Hibernate odpowiadających ich licznościom:

- *1 - \** $arrow$ `@OneToMany` po stronie właściciela kolekcji oraz `@ManyToOne` po stronie elementów (np. `Delegation` $arrow$ `Match`, `Scout` $arrow$ `Scouting Report`).
- *\* - \** $arrow$ `@ManyToMany` (np. powiązanie meczów obserwowanych przez wielu skautów w ramach różnych delegacji).
- *Agregacja* jest implementowana w taki sam sposób jak zwykła asocjacja - z punktu widzenia Hibernate nie ma osobnej adnotacji dla agregacji; różnica między asocjacją a agregacją pozostaje informacją semantyczną na poziomie modelowania.

=== Ekstensja klas a persystencja (decyzja implementacyjna)

Na diagramie projektowym ekstensja klasy może być oznaczona metodami klasowymi. W implementacji ze Spring + Hibernate *nie będzie* utrzymywana jako `private static Set<T>` w encjach - stan trafi do bazy H2, a dostęp do zbiorów instancji oraz powiązań między obiektami będzie realizowany przez repozytoria i asocjacje JPA (patrz sekcja na końcu rozdziału).

#pagebreak()

== Klasa `Scouting Report` - kompozycja, klasa pośrednicząca i atrybuty pochodne

#figure(
  image("../assets/ScoutForce_adr_stats.svg", width: 90%),
  caption: [Fragment projektowego diagramu klas - klasa `Scouting Report` wraz z `Detailed Rating` (kompozycja) oraz klasą pośredniczącą `Match Stats`],
) <fig:adr-stats>

=== Klasa pośrednicząca `Match Stats`

W modelu analitycznym `Match Stats` reprezentowało asocjację wiele-do-wielu między `Player` a `Match` z dodatkowymi atrybutami (`minutes_played`, `points`, `rebounds`, `assists`, `steals`, `blocks`, `turnovers`, `fouls`, statystyki rzutowe). W projekcie taka konstrukcja nie istnieje wprost w Javie - została więc zastąpiona *klasą pośredniczącą* powiązaną z `Player` i `Match` dwiema asocjacjami *jeden-do-wielu*:

- `Player` `1` - `*` `Match Stats`,
- `Match` `1` - `*` `Match Stats`.

Każda instancja `Match Stats` reprezentuje pojedynczy występ konkretnego zawodnika w konkretnym meczu i nosi wszystkie atrybuty wcześniej leżące „na asocjacji".

== Kompozycja `Scouting Report` - `Detailed Rating`

Zgodnie z wymaganiem 17, ocena szczegółowa (`Detailed Rating`) nie istnieje bez raportu, który ją zawiera - usunięcie raportu skutkuje automatycznym usunięciem wszystkich ocen szczegółowych. Kompozycja zostanie zrealizowana w Hibernate przez:

- `@OneToMany(mappedBy = "scoutingReport", cascade = CascadeType.ALL, orphanRemoval = true)` po stronie `Scouting Report`,
- `@ManyToOne(optional = false)` po stronie `Detailed Rating`.

Kolekcja ocen szczegółowych w encji `ScoutingReport` (`List<DetailedRating>`) będzie persystowana przez Hibernate; dodawanie i usuwanie elementów odbywa się na tej kolekcji przed zapisem encji nadrzędnej.

=== Walidacja sumy wag - `validateDetailedRatings()`

Ograniczenie z wymagania 18 („suma wag wszystkich ocen szczegółowych w obrębie jednego raportu = dokładnie `1.0`") nie da się wyrazić wprost w UML jako liczność ani w Hibernate jako ograniczenie kolumny. Zostanie zrealizowane jako *metoda obiektowa* klasy `Scouting Report`:

- `+ validateDetailedRatings(): void` - przegląda kolekcję `Detailed Rating`, sumuje wagi (`BigDecimal`) i rzuca wyjątek walidacyjny, jeżeli suma jest różna od `1.0` (z tolerancją dla błędów reprezentacji zmiennoprzecinkowej).

Metoda zostanie wywołana w serwisie aplikacyjnym przed `scoutingReportRepository.save(...)` (scenariusz `Create Scouting Report`, krok 13).

=== Ograniczenie `{subset}` - `validateMatchesObservedByScout()`

Na diagramie klasowym występuje ograniczenie `{subset}` pomiędzy asocjacjami `Scout watches Match` oraz `Scouting Report is made based on Match`. Oznacza to, że raport nie może być oparty o mecz, którego autor raportu nie obserwował (zbiór meczów raportu jest podzbiorem zbioru meczów obserwowanych przez tego samego Scouta).

Ograniczenie zostanie zaimplementowane jako metoda obiektowa klasy `Scouting Report`:

- `+ validateMatchesObservedByScout(): void` - dla każdego meczu z `basedOnMatches` sprawdza, czy znajduje się on w `createdBy.watchedMatches`; w przeciwnym przypadku rzuca wyjątek walidacyjny.

Metoda będzie wywoływana w serwisie aplikacyjnym przed zapisem raportu (w tym samym miejscu co `validateDetailedRatings()`), aby nigdy nie utrwalić w bazie powiązania `Scouting Report - Match` niepopartego wcześniej istniejącym powiązaniem `Scout - Match`.

== Atrybuty pochodne - metody `get{AtrybutPochodny}()`

Wszystkie atrybuty pochodne (oznaczone w diagramie prefiksem `/`) zostaną zaimplementowane jako *gettery wyliczające wartość on-the-fly* na podstawie powiązanych obiektów, bez utrwalania w bazie (`@Transient` w Hibernate):

- `Player.getAverageRating(): BigDecimal` - średnia z `finalRating` wszystkich raportów zawodnika.
- `Scouting Report.getFinalRating(): BigDecimal` - średnia ważona ocen szczegółowych (`rating × weight`).
- `Delegation.getCost(): BigDecimal` - liczone według wzoru z wymagania 21.
- `Match Stats.getFieldGoalPercentage()` / `getThreePointPercentage()` / `getFreeThrowPercentage()` - procenty skuteczności (analogicznie dla `Shooting Analysis`).

== Ograniczenia unikalności (`unique`)

Atrybuty oznaczone jako unikatowe (`email` w `Person`, `license_number` w `Scout`) zostaną zmapowane adnotacją `@Column(unique = true, nullable = false)` w Hibernate. Ograniczenie jest egzekwowane na poziomie bazy (constraint `UNIQUE`) - próba zapisu duplikatu kończy się wyjątkiem `DataIntegrityViolationException` przechwytywanym w warstwie serwisowej i tłumaczonym na czytelny komunikat dla użytkownika (NFR 6).

== Ograniczenie istnienia obu drużyn przy tworzeniu meczu

Wymaganie 12 narzuca, że każdy mecz ma dokładnie dwie drużyny i pełnią one różne role: `host` oraz `guest`. Ograniczenie to zostanie wymuszone już na etapie tworzenia obiektu `Match`.

- W konstruktorze klasy `Match` zostanie wywołana metoda:
  - `+ validateBothTeams(): void`
- Metoda sprawdzi, że:
  - `host != null`,
  - `guest != null`,
  - `host.id != guest.id` (lub referencyjnie `host != guest`),
  - role są kompletne (mecz zawsze ma oba końce relacji: gospodarza i gościa).
- Jeżeli warunek nie zostanie spełniony, konstruktor przerwie tworzenie obiektu przez rzucenie wyjątku domenowego (np. `IllegalArgumentException` / własny `DomainValidationException`).

W praktyce oznacza to, że nie da się utworzyć ani zapisać meczu bez przypisanego gospodarza i gościa oraz nie da się utworzyć meczu, w którym obie role wskazują ten sam klub.

== Implementacja atrybutów klasowych, dostępu do danych i metod obiektowych

*Atrybuty klasowe* (wymagania 22 i 23) - na encji `Delegation`:
- `private static final BigDecimal DAILY_LIVING_COST = 250`
- `private static final BigDecimal FLIGHT_COST = 1500`
- użycie w `getCost()` przy liczeniu kosztu delegacji

*Dostęp do zbiorów obiektów (zamiast `static Set` w encji)* - repozytoria Spring Data JPA, np.:
- `PlayerRepository extends JpaRepository<Player, Long>` - `findAll()`, zapytania po `player_status`, `position`,
- `ClubRepository` - `findByLeague(...)`,
- `ScoutingReportRepository`, `DelegationRepository` itd. według potrzeb przypadków użycia,
- zapis i usuwanie: `repository.save(entity)`, `repository.delete(entity)` (kaskady kompozycji obsłuży Hibernate).

*Nawigacja po asocjacjach* - pola kolekcji / referencji w encjach (`List`, `Set`) z adnotacjami JPA, np. `@OneToMany List<DetailedRating> detailedRatings` w `ScoutingReport`, `@OneToMany List<MatchStats> matchStats` w `Player`.

*Metody obiektowe na encjach* - m.in.:
- gettery/settery pól oraz `@Enumerated` dla enumów,
- `Player.becomeUniversityPlayer(...)`, `Player.becomeProfessionalPlayer(...)`, `Player.changeStatus(PlayerStatus)`,
- `ScoutingReport.validateDetailedRatings()` (wywołanie przed `save` w serwisie),
- `@Transient` gettery pochodne: `getFinalRating()`, `getAverageRating()`, `getCost()`, procenty w `MatchStats` / `ShootingAnalysis`.
