import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { Book, BookRequest } from '../models/book.model';

@Injectable({
  providedIn: 'root'
})
export class BookApiService {
  private readonly booksUrl = `${API_BASE_URL}/catalog/books`;

  constructor(private readonly http: HttpClient) {
    }

   getBooks(): Observable<Book[]> {
      return this.http.get<Book[]>(this.booksUrl);
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
}
