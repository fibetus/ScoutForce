= Scenariusz przypadku użycia

== Informacje ogólne

#table(
  columns: (auto, 1fr),
  align: (left, left),
  stroke: 0.5pt,

  [*Nazwa*],
  [`Create Scouting Report`],

  [*Aktor główny*],
  [Scout],

  [*Aktorzy drugorzędni*],
  [brak],

  [*Cel*],
  [Utworzenie i zatwierdzenie raportu skautingowego dla wybranego zawodnika na podstawie obserwacji jego występów.],

  [*Wyzwalacz*],
  [Skaut decyduje się stworzyć raport skautingowy zawodnika po obserwacji jego występu w co najmniej jednym meczu.],

  [*Warunki początkowe*],
  [
    - Skaut obserwował dotychczas *co najmniej jednego zawodnika* (istnieje co najmniej jedna instancja `Match Stats` dla zawodnika w meczu, który skaut oglądał).
    - Skaut obejrzał dotychczas *co najmniej jeden mecz*.
  ],

  [*Warunki końcowe - sukces*],
  [
    - W systemie istnieje nowy obiekt `Scouting Report` powiązany z zalogowanym skautem oraz wybranym zawodnikiem.
    - Raport zawiera co najmniej jedną ocenę szczegółową (`Detailed Rating`) o sumie wag równej dokładnie `1.0`.
    - Atrybut pochodny `/finalRating` raportu jest możliwy do wyliczenia na podstawie ocen szczegółowych.
    - Atrybut `recommendation` raportu przyjmuje jedną z wartości: `strong_buy`, `buy`, `neutral`, `pass`..
  ],

  [*Warunki końcowe - porażka*],
  [
    - Stan systemu nie zmienia się - żaden `Scouting Report` ani żadna instancja `Detailed Rating` nie zostają zapisane, atrybuty pochodne zawodnika pozostają bez zmian.
  ],
)

== Powiązane przypadki użycia

- `«include» View All Players` - wyświetlenie listy zawodników jako punkt wejścia do wyboru obiektu raportu.
- `«include» Add Detailed Ratings` - obowiązkowa część procesu (raport musi mieć `1..*` ocen szczegółowych).
- `«extend» View Player Matches` - opcjonalne rozszerzenie umożliwiające zawężenie raportu do podzbioru meczów wybranego ręcznie przez skauta.

== Scenariusz główny

Wariant domyślny - raport tworzony bezpośrednio z poziomu panelu zawodnika, *bez przeglądania listy meczów*. Zakres raportu obejmuje *wszystkie* mecze, w których wystąpił wybrany zawodnik i które jednocześnie obserwował skaut.

