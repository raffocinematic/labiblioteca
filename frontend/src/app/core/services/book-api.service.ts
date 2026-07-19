import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { HttpParams } from '@angular/common/http';
import { Book, BookRequest, BookSearchFilters, PageResponse } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class BookApiService {
  private readonly booksUrl = `${API_BASE_URL}/catalog/books`;

  constructor(private readonly http: HttpClient) {
    }

    // Qui nel service traduciamo parametri frontend in query param HTTP, il componente non deve sapere come si
    // costruisce HttpParams.
   getBooks(page = 0, size = 20, sort = 'title'): Observable<PageResponse<Book>> {
     const params = new HttpParams()
       .set('page', page)
       .set('size', size)
       .set('sort', sort);

     return this.http.get<PageResponse<Book>>(this.booksUrl, { params });
   }


  createBook(book: BookRequest): Observable<Book> {
      return this.http.post<Book>(this.booksUrl, book);
    }

    updateBook(id: number, book: BookRequest): Observable<Book> {
      return this.http.put<Book>(`${this.booksUrl}/${id}`, book);
    }

    deleteBook(id: number): Observable<void> {
      return this.http.delete<void>(`${this.booksUrl}/${id}`);
    }

  searchBooks(filters: BookSearchFilters): Observable<Book[]> {
    let params = new HttpParams();

    if (filters.title?.trim()) {
      params = params.set('title', filters.title.trim());
    }

    if (filters.author?.trim()) {
      params = params.set('author', filters.author.trim());
    }

    if (filters.isbn?.trim()) {
      params = params.set('isbn', filters.isbn.trim());
    }

    if (filters.publicationYear !== null && filters.publicationYear !== undefined) {
      params = params.set('publicationYear', filters.publicationYear);
    }

    return this.http.get<Book[]>(`${this.booksUrl}/search`, { params });
  }
}
