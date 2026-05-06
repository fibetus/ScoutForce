# ScoutNBA - Plan Tworzenia Dokumentacji (MAS)

## Cel
Połączenie formalnych wymagań projektowych MAS z konkretnym kontekstem biznesowym domeny ScoutForce NBA w celu stworzenia ustrukturyzowanego, podzielonego na etapy planu pisania dokumentacji.

---

## Faza 1: Wymagania i Przypadki Użycia

### Krok 1: Wymagania Użytkownika ("Historyjka") i Wymagania Niefunkcjonalne
**Działania:**
1. **Historyjka użytkownika:** Zredagowanie narracyjnego scenariusza biznesowego opisującego codzienną pracę Skauta – od zaplanowania delegacji w systemie, przez ocenę gracza na wybranym meczu NCAA, aż po powrót z raportem i wpływ jego pracy na ranking klubu.
2. **Wymagania niefunkcjonalne:** Przygotowanie wypunktowanej listy wymogów (technologia: Spring Boot, React, PostgreSQL; reguły biznesowe: ograniczenia budżetowe, ścisła skala ocen 1-10, unikalność w rankingu Big Board).
*Odpowiada punktom MAS:* 4.1.2, 4.1.3 (wym. niefunkcjonalne).

### Krok 2: Wymagania Funkcjonalne (Diagram Przypadków Użycia)
**Działania:**
1. Zdefiniowanie głównych aktorów: `Skaut` i `Dyrektor/Manager`.
2. Stworzenie głównych Use Case'ów (UC) i połączeń między nimi.
3. **Kluczowy cel etapu:** Wyróżnienie na diagramie nietrywialnego UC: **"Stwórz Raport Skautingowy"**, dołączając za pomocą relacji `«include»` przypadek **"Aktualizuj Pozycję w Big Board"** (lub ewentualnie relacja `«extend»` z weryfikacją medyczną).
*Odpowiada punktom MAS:* 4.1.3 (wym. funkcjonalne).

---

## Faza 2: Modelowanie Statyczne

### Krok 3: Analiza Statyczna (Analityczny Diagram Klas)
**Działania:**
1. Narysowanie bazowego diagramu opartego o **15 klas biznesowych** wymienionych w pliku `ScoutNBA - Domain Model.md` (wraz ze wszystkimi asocjacjami, agregacjami, dziedziczeniami).
2. Zadbanie o abstrakcję: klasy `Osoba`, relacje asocjacyjne wiele-do-wielu (dla `StatystykaMeczowa` i `PozycjaNaBigBoard`). W tym kroku **nie wolno** umieszczać konstruktów czysto programistycznych z Javy (typy tablicowe implementacyjne).
3. **Absolutny Priorytet:** Ten diagram musi być perfekcyjny logicznie, ponieważ będzie głównym przedmiotem ewaluacji przed wdrożeniem (brak błędów notacji UML).
*Odpowiada punktom MAS:* 4.1.4.

---

## Faza 3: Modelowanie Dynamiczne (Wybrany Nietrywialny Przypadek)

### Krok 4: Scenariusz i Diagram Aktywności
**Działania:**
1. Rozpisanie krok po kroku (wypunktowany tekst) wybranego w Kroku 2 UC: **"Stwórz Raport Skautingowy"**.
   - Aktor wybiera `Delegację` -> Aktor wybiera `Mecz` -> Skaut widzi statystyki i przypisuje oceny -> Wpisuje wniosek -> Zapisuje -> System odpala aktualizację Big Board.
2. Przeniesienie tekstu na **Diagram Aktywności**, uwzględniający instrukcje warunkowe (np. "Czy ocena >= 7? Jeśli tak, rozważ status obserwacji").
*Odpowiada punktom MAS:* 4.1.5, 4.1.7.

### Krok 5: Analiza Stanów
**Działania:**
1. Skupienie się na wybranej klasie (np. `Zawodnik` i jego atrybucie `statusProfilu`).
2. Narysowanie **Diagramu Stanu** z przejściami zdefiniowanymi w dokumentacji: 
   - `NOWY` → `OBSERWOWANY` → `WERYFIKACJA_MEDYCZNA` → `ZAPROSZONY_NA_WORKOUT` → `NA_BIG_BOARD` → `SKRESLONY`.
3. Opisanie warunków przejść (ang. guards), np. przejście z *Nowy* do *Obserwowany* wymaga > 0 raportów.
*Odpowiada punktom MAS:* 4.1.7.

---

## Faza 4: Decyzje i Implementacja Architektury

### Krok 6: Decyzje Projektowe i Skutki Analizy Dynamicznej
**Działania:**
1. **Analiza dynamiczna (wnioski):** Zapisanie konkretnych skutków modelowania dynamicznego. Odkrycie jakich metod brakuje (np. metody `recalculateAverages()` po dodaniu Raportu, metody `updateStatus()` podczas procesowania Diagramu Stanu).
2. **Decyzje projektowe:** Opisanie jak abstrakcja UML "Klasa Asocjacyjna" (np. `StatystykaMeczowa`) została fizycznie zaimplementowana jako encja z dwoma relacjami Many-To-One do `Zawodnika` i do `Meczu`. Jak zrealizowane zostanie dziedziczenie `Osoba`->`Skaut`/`Zawodnik` (np. za pomocą strategii bazy danych JOINED lub SINGLE_TABLE).
*Odpowiada punktom MAS:* 4.1.8, 4.1.12.

### Krok 7: Projektowy Diagram Klas
**Działania:**
1. Wykorzystanie diagramu z Kroku 3 i uzupełnienie go o:
   - Kolekcje obiektowe i typy znane językowi (Java).
   - Metody i konstrukcje wynikające z Kroku 6.
   - Usunięcie abstrakcji nierozumianych przez kod (np. zamiana klasy asocjacyjnej z symbolem przerywanej linii na normalną klasę powiązaną asocjacjami).
   - Wskazanie modyfikatorów dostępu, hermetyzacji.
*Odpowiada punktom MAS:* 4.1.9.

---

## Faza 5: Użyteczność i Finalizacja

### Krok 8: Projekt Interfejsu (GUI) - Skupienie na Usability
**Działania:**
1. Opracowanie makiety (widoku) dla wybranego przypadku ("Stwórz Raport").
2. Realizacja wymagania asocjacyjnego z MAS: Widok Master-Detail. Lewy panel wyświetla listę `Delegacji`. Użytkownik klika w jedną z nich, a prawy panel ładuje powiązane po asocjacji (1..*) encje `Meczów`. Dopiero po kliknięciu w Mecz, ładuje się lista `Zawodnikow`.
3. Dopasowanie GUI do biblioteki React+Material UI (MUI), zwrócenie uwagi na ergonomię.
*Odpowiada punktom MAS:* 4.1.6, 4.2.3, 4.2.6.

### Krok 9: Kompletacja, Formatowanie i Eksport
**Działania:**
1. Przeniesienie wszystkich zebranych punktów i grafik w logicznej ciągłości.
2. **Kontrola jakości grafik:** Upewnienie się, że diagramy klas i aktywności mieszczą się w całości na wirtualnej stronie A4 bez "dzielenia na części" oraz że czcionka jest czytelna przy podglądzie wielkości 100%. Diagramy muszą używać tylko formatów bezstratnych (PNG) lub wektorowych.
3. Wygenerowanie na sam koniec pliku **PDF**.
4. Sprawdzenie wymogu z nazwą pliku: `MAS_Grupa_Nazwisko_Imię_NrIndeksu.PDF`.
*Odpowiada punktom MAS:* 4.1.10, 4.1.11.

---
*Back to [[ScoutForce NBA]]*