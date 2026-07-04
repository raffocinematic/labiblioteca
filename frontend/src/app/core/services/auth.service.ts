import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly authUrl = `${API_BASE_URL}/auth`;
  private readonly tokenKey = 'biblioteca_token';
  private readonly usernameKey = 'biblioteca_username';
  private readonly roleKey = 'biblioteca_role';

  readonly currentUsername = signal<string | null>(localStorage.getItem(this.usernameKey));

  constructor(private readonly http: HttpClient) {
  }

// login e register chiamano il backend
//tap salva il token quando la chiamata va bene
//signal permette di mostrare username/logged state nella UI

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, request)
      .pipe(tap((response) => this.saveSession(response)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/register`, request)
      .pipe(tap((response) => this.saveSession(response)));
  }

// nota: local storage va bene per ora ma in produzione un JWT nel browser va ragionato meglio per rischio XSS

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.usernameKey);
    localStorage.removeItem(this.roleKey);
    this.currentUsername.set(null);
  }

  private saveSession(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    localStorage.setItem(this.usernameKey, response.username);
    localStorage.setItem(this.roleKey, response.role);
    this.currentUsername.set(response.username);
  }
}