+ Skaut wybiera z menu opcję przeglądania zawodników. *System wywołuje* `«include» View All Players`.
+ System wyświetla listę zawodników. Dla każdej pozycji prezentowane są: pola identyfikujące (`firstName`, `lastName`, `position`, `club`, `player_status`) oraz atrybuty pochodne pomocnicze: `/averageRating`.
+ Skaut wybiera zawodnika z listy.
+ System wyświetla panel wybranego zawodnika z dwiema akcjami dostępnymi natychmiast:
  - _„Create Scouting Report"_ - utworzenie raportu obejmującego *wszystkie* mecze zawodnika obserwowane przez skauta (przepływ domyślny, kontynuuje krok 5);
  - _„View Player Matches"_ - opcjonalne przejście do listy meczów w celu zawężenia raportu do wybranych meczów (przepłw alternatywny A1).\
+ Skaut wybiera akcję _„Create Scouting Report"_.
+ System ustala zakres meczów raportu jako pełny zbiór meczów spełniających dwa warunki: zawodnik wystąpił w meczu (istnieje obiekt `Match Stats` dla pary _(zawodnik, mecz)_) oraz skaut obserwował ten mecz. 
_Jeżeli zbiór jest pusty - wyjątek E1._
+ System wyświetla formularz nowego raportu z polami: notatka tekstowa (`note`), rekomendacja draftowa (`recommendation`), dynamiczna sekcja ocen szczegółowych oraz panel statystyk meczów wchodzących w zakres raportu (uśrednione `minutes_played`, `points`, `rebounds`, `assists`, `steals`, `blocks` z odpowiednich `Match Stats`).
+ Skaut dodaje pierwszą ocenę szczegółową. *System wywołuje* `«include» Add Detailed Ratings` - przyjmuje od skauta: `type` (kategoria swobodna, np. _„offense"_, _„defense"_, _„athleticism"_, _„basketball IQ"_, _„character"_), `rating: [1, 10]`, `comment` oraz `weight: [0.0, 1.0]`. _Jeżeli wartość pola jest poza zakresem - wyjątek E4._
+ Skaut powtarza krok 8 dla kolejnych kategorii oceny (`1..*` ocen szczegółowych - co najmniej jedna wymagana).
+ Skaut wypełnia pole `note` opisem słownym swojej obserwacji.
+ Skaut wybiera z listy rozwijanej wartość pola `recommendation`: `strong_buy`, `buy`, `neutral` lub `pass`.
+ Skaut zatwierdza formularz przyciskiem _„Zatwierdź raport"_.
+ System weryfikuje czy suma `Detailed Rating` spełnia warunek `weight: [0.0, 1.0]`.
+ System zapisuje nowy obiekt `Scouting Report` wraz z powiązanymi obiektami `Detailed Rating`.
+ System wyświetla potwierdzenie zapisu raportu wraz z wyliczonym `/finalRating`.
+ Przypadek użycia kończy się sukcesem.

== Scenariusze alternatywne

=== A1. Skaut zawęża raport do wybranych meczów (`«extend» View Player Matches`)

_Punkt rozszerzenia:_ _Wybór zakresu meczów_ (krok 4 scenariusza głównego). Rozszerzenie jest *opcjonalne* - skaut może je pominąć i utworzyć raport bezpośrednio (krok 5 scenariusza głównego).

+ Skaut w kroku 4 wybiera akcję _„View Player Matches"_ zamiast _„Create Scouting Report"_. *System wywołuje* `«extend» View Player Matches`.
+ System wyświetla listę meczów wybranego zawodnika *ograniczoną* do meczów obserwowanych przez skauta. Dla każdego meczu prezentowane są: `date`, `place`, `host_score : guest_score`, nazwa klubu gospodarza i gościa oraz statystyki indywidualne zawodnika z `Match Stats` (`minutes_played`, `points`, `rebounds`, `assists`, `steals`, `blocks`). _Jeżeli lista jest pusta - wyjątek E1._
+ Skaut zaznacza wybrane mecze (co najmniej jeden mecz wymagany).
+ Skaut wybiera akcję _„Create Scouting Report from selected matches"_.
+ System ustala zakres meczów raportu jako zbiór dokładnie tych meczów, które skaut zaznaczył (zamiast pełnego zbioru obserwowanych meczów zawodnika).
+ Scenariusz wraca do kroku 7 scenariusza głównego.

== Scenariusze wyjątków

=== E1. Wybrany zawodnik nie ma żadnego meczu obserwowanego przez skauta

_W kroku 6 scenariusza głównego lub w kroku A1.2 scenariusza alternatywnego_, jeżeli ustalony zbiór meczów wchodzących w zakres raportu jest pusty:

+ System wyświetla komunikat: _„Wybrany zawodnik nie ma żadnego meczu, który obserwowałeś. Aby utworzyć raport, najpierw zaobserwuj co najmniej jeden mecz tego zawodnika."_
+ System wraca do panelu zawodnika (krok 4 scenariusza głównego).
+ Skaut może wybrać innego zawodnika lub zakończyć pracę.
+ Przypadek użycia kończy się niepowodzeniem (warunki końcowe — porażka).

=== E2. Skaut nie dodał żadnej oceny szczegółowej

_W kroku 13 scenariusza głównego_, jeżeli kolekcja `Detailed Rating` raportu jest pusta:

+ System wyświetla komunikat walidacji: _„Raport musi zawierać co najmniej jedną ocenę szczegółową. Dodaj przynajmniej jedną ocenę przed zatwierdzeniem raportu."_
+ System pozostawia formularz w trybie edycji bez zapisu danych.
+ Scenariusz wraca do kroku 8 scenariusza głównego.

=== E3. Suma wag ocen szczegółowych jest różna od 1.0

_W kroku 13 scenariusza głównego_, podczas walidacji:

+ System wyświetla komunikat: _„Suma wag ocen szczegółowych musi wynosić dokładnie 1.0. Aktualna suma: \<obecna wartość\>."_
+ System pozostawia formularz w trybie edycji.
+ Scenariusz wraca do kroku 8 scenariusza głównego (w celu korekty wag).

=== E4. Wartość oceny lub wagi poza dopuszczalnym zakresem

_W kroku 8 scenariusza głównego_, podczas wprowadzania pojedynczej oceny szczegółowej, jeżeli  niespełnione warunki: `rating: [1, 10]` lub `weight: [0.0, 1.0]`:

+ System wyświetla komunikat walidacji pola: _„Ocena musi mieścić się w zakresie 1–10. Waga musi mieścić się w zakresie 0.0–1.0."_
+ System uniemożliwia dodanie nieprawidłowej oceny do raportu.
+ Skaut koryguje wartość - scenariusz pozostaje w kroku 8.

=== E5. Brak wybranej rekomendacji draftowej

_W kroku 11 scenariusza głównego_, jeżeli pole `recommendation` nie zostało wypełnione (`recommendation = NULL`):

+ System wyświetla komunikat walidacji: _„Rekomendacja draftowa jest wymagana. Wybierz jedną z opcji: strong_buy, buy, neutral, pass."_
+ System pozostawia formularz w trybie edycji.
+ Scenariusz wraca do kroku 11 scenariusza głównego.

=== E6. Skaut anuluje operację

_W dowolnym kroku po kroku 5 scenariusza głównego_:

+ Skaut wybiera akcję _„Anuluj"_.
+ System wyświetla potwierdzenie: _„Czy na pewno chcesz anulować raport? Wprowadzone dane zostaną utracone."_
+ Jeżeli skaut potwierdza - system odrzuca wszystkie wprowadzone dane i wraca do panelu zawodnika (krok 4).
+ Jeżeli skaut wycofuje anulowanie - scenariusz wraca do ostatnio aktywnego kroku.
+ Przypadek użycia kończy się niepowodzeniem (warunki końcowe - porażka).
