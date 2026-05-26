= Projekt interfejsu użytkownika (GUI)

== Ekran główny aplikacji po kliknięciu w zawodnika

#figure(
  image("../assets/ScoutForceGUI/landingPage.png", width: 95%),
  caption: [Ekran główny aplikacji ScoutForce po kliknięciu w zawodnika z listy zawodników po lewej stronie.],
) <fig:gui-landing>

== Create Scouting Report

#figure(
  image("../assets/ScoutForceGUI/CSR_landing1.png", width: 95%),
  caption: [Ekran tworzenia raportu skautingowe zawodnika - widzimy statystyki zawodnika, sekcję z Detailed Ratings oraz Notes.],
) <fig:gui-players-list>

#figure(
  image("../assets/ScoutForceGUI/CSR_landing2.png", width: 95%),
  caption: [Ekran tworzenia raportu skautingowe zawodnika część dalsza z Recommendation do wyboru],
) <fig:gui-player-panel>

#figure(
  image("../assets/ScoutForceGUI/CSR_chosen.png", width: 95%),
  caption: [Formularz wypełniony, spełniający wymogi do wysłania.],
) <fig:gui-report-form>

== Stany walidacji przy zatwierdzaniu raportu

#figure(
  image("../assets/ScoutForceGUI/CSR_EX_Submit.png", width: 95%),
  caption: [Stany walidacji formularza (wyjątki E2-E5). Komunikaty błędów: brak ocen szczegółowych (E2), suma wag != 1.0 (E3), wartości poza zakresem (E4), brak wybranej rekomendacji oraz pusta notatka (E5). System blokuje zapis do momentu spełnienia wszystkich warunków.],
) <fig:gui-validation>

== Potwierdzenie anulowania raportu (E6)

#figure(
  image("../assets/ScoutForceGUI/CSR_E6.png", width: 95%),
  caption: [Dialog potwierdzenia anulowania (wyjątek E6). Komunikat: _„Czy na pewno chcesz anulować raport? Wprowadzone dane zostaną utracone."_ Skaut może potwierdzić anulowanie lub wrócić do edycji.],
) <fig:gui-cancel>

== Potwierdzenie zapisu raportu - sukces

#figure(
  image("../assets/ScoutForceGUI/CSR_success.png", width: 95%),
  caption: [Ekran potwierdzenia zapisu raportu (kroki 14-15 scenariusza głównego). System wyświetla wyliczony `/finalRating` oraz podsumowanie zapisanego raportu. Przypadek użycia kończy się sukcesem.],
) <fig:gui-success>

== Wyjątek E1 - brak meczów obserwowanych przez skauta (widok Create Report)

#figure(
  image("../assets/ScoutForceGUI/E1_NoMatches.png", width: 95%),
  caption: [Stan pusty E1 - wybrany zawodnik nie ma żadnego meczu obserwowanego przez skauta. Komunikat: _„Wybrany zawodnik nie ma żadnego meczu, który obserwowałeś. Aby utworzyć raport, najpierw zaobserwuj co najmniej jeden mecz tego zawodnika."_ System wraca do panelu zawodnika.],
) <fig:gui-e1>

== Widok meczów zawodnika - brak meczów (scenariusz alternatywny A1 + E1)

#figure(
  image("../assets/ScoutForceGUI/ViewMatches_NoMatches.png", width: 95%),
  caption: [Widok _„View Player Matches"_ (scenariusz alternatywny A1, krok A1.2) w sytuacji, gdy lista meczów jest pusta - wyjątek E1. Skaut nie może utworzyć raportu z wybranych meczów, ponieważ żaden mecz nie spełnia warunków początkowych.],
) <fig:gui-matches-empty>
