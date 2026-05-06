# ScoutNBA - Wymagania Dokumentacyjne (MAS)

Na podstawie punktu 4.1 "Dokumentacja" oraz wytycznych z pliku organizacyjnego MAS, poniżej znajduje się szczegółowa analiza i lista wszystkich wymagań dokumentacyjnych do spełnienia w ramach projektu końcowego ScoutForce NBA.

## 1. Wymagania Organizacyjne i Wstępne
- **Skomplikowanie dziedziny biznesowej:** Projekt musi obejmować **12 - 15** sensownych klas biznesowych.
- **Format i nazewnictwo pliku:** Całość dokumentacji musi być oddana w postaci **pojedynczego pliku PDF**. Plik musi zostać nazwany według wzoru: `MAS_Grupa_Nazwisko_Imię_NrIndeksu.PDF` i wysłany na adres mailowy prowadzącego ćwiczenia w wyznaczonym terminie.

## 2. Zawartość Dokumentacji Projektowej
Dokumentacja musi obligatoryjnie składać się z następujących elementów:

1. **Wymagania użytkownika**
   - Przedstawione w postaci tzw. „historyjki” rozpisanej w punktach (odzwierciedlające działania np. Skauta, Managera Klubu).
2. **Diagramy przypadków użycia**
   - Opracowane na podstawie wymagań użytkownika, odzwierciedlające **wymagania funkcjonalne**. Ewentualnie mogą zostać uzupełnione postacią tekstową.
3. **Wymagania niefunkcjonalne**
   - Wypisane w formie tekstowej, w punktach (np. wymagania wydajnościowe, technologiczne).
4. **Analityczny diagram klas**
   - Niezwykle istotny element, na którym bazują dalsze prace. **Nie może zawierać błędów**. Służy jako fundament do przygotowania projektowego diagramu klas.
5. **Projektowy (implementacyjny) diagram klas**
   - Budowany wyłącznie na podstawie analitycznego diagramu klas (tylko wynikające z niego elementy, bez wprowadzania "nowych" zjawisk niewynikających z modelu).
   - Jest znacznie **uszczegółowiony**.
   - **Zamiana konstrukcji:** Wszystkie konstrukcje pojęciowe nieistniejące w języku programowania (Java) muszą zostać zamienione zgodnie z podjętymi decyzjami projektowymi (np. w jaki sposób zrealizowano kompozycję czy asocjacje wiele do wielu).
   - Uzupełniony o **metody wynikłe z analizy dynamicznej**.
6. **Scenariusz przypadku użycia**
   - Wymagany dla przynajmniej jednego **nietrywialnego** przypadku użycia, który odwołuje się do innego przypadku użycia (użycie relacji `«include»` / `«extend»`).
   - Sporządzony w postaci wypunktowanego tekstu.
7. **Diagram aktywności**
   - Stworzony dla tego samego wybranego nietrywialnego przypadku użycia (graficzna forma scenariusza).
8. **Diagram stanu dla klasy**
   - Wykonywany w ramach analizy dynamicznej **dla konkretnej klasy** (np. `Zawodnik` dla zmiany jego statusów), a nie całego systemu.
   - W miarę możliwości powinien dotyczyć klasy, która odgrywa istotną rolę w analizowanym przypadku użycia.
9. **Projekt interfejsu użytkownika (GUI)**
   - Oparty na opisywanym nietrywialnym przypadku użycia. Należy go sporządzić zgodnie z wytycznymi dotyczącymi *usability* podanymi na wykładzie.
10. **Omówienie decyzji projektowych i skutków analizy dynamicznej**
    - Jawne omówienie skutków analizy dynamicznej na architekturę. Należy udokumentować, jakie nowe asocjacje, atrybuty, czy metody (skutki) powstały i umieścić je następnie na implementacyjnym diagramie klas.
    - Opis decyzji projektowych z przykładami wskazującymi odpowiednie fragmenty diagramów (np. dlaczego zrealizowano daną ekstensję klasy w konkretny sposób).

## 3. Wytyczne do Tworzenia Diagramów (Wymóg Czytelności)
Każdy użyty w dokumencie diagram musi ściśle spełniać wymogi wizualne, gdyż **prace z nieczytelnymi diagramami nie będą w ogóle sprawdzane**:
- Prawidłowe stosowanie notacji UML (nie akceptuje się elementów "podobnych" lub innej, dowolnej grafiki).
- Każdy diagram musi być **podpisany**.
- Wykorzystanie dedykowanego narzędzia (np. UMLet).
- **Rozmiar czcionek:** Czytelny bez powiększania. Przy podglądzie dokumentu PDF w 100% na formacie A4 czcionki muszą być swobodnie widoczne.
- **Zakaz dzielenia diagramów** na osobne strony/części.
- Właściwe rozplanowanie: dziedziczenie prowadzone w pionie, asocjacje w poziomie, zapobieganie przecinaniu się linii łączących. Wszystkie asocjacje muszą posiadać nazwy i liczności.
- Diagramy powinny być w **formacie wektorowym** (np. Enhanced Metafile) lub formacie z bezstratną kompresją, takim jak PNG.

## 4. System Oceniania Części Dokumentacyjnej
Łącznie do zdobycia jest **100 punktów**. Podstawowym warunkiem oceny jest spełnienie wymagań ujętych w pkt. 4.1 dokumentu (w tym poprawnego formatu i czytelności):
- Skomplikowanie dziedziny biznesowej: **10 pkt**
- Udokumentowanie przypadku użycia (scenariusz i diagram): **10 pkt**
- Poprawność i złożoność projektowego diagramu klas: **35 pkt**
- Poprawność i złożoność diagramu aktywności: **10 pkt**
- Poprawność i złożoność diagramu stanu: **10 pkt**
- Projekt GUI: **10 pkt**
- Omówienie decyzji projektowych: **10 pkt**
- Czytelność i organizacja dokumentu: **5 pkt**

---
*Back to [[ScoutForce NBA]]*