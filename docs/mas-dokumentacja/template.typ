#let project(
  title: "",
  authors: (),
  group: "",
  date: none,
  body
) = {
  // Metadane dokumentu
  set document(author: authors, title: title)

  // Ustawienia strony
  set page(
    paper: "a4",
    margin: (left: 25mm, right: 25mm, top: 25mm, bottom: 25mm),
    numbering: "1",
    number-align: center,
  )

  // Ustawienia tekstu
  set text(
    font: "New Computer Modern", // Możesz zmienić na "Arial", "Times New Roman" itp.
    size: 11pt,
    lang: "pl"
  )

  // Formatowanie akapitów
  set par(justify: true, leading: 0.65em)

  // Stylizacja nagłówków
  set heading(numbering: "1.1")
  show heading: it => {
    v(1em)
    it
    v(0.5em)
  }

  // Strona tytułowa
  align(center)[
    #v(4cm)
    #text(size: 2.5em, weight: "bold", title)
    
    #v(2cm)
    #text(size: 1.5em, [Grupa: #group])
    
    #v(1cm)
    #for author in authors [
      #text(size: 1.3em, author) \
    ]
    
    #v(1fr)
    #if date != none {
      text(size: 1.2em, date)
    }
  ]
  pagebreak()

  // Spis treści
  outline(title: "Spis Treści", depth: 3, indent: true)
  pagebreak()

  // Ciało dokumentu
  body
}
