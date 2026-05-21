= Wymagania niefunkcjonalne

Wymagania niefunkcjonalne dla systemu ScoutForce sformułowano w sposób mierzalny i realistycznie weryfikowalny.

== Wydajnościowe

1. *Czas odpowiedzi dla operacji odczytu:* dla typowych zapytań (wyświetlenie listy zawodników, listy raportów skautingowych, listy delegacji) system zwraca odpowiedź w czasie *poniżej 2 sekund* przy zbiorze do 1000 rekordów w bazie danych.

2. *Czas zapisu raportu skautingowego:* zapis nowego raportu skautingowego wraz z ocenami szczegółowymi i aktualizacją statusu zawodnika trwa *poniżej 1 sekundy*.

3. *Obsługa równoległych użytkowników:* system poprawnie obsługuje co najmniej *10 jednoczesnych sesji użytkowników* bez utraty spójności danych.

4. *Wydajność obliczeń pochodnych:* wyliczenie atrybutów pochodnych (`/averageRating` zawodnika, `/finalRating` raportu, `/cost` delegacji) odbywa się w czasie *poniżej 200 ms* dla zawodnika z maksymalnie 100 raportami.

== Niezawodnościowe

5. *Transakcyjność zapisów:* wszystkie operacje zmieniające stan systemu (utworzenie raportu, zmiana statusu zawodnika, utworzenie delegacji) muszą być atomowe - przerwanie operacji nie pozostawia bazy w niespójnym stanie.

6. *Walidacja danych po stronie serwera oraz klienta:* każda operacja zapisu jest poprzedzona walidacją (np. suma wag ocen szczegółowych równa 1.0, unikatowość pól `email` i `license_number`, poprawność dat w delegacji). Nieprawidłowe dane są odrzucane z czytelnym komunikatem błędu.

== Bezpieczeństwa

7. *Autoryzacja w oparciu o role:* system rozróżnia co najmniej dwie role - `Skaut` oraz `Dyrektor klubu`. Tworzenie delegacji jest dostępne wyłącznie dla roli `Dyrektor klubu`.

8. *Ochrona danych w transporcie:* komunikacja klient - serwer w środowisku produkcyjnym odbywa się przez *HTTPS*.

== Użyteczności

9. *Nawigacja:* każda funkcjonalność wymieniona w wymaganiach użytkownikajest dostępna w maksymalnie 3 kliknięciach od ekranu głównego.

== Przenośności

10. *Środowisko docelowe:* system działa na systemach operacyjnych *Windows 10+*, *Linux (Ubuntu 22.04+)*, *macOS 13+*, *Android 14+*, *iOS 18+*.

== Skalowalności i architektury

11. *Niezależność warstw:* warstwa prezentacji komunikuje się z backendem wyłącznie przez zdefiniowane API REST — bez bezpośredniego dostępu do bazy.
