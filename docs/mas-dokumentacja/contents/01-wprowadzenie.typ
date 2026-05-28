= Wprowadzenie

== Dziedzina problemowa

System ScoutForce znajduje zastosowanie w dziedzinie profesjonalnego skautingu koszykarskiego w lidze NBA. Wspiera on pracę działów skautingu klubów NBA, odpowiedzialnych za identyfikację, obserwację i ewaluację potencjalnych talentów draftowych - zarówno z uczelni amerykańskich (NCAA), lig zagranicznych, jak i niższych rozgrywek profesjonalnych (np. G-League).

== Cel

Celem budowy systemu jest usprawnienie i usystematyzowanie pełnego cyklu pracy skautingowej: od planowania wyjazdów obserwacyjnych (delegacji), przez rejestrowanie statystyk meczowych i tworzenie raportów skautingowych, po generowanie rankingów draftowych. System ma wyeliminować rozproszone arkusze kalkulacyjne i niespójne notatki, zapewniając jednolite środowisko pracy dla skautów i dyrektorów sportowych.

== Zakres odpowiedzialności systemu

System odpowiada za: zarządzanie danymi zawodników i ich statusami w procesie skautingowym, rejestrowanie meczów i szczegółowych statystyk występów, tworzenie raportów skautingowych z wielowymiarowymi ocenami, planowanie i rozliczanie delegacji skautingowych, prowadzenie analiz strzeleckich per sezon i strefa boiska oraz generowanie rankingu draftowego (Big Board) na podstawie zgromadzonych ocen.

== Użytkownicy systemu

Potencjalni użytkownicy systemu to:
- *Dyrektor sportowy klubu* (*Director* w notacji UML) - osoba zarządzająca działem skautingu, odpowiedzialna za planowanie delegacji i podejmowanie decyzji draftowych.
- *Skaut* (*Scout* w notacji UML) - pracownik terenowy obserwujący zawodników na meczach, tworzący raporty skautingowe i wprowadzający statystyki meczowe.

== Konwencje nazewnictwa

W całej dokumentacji przyjęto rozdzielenie warstwy opisu biznesowego (język polski) od warstwy modelowania i implementacji (język angielski). Poniższe reguły obowiązują we wszystkich rozdziałach.

=== Język i zakres stosowania

- *Opis narracyjny* (wymagania użytkownika, wprowadzenie, komentarze i przepływ scenariuszy, komunikaty w scenariuszu) - *polski*.
- *Elementy modelu UML* (aktorzy, przypadki użycia, klasy, atrybuty, stany, relacje `<<include>>` / `<<extend>>` oraz komunikaty w GUI) - *angielski*.
- W scenariuszach przypadków użycia aktor wykonujący kroki oznaczany jest jako *Scout* lub *Director* (zgodnie z diagramem przypadków użycia), a nie „Skaut" / „Dyrektor".

=== Przypadki użycia (Use Cases)

- Nazwy w notacji *Title Case*, po angielsku, zgodne z diagramem przypadków użycia, np. `Create Scouting Report`, `View Players List`, `Add Detailed Ratings`, `View Player's Matches`.
- W tekście scenariusza odwołania do przypadków użycia zapisywane są w backtickach, np. `Create Scouting Report`.
- Relacje UML: `<<include>>`, `<<extend>>` - nazwa docelowa po angielsku.

=== Klasy i obiekty domenowe

- Nazwy klas w diagramach: *PascalCase* z odstępem między słowami, np. `Player`, `Scouting Report`, `Detailed Rating`, `Match Stats`, `Delegation`, `Club`.

=== Atrybuty i pola danych

- Wartości wyliczeniowe (enum) w modelu UML i implementacji (Java): *UPPERCASE* ze znakiem podkreślenia, zgodnie z konwencją stałych enumów Javy, np. `STRONG_BUY`, `BUY`, `NEUTRAL`, `PASS` oraz statusy zawodnika: `NEW`, `OBSERVED`, `MEDICAL_VERIFICATION`, `INVITED_TO_WORKOUT`, `INVITED_TO_BIG_BOARD`, `DELISTED`.
- W *wymaganiach funkcjonalnych* (rozdz. 2) wartości enum mogą być podane w pisowni naturalnej (np. `"strong buy"`, `"new"`) - bez konieczności stosowania UPPERCASE.

=== Aktorzy i role

#table(
  columns: (1fr, 1fr),
  stroke: 0.5pt,
  [*Opis (PL, narracja)*], [*Aktor / rola (UML, EN)*],
  [Skaut], [`Scout`],
  [Dyrektor sportowy klubu / dyrektor klubu], [`Director`],
)

=== Terminy domenowe w języku polskim

Poniższe pojęcia w tekście polskim *nie są tłumaczone* na angielski w narracji, lecz mają przypisane nazwy techniczne w modelu:

#table(
  columns: (1fr, 1fr),
  stroke: 0.5pt,
  [*Termin (PL)*], [*Odpowiednik w modelu UML*],
  [Raport skautingowy], [`Scouting Report`],
  [Ocena szczegółowa], [`Detailed Rating`],
  [Statystyki meczowe (występ zawodnika)], [`Match Stats`],
  [Zawodnik], [`Player`],
  [Mecz], [`Match`],
  [Delegacja], [`Delegation`],
  [Klub], [`Club`],
  [Ranking draftowy (Big Board)], [lista zawodników o statusie `INVITED_TO_BIG_BOARD`],
)
