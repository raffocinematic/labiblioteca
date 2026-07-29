export interface AuditLog {
  id: number;
  action: AuditAction;
  actorUsername: string;
  entityType: string | null;
  entityId: number | null;
  details: Record<string, string>;
  occurredAt: string;
}

export type AuditAction =
  | 'BOOK_CREATED'
  | 'BOOK_UPDATED'
  | 'BOOK_DELETED'
  | 'LOAN_CREATED'
  | 'BOOK_RETURNED'
  | 'LOGIN_FAILED';
