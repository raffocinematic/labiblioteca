import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormBuilder, FormControl, FormGroup, ReactiveFormsModule,
  ValidationErrors, Validators } from '@angular/forms';

import { Book, BookRequest } from '../../core/models/book.model';
import { BookApiService } from '../../core/services/book-api.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-book-list',
  imports: [PageHeaderComponent, ReactiveFormsModule],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.scss'
})
export class BookListComponent implements OnInit {
  protected readonly books = signal<Book[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly editingBookId = signal<number | null>(null);

  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(20);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly sort = signal('title');

  protected readonly bookForm;

  protected readonly searchForm!: FormGroup<{
    title: FormControl<string | null>;
    author: FormControl<string | null>;
    isbn: FormControl<string | null>;
    publicationYear: FormControl<number | null>;
  }>;

  constructor(
    private readonly bookApiService: BookApiService,
    private readonly formBuilder: FormBuilder
  ) {
    this.bookForm = this.formBuilder.nonNullable.group({
      title: ['', [Validators.required, Validators.maxLength(255)]],
      author: ['', [Validators.required, Validators.maxLength(255)]],
      isbn: ['', [Validators.required, this.isbnValidator]],
      publicationYear: this.formBuilder.control<number | null>(null, [
        Validators.min(0),
        Validators.max(9999)
      ]),
      totalCopies: [0, [Validators.required, Validators.min(0)]],
      availableCopies: [0, [Validators.required, Validators.min(0)]]
    }, {
      validators: this.availableCopiesCannotExceedTotalCopies
    });

    this.searchForm = this.formBuilder.group({
      title: [''],
      author: [''],
      isbn: [''],
      publicationYear: this.formBuilder.control<number | null>(null)
    });
  }

  ngOnInit(): void {
    this.loadBooks();
  }

  protected loadBooks(page = this.currentPage()): void {
    this.loading.set(true);
    this.error.set(null);

    this.bookApiService.getBooks(page, this.pageSize(), this.sort()).subscribe({
      next: (response) => {
        this.books.set(response.content);
        this.currentPage.set(response.page);
        this.pageSize.set(response.size);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossibile caricare i libri.');
        this.loading.set(false);
      }
    });
  }
// La pagina corrente è stato del componente, i bottoni non devon calcolare HTTP, devono solo chiedere al componente
// di caricare una pagina diversa.
protected goToPreviousPage(): void {
  if (this.currentPage() === 0) {
    return;
  }

  this.loadBooks(this.currentPage() - 1);
}

protected goToNextPage(): void {
  if (this.currentPage() >= this.totalPages() - 1) {
    return;
  }

  this.loadBooks(this.currentPage() + 1);
}

  protected saveBook(): void {
    if (this.bookForm.invalid) {
      this.bookForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const request: BookRequest = this.bookForm.getRawValue();
    const editingId = this.editingBookId();

    const operation = editingId === null
      ? this.bookApiService.createBook(request)
      : this.bookApiService.updateBook(editingId, request);

    operation.subscribe({
      next: () => {
        this.resetForm();
        this.loadBooks();
        this.saving.set(false);
      },
      error: (error: unknown) => {
        this.error.set(this.getApiErrorMessage(error, 'Impossibile salvare il libro.'));
        this.saving.set(false);
      }
    });
  }

  protected editBook(book: Book): void {
    this.editingBookId.set(book.id);

    this.bookForm.setValue({
      title: book.title,
      author: book.author,
      isbn: book.isbn,
      publicationYear: book.publicationYear,
      totalCopies: book.totalCopies,
      availableCopies: book.availableCopies
    });
  }

  protected deleteBook(book: Book): void {
    const confirmed = window.confirm(`Vuoi eliminare "${book.title}"?`);

    if (!confirmed) {
      return;
    }

    this.bookApiService.deleteBook(book.id).subscribe({
      next: () => {
        this.loadBooks();
      },
      error: () => {
        this.error.set('Impossibile eliminare il libro.');
      }
    });
  }

  protected resetForm(): void {
    this.editingBookId.set(null);

    this.bookForm.reset({
      title: '',
      author: '',
      isbn: '',
      publicationYear: null,
      totalCopies: 0,
      availableCopies: 0
    });
  }

  protected searchBooks(): void {
    this.loading.set(true);
    this.error.set(null);

    this.bookApiService.searchBooks(this.searchForm.getRawValue()).subscribe({
      next: (books) => {
        this.books.set(books);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossibile cercare i libri.');
        this.loading.set(false);
      }
    });
  }

  protected resetSearch(): void {
    this.searchForm.reset({
      title: '',
      author: '',
      isbn: '',
      publicationYear: null
    });

    this.loadBooks(0);
  }

/*
Miglioriamo il messaggio di errore in modo che sia parlante.

Se il BE manda errori di validazione, mostri quelli specifici.
Se non ci sono validationErrors, continui a mostrare message.

Se non arriva nulla di utile, usi il fallback message 'Impossibile salvare il libro'

Il BE era correto, il problema era l'ordine di priorità nel FE, validationErrors deve venire prima di message.
*/
  private getApiErrorMessage(error: unknown, fallbackMessage: string): string {
    if (error instanceof HttpErrorResponse) {
      const validationErrors = error.error?.validationErrors;

      if (validationErrors && typeof validationErrors === 'object') {
        const messages = Object.values(validationErrors)
        .filter((value): value is string =>
        typeof value === 'string' && value.trim().length > 0
        );

      if (messages.length > 0) {
        return messages.join(' ');
        }
      }

     const message = error.error?.message;

        if (typeof message === 'string' && message.trim().length > 0) {
          return message;
        }
      }

      return fallbackMessage
  }

private availableCopiesCannotExceedTotalCopies(control: AbstractControl): ValidationErrors | null {
  const totalCopies = control.get('totalCopies')?.value;
  const availableCopies = control.get('availableCopies')?.value;

  if (totalCopies === null || availableCopies === null) {
    return null;
  }

  return availableCopies <= totalCopies
    ? null
    : { availableCopiesExceedTotalCopies: true };
}

private isbnValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value;

  if (typeof value !== 'string' || value.trim().length === 0) {
    return null;
  }

  const isbn = value.replace(/[\s-]/g, '').toUpperCase();

  return this.isValidIsbn10(isbn) || this.isValidIsbn13(isbn)
    ? null
    : { invalidIsbn: true };
}

private isValidIsbn10(isbn: string): boolean {
  if (!/^\d{9}[\dX]$/.test(isbn)) {
    return false;
  }

  let sum = 0;

  for (let i = 0; i < 10; i++) {
    const char = isbn.charAt(i);
    const value = char === 'X' ? 10 : Number(char);
    sum += value * (10 - i);
  }

  return sum % 11 === 0;
}

private isValidIsbn13(isbn: string): boolean {
  if (!/^\d{13}$/.test(isbn)) {
    return false;
  }

  let sum = 0;

  for (let i = 0; i < 13; i++) {
    const value = Number(isbn.charAt(i));
    sum += i % 2 === 0 ? value : value * 3;
  }

  return sum % 10 === 0;
}

}
