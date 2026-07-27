import { PageResponse } from './book.model';

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';

export interface AppUser {
  id: number;
  username: string;
  role: UserRole;
  createdAt: string;
}

export interface UpdateUserRoleRequest {
  role: UserRole;
}

export type UserPageResponse = PageResponse<AppUser>;
