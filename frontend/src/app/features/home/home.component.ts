import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Book } from '../../core/models/book.model';
import { AuthService } from '../../core/services/auth.service';
import { BookApiService } from '../../core/services/book-api.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  protected readonly books = signal<Book[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly recentBooks = computed(() => this.books().slice(0, 5));

  protected readonly stats = computed(() => {
    const books = this.books();

    return {
      titles: books.length,
      totalCopies: books.reduce((total, book) => total + book.totalCopies, 0),
      availableCopies: books.reduce((total, book) => total + book.availableCopies, 0),
      unavailableTitles: books.filter((book) => book.availableCopies === 0).length
    };
  });

  constructor(
    protected readonly authService: AuthService,
    private readonly bookApiService: BookApiService
  ) {
  }

  ngOnInit(): void {
    this.loadBooks();
  }

  protected loadBooks(): void {
    this.loading.set(true);
    this.error.set(null);

    this.bookApiService.getBooks(0, 5, 'id,desc').subscribe({
      next: (response) => {
        this.books.set(response.content);
        this.loading.set(false);
      },

      error: () => {
        this.error.set('Impossibile caricare il riepilogo del catalogo.');
        this.loading.set(false);
      }
    });
  }
}
