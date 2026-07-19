/*
  Rappresenta un libro completo come arriva dal backend.
  Nel frontend viene usato per tipizzare liste, dettagli e risposte delle API.
 */

export interface Book {

  // Identificativo generato dal backend: serve per modifica e cancellazione.
  id: number;
  title: string;
  author: string;
  isbn: string;
  // Puo' essere null quando l'anno di pubblicazione non e' disponibile.
  publicationYear: number | null;
  // Numero totale di copie registrate in biblioteca.
  totalCopies: number;
  // Numero di copie attualmente disponibili per il prestito.
  availableCopies: number;

}

// Payload inviato dal frontend nelle richieste POST e PUT.
// E' uguale a Book, ma senza id perche' l'id non si inserisce nel form:
// in creazione lo genera il backend, in modifica viene passato nell'URL.
export type BookRequest = Omit<Book, 'id'>;

// Tipizzare i filtri rende il service leggibile.
export interface BookSearchFilters {
  title?: string | null;
  author?: string | null;
  isbn?: string | null;
  publicationYear?: number | null;
}

// Il BE ora non restituisce più Book[] ma un wrapper paginato. Il generic <T> replica lato TS lo stesso concetto
// del PageResponse<T> Java.
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
