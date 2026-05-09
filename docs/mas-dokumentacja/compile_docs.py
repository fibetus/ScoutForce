import subprocess
import os
import sys

def main():
    # Ustalamy katalog roboczy na ten, w którym znajduje się skrypt
    working_dir = os.path.dirname(os.path.abspath(__file__))
    
    input_file = "main.typ"
    # Zgodnie z wymaganiami MAS, odpowiedni format nazwy pliku:
    output_file = "MAS_16c_Betlej_Filip_s30331.pdf"
    
    print(f"Rozpoczynam kompilacje {input_file} do {output_file}...")
    
    try:
        # Wywołanie komendy typst compile
        result = subprocess.run(
            ["typst", "compile", input_file, output_file],
            cwd=working_dir,
            check=True,
            capture_output=True,
            text=True
        )
        print("Kompilacja zakonczona sukcesem!")
        print(f"Plik wygenerowany: {os.path.join(working_dir, output_file)}")
    except subprocess.CalledProcessError as e:
        print("Wystapil blad podczas kompilacji!", file=sys.stderr)
        print(e.stderr, file=sys.stderr)
        sys.exit(1)
    except FileNotFoundError:
        print("Nie znaleziono polecenia 'typst'. Upewnij sie, ze Typst jest zainstalowany i dodany do zmiennej srodowiskowej PATH.", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
