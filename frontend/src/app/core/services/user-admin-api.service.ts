import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { AppUser, UpdateUserRoleRequest, UserPageResponse, UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserAdminApiService {
  private readonly usersUrl = `${API_BASE_URL}/admin/users`;

  constructor(private readonly http: HttpClient) {
  }

  getUsers(): Observable<UserPageResponse> {
    return this.http.get<UserPageResponse>(this.usersUrl);
  }

  updateUserRole(id: number, role: UserRole): Observable<AppUser> {
    const request: UpdateUserRoleRequest = { role };

    return this.http.patch<AppUser>(`${this.usersUrl}/${id}/role`, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.usersUrl}/${id}`);
  }
}
