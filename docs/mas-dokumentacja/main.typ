#import "template.typ": *

// Główna konfiguracja dokumentu
#show: project.with(
  title: "ScoutForce - Dokumentacja",
  authors: (
    "Betlej Filip s30331", // Zmień na swoje dane, np. Nazwisko Imię NrIndeksu
  ),
  group: "WIs I.6 - 16c, 2w",
  date: "Maj 2026",
)

// Dołączanie poszczególnych sekcji dokumentacji
#include "contents/01-wymagania-uzytkownika.typ"
#include "contents/02-diagramy-przypadkow-uzycia.typ"
#include "contents/03-wymagania-niefunkcjonalne.typ"
#include "contents/04-analityczny-diagram-klas.typ"
#include "contents/05-projektowy-diagram-klas.typ"
#include "contents/06-scenariusz-przypadku-uzycia.typ"
#include "contents/07-diagram-aktywnosci.typ"
#include "contents/08-diagram-stanu.typ"
#include "contents/09-projekt-gui.typ"
#include "contents/10-omowienie-decyzji.typ"
