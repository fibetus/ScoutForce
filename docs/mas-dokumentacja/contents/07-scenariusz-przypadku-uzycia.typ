= Scenariusze przypadków użycia

== Create Scouting Report

=== Informacje ogólne

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
  [Utworzenie i zatwierdzenie raportu skautingowego (`Scouting Report`) dla wybranego zawodnika (`Player`) na podstawie obserwacji jego występów.],

  [*Wyzwalacz*],
  [Scout decyduje się utworzyć raport skautingowy zawodnika po obserwacji jego występu w co najmniej jednym meczu.],

  [*Warunki początkowe*],
  [
    - Scout obserwował dotychczas *co najmniej jednego zawodnika* (istnieje co najmniej jedna instancja `Match Stats` dla zawodnika w meczu, który Scout obserwował).
    - Scout obejrzał dotychczas *co najmniej jeden mecz*.
  ],

  [*Warunki końcowe - sukces*],
  [
    - W systemie istnieje nowy obiekt `Scouting Report` powiązany z zalogowanym Scoutem oraz wybranym zawodnikiem (`Player`).
    - Raport zawiera co najmniej jedną ocenę szczegółową (`Detailed Rating`); *suma wag* wszystkich ocen w raporcie jest równa dokładnie `1.0`.
    - Atrybut pochodny `/finalRating` raportu jest możliwy do wyliczenia na podstawie ocen szczegółowych.
    - Atrybut `recommendation` raportu przyjmuje jedną z wartości: `STRONG_BUY`, `BUY`, `NEUTRAL`, `PASS`.
  ],

  [*Warunki końcowe - porażka*],
  [
    - Stan systemu nie zmienia się - żaden `Scouting Report` ani żadna instancja `Detailed Rating` nie zostają zapisane, atrybuty pochodne zawodnika pozostają bez zmian.
  ],
)

=== Powiązane przypadki użycia

- `<<include>> View Players List` - wyświetlenie listy zawodników jako punkt wejścia do wyboru obiektu raportu.
- `<<include>> Add Detailed Ratings` - obowiązkowa część procesu (raport musi mieć `1..*` ocen szczegółowych).
- `<<extend>> View Player's Matches` - opcjonalne rozszerzenie umożliwiające zawężenie raportu do podzbioru meczów wybranego ręcznie przez Scouta.

=== Scenariusz główny

Wariant domyślny - raport tworzony bezpośrednio z poziomu panelu zawodnika, *bez przeglądania listy meczów*. Zakres raportu obejmuje *wszystkie* mecze, w których wystąpił wybrany zawodnik i które jednocześnie obserwował Scout.

