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
#include "contents/01-wprowadzenie.typ"
#include "contents/02-wymagania-uzytkownika.typ"
#include "contents/03-diagramy-przypadkow-uzycia.typ"
#include "contents/04-wymagania-niefunkcjonalne.typ"
#include "contents/05-analityczny-diagram-klas.typ"
#include "contents/06-projektowy-diagram-klas.typ"
#include "contents/07-scenariusz-przypadku-uzycia.typ"
#include "contents/08-diagram-aktywnosci.typ"
#include "contents/09-diagram-stanu.typ"
#include "contents/10-projekt-gui.typ"
#include "contents/11-omowienie-decyzji.typ"
