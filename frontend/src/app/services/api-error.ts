import { HttpErrorResponse } from '@angular/common/http';

export function toErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Cannot reach the server. Is the backend running?';
    }
    return err.error?.detail ?? `Request failed (${err.status})`;
  }
  return 'Unexpected error';
}