+ Scout wybiera z menu opcję przeglądania zawodników. *System wywołuje* `<<include>> View Players List`.
+ System wyświetla listę zawodników. Dla każdej pozycji prezentowane są: pola identyfikujące (`first_name`, `last_name`, `position`, `club`, `player_status`) oraz atrybut pochodny: `/averageRating`.
+ Scout wybiera zawodnika z listy.
+ System wyświetla panel wybranego zawodnika z dwiema akcjami dostępnymi natychmiast:
  - _„Create Scouting Report"_ - utworzenie raportu obejmującego *wszystkie* mecze zawodnika obserwowane przez Scouta (przepływ domyślny, kontynuuje krok 5);
  - _„View Player's Matches"_ - opcjonalne przejście do listy meczów w celu zawężenia raportu do wybranych meczów (przepływ alternatywny A1).\
+ Scout wybiera akcję _„Create Scouting Report"_.
+ System ustala zakres meczów raportu jako pełny zbiór meczów spełniających dwa warunki: zawodnik wystąpił w meczu (istnieje obiekt `Match Stats` dla pary _(Player, Match)_) oraz Scout obserwował ten mecz. _Jeżeli zbiór jest pusty - wyjątek E1._
+ System wyświetla formularz nowego raportu z polami: notatka tekstowa (`note`), rekomendacja draftowa (`recommendation`), dynamiczna sekcja ocen szczegółowych oraz panel statystyk meczów wchodzących w zakres raportu (uśrednione `minutes_played`, `points`, `rebounds`, `assists`, `steals`, `blocks` z odpowiednich `Match Stats`).
+ Scout dodaje pierwszą ocenę szczegółową. *System wywołuje* `<<include>> Add Detailed Ratings` - przyjmuje od Scouta: `type` (kategoria swobodna, np. _„offense"_, _„defense"_, _„athleticism"_, _„basketball IQ"_, _„character"_), `rating: [1, 10]`, `comment` oraz `weight: [0.0, 1.0]`. _Jeżeli wartość pola jest poza zakresem - wyjątek E4._
+ Scout powtarza krok 8 dla kolejnych kategorii oceny (`1..*` ocen szczegółowych - co najmniej jedna wymagana).
+ Scout wypełnia pole `note` opisem słownym swojej obserwacji.
+ Scout wybiera z listy rozwijanej wartość pola `recommendation`: `STRONG_BUY`, `BUY`, `NEUTRAL` lub `PASS`.
+ Scout zatwierdza formularz przyciskiem _„Submit Report"_.
+ System weryfikuje, czy *suma* atrybutów `weight` wszystkich pozycji `Detailed Rating` w raporcie jest równa dokładnie `1.0`. _Jeżeli suma jest inna - wyjątek E3._
+ System zapisuje nowy obiekt `Scouting Report` wraz z powiązanymi obiektami `Detailed Rating`.
+ System wyświetla potwierdzenie zapisu raportu wraz z wyliczonym `/finalRating`.
+ Przypadek użycia kończy się sukcesem.

=== Scenariusze alternatywne

==== A1. Scout zawęża raport do wybranych meczów (`<<extend>> View Player's Matches`)

_Punkt rozszerzenia:_ _Wybór zakresu meczów_ (krok 4 scenariusza głównego). Rozszerzenie jest *opcjonalne* - Scout może je pominąć i utworzyć raport bezpośrednio (krok 5 scenariusza głównego).

+ Scout w kroku 4 wybiera akcję _„View Player's Matches"_ zamiast _„Create Scouting Report"_. *System wywołuje* `<<extend>> View Player's Matches`.
+ System wyświetla listę meczów wybranego zawodnika *ograniczoną* do meczów obserwowanych przez Scouta. Dla każdego meczu prezentowane są: `date`, `place`, `host_score : guest_score`, nazwa klubu gospodarza i gościa oraz statystyki indywidualne zawodnika z `Match Stats` (`minutes_played`, `points`, `rebounds`, `assists`, `steals`, `blocks`). _Jeżeli lista jest pusta - wyjątek E1._
+ Scout zaznacza wybrane mecze (co najmniej jeden mecz wymagany).
+ Scout wybiera akcję _„Create Scouting Report from selected matches"_.
+ System ustala zakres meczów raportu jako zbiór dokładnie tych meczów, które Scout zaznaczył (zamiast pełnego zbioru obserwowanych meczów zawodnika).
+ Scenariusz wraca do kroku 7 scenariusza głównego.

=== Scenariusze wyjątków

==== E1. Wybrany zawodnik nie ma żadnego meczu obserwowanego przez Scouta

_W kroku 6 scenariusza głównego lub w kroku A1.2 scenariusza alternatywnego_, jeżeli ustalony zbiór meczów wchodzących w zakres raportu jest pusty:

+ System wyświetla komunikat: _„Chosen player has no matches you've observed"_
+ System wraca do panelu zawodnika (krok 4 scenariusza głównego).
+ Scout może wybrać innego zawodnika lub zakończyć pracę.
+ Przypadek użycia kończy się niepowodzeniem (warunki końcowe - porażka).

==== E2. Scout nie dodał żadnej oceny szczegółowej

_W kroku 13 scenariusza głównego_, jeżeli kolekcja `Detailed Rating` raportu jest pusta:

+ System wyświetla komunikat walidacji: _„Report needs at least one Detailed Rating"_
+ System pozostawia formularz w trybie edycji bez zapisu danych.
+ Scenariusz wraca do kroku 8 scenariusza głównego.

==== E3. Suma wag ocen szczegółowych jest różna od 1.0

_W kroku 13 scenariusza głównego_, podczas walidacji:

+ System wyświetla komunikat: _„Sum of weights need to be exactly 1.0. Current sum: <x>."_
+ System pozostawia formularz w trybie edycji.
+ Scenariusz wraca do kroku 8 scenariusza głównego (w celu korekty wag).

==== E4. Wartość oceny lub wagi poza dopuszczalnym zakresem, brak komentarza

_W kroku 8 scenariusza głównego_, podczas wprowadzania pojedynczej oceny szczegółowej, jeżeli niespełnione warunki: `rating: [1, 10]`, `weight: [0.0, 1.0]` lub brak `comment`:

+ System wyświetla komunikat: _„Rating must be in 1-10 range. Weight must be in 0.0-1.0 range."_
+ System uniemożliwia dodanie nieprawidłowej oceny do raportu.
+ Scout koryguje wartość - scenariusz pozostaje w kroku 8.

==== E5. Brak wybranej rekomendacji draftowej, brak notki

_W kroku 11 scenariusza głównego_, jeżeli pole `recommendation` nie zostało wypełnione (`recommendation = NULL`) lub pole `note` jest puste:

+ System wyświetla komunikat walidacji: _„Notes and recommendation cannot be empty."_
+ System pozostawia formularz w trybie edycji.
+ Scenariusz wraca do kroku 11 scenariusza głównego.

==== E6. Scout anuluje operację

_W dowolnym kroku po kroku 5 scenariusza głównego_:

+ Scout wybiera akcję _„Cancel"_.
+ System wyświetla potwierdzenie: _„Do you want to discard report?"_ z przyciskami _„Discard"_ oraz _„Keep editing"_.
+ Jeżeli Scout wybiera _„Discard"_ - system odrzuca wszystkie wprowadzone dane i wraca do panelu zawodnika (krok 4).
+ Jeżeli Scout wybiera _„Keep editing"_ - scenariusz wraca do ostatnio aktywnego kroku.
+ Przypadek użycia kończy się niepowodzeniem (warunki końcowe - porażka).


#pagebreak()

== Add Detailed Ratings

=== Informacje ogólne

#table(
  columns: (auto, 1fr),
  align: (left, left),
  stroke: 0.5pt,

  [*Nazwa*],
  [`Add Detailed Ratings`],

  [*Aktor główny*],
  [Scout],

  [*Aktorzy drugorzędni*],
  [brak],

  [*Cel*],
  [Wprowadzenie w formularzu tworzonego raportu skautingowego (`Scouting Report`) pojedynczej pozycji oceny szczegółowej (`Detailed Rating`) w wybranej kategorii - wraz z komentarzem oraz wagą określającą udział tej oceny w `/finalRating` - tak aby została utrwalona w systemie razem z raportem przy zatwierdzeniu w scenariuszu `Create Scouting Report`.],

  [*Wyzwalacz*],
  [Scout, w trakcie wypełniania formularza raportu skautingowego, decyduje się wprowadzić kolejną ocenę szczegółową w wybranej kategorii.],

  [*Warunki początkowe*],
  [
    - Przypadek użycia `Create Scouting Report` jest aktywny - formularz nowego raportu jest otwarty w trybie edycji.
    - Sekcja ocen szczegółowych formularza jest dostępna dla Scouta.
  ],

  [*Warunki końcowe - sukces*],
  [
    - W formularzu tworzonego raportu pojawia się nowa pozycja `Detailed Rating` z polami: `type`, `rating: [1, 10]`, `comment` oraz `weight: [0.0, 1.0]`.
    - Pozycja zostaje dołączona do lokalnej kolekcji ocen formularza (bufor w pamięci) - jeszcze bez utrwalania w systemie.
    - Faktyczne utworzenie obiektu `Detailed Rating` w systemie nastąpi wyłącznie wraz z zatwierdzeniem nadrzędnego `Scouting Report` w scenariuszu `Create Scouting Report` (relacja kompozycji - `Detailed Rating` nie istnieje bez `Scouting Report`).
  ],

  [*Warunki końcowe - porażka*],
  [
    - Lokalny stan formularza nie zmienia się - żadna nowa pozycja `Detailed Rating` nie zostaje dołączona do sekcji ocen szczegółowych raportu.
    - W systemie nie powstaje żaden obiekt `Detailed Rating`.
  ],
)

=== Powiązane przypadki użycia

- `<<include>> from Create Scouting Report` - przypadek użycia jest dołączany w trakcie tworzenia raportu skautingowego (jest jego obowiązkową częścią - raport musi zawierać `1..*` ocen szczegółowych). Scenariusz może zostać wykonany wielokrotnie w obrębie jednego wywołania `Create Scouting Report`.

=== Scenariusz główny

Scout nie zatwierdza pojedynczej oceny - wprowadzone wartości pozostają w buforze formularza, a właściwy zapis ocen w systemie następuje dopiero przy zatwierdzeniu raportu (`Submit Report`) w scenariuszu `Create Scouting Report`. Walidacja zakresów odbywa się po stronie frontendu w trybie ciągłym (live), natomiast wiążąca walidacja systemowa wykonywana jest dopiero w momencie `Submit Report`.

+ Scout wybiera w sekcji ocen szczegółowych formularza akcję _„+ Add rating"_ (przy pustej sekcji: _„+ Add first rating"_).
+ System dodaje do sekcji ocen nowy pusty wiersz oceny szczegółowej z polami: `type`, `rating`, `comment`, `weight` i uruchamia ich walidację wizualną w trybie ciągłym.
+ Scout wprowadza wartość pola `type` (kategoria swobodna, np. _„offense"_, _„defense"_, _„athleticism"_, _„basketball IQ"_, _„character"_).
+ Scout wprowadza wartość pola `rating` z zakresu `[1, 10]`. Frontend na bieżąco sygnalizuje wizualnie poprawność wartości - pole pozostaje w stanie błędu, jeżeli wartość wykracza poza zakres _(wskaźnik wyjątku E1)_.
+ Scout wprowadza treść pola `comment` z uzasadnieniem oceny.
+ Scout wprowadza wartość pola `weight` z zakresu `[0.0, 1.0]`. Frontend na bieżąco sygnalizuje wizualnie poprawność wartości - pole pozostaje w stanie błędu, jeżeli wartość wykracza poza zakres _(wskaźnik wyjątku E1)_.
+ Wprowadzona pozycja pozostaje w sekcji ocen szczegółowych formularza jako element bufora tworzonego raportu - bez utrwalania w systemie.
+ Scout może powtórzyć kroki 1-7 dla kolejnych ocen szczegółowych w innych kategoriach (`1..*` pozycji w obrębie jednego raportu).
+ Przypadek użycia kończy się sukcesem - sterowanie wraca do scenariusza `Create Scouting Report`. Faktyczne utworzenie obiektów `Detailed Rating` nastąpi dopiero przy zatwierdzeniu raportu (`Submit Report`).

=== Scenariusze wyjątków

==== E1. Wartość oceny lub wagi poza dopuszczalnym zakresem (walidacja wizualna)

_W kroku 4 lub 6 scenariusza głównego_, jeżeli wprowadzona wartość nie spełnia warunku `rating: [1, 10]` lub `weight: [0.0, 1.0]`:

+ Frontend wyświetla błąd w czerwonej ramce z błędami: _„Rating must be in 1-10 range. Weight must be in 0.0-1.0 range."_
+ Scout może kontynuować edycję pozostałych pól, jednak nieprawidłowa wartość nie zostaje wyczyszczona automatycznie.
+ Wiążąca walidacja systemowa wykonywana jest w scenariuszu `Create Scouting Report` przy `Submit Report` - jeżeli pozycja nadal zawiera nieprawidłową wartość, raport zostaje odrzucony zgodnie z wyjątkiem E4 scenariusza `Create Scouting Report`.
+ Scenariusz pozostaje odpowiednio w kroku 4 lub 6 do czasu poprawienia wartości lub usunięcia pozycji oceny _(wyjątek E2)_.

==== E2. Scout usuwa rozpoczętą ocenę

_W dowolnym kroku po kroku 1 scenariusza głównego_:

+ Scout wybiera w wierszu oceny akcję _„Delete"_.
+ System usuwa wiersz oceny z sekcji ocen szczegółowych formularza wraz z wprowadzonymi wartościami pól.
+ Lokalny stan formularza nie zawiera tej pozycji - pozostałe wprowadzone wcześniej oceny pozostają nienaruszone.
+ Przypadek użycia kończy się niepowodzeniem (warunki końcowe - porażka) dla tej konkretnej pozycji, sterowanie wraca do scenariusza `Create Scouting Report`.
