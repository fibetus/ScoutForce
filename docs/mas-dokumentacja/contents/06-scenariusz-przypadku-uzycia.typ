= Scenariusz przypadku użycia

Wymagany dla przynajmniej jednego nietrywialnego przypadku użycia, który odwołuje się do innego przypadku użycia (użycie relacji `«include»` / `«extend»`). Sporządzony w postaci wypunktowanego tekstu.

// Przykład:
// **Nazwa:** Dodanie nowego raportu skautingowego
// **Aktor główny:** Skaut
// **Warunki początkowe:** Skaut jest zalogowany do systemu.
// 
// **Scenariusz główny:**
// 1. Skaut wybiera opcję "Dodaj raport".
// 2. System wyświetla formularz raportu (odwołanie do `«include»` "Wyszukaj zawodnika").
// 3. Skaut wypełnia dane i zatwierdza formularz.
// ...
