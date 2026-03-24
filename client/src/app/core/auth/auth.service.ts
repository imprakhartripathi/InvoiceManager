import { Injectable, signal } from '@angular/core';
import { tap } from 'rxjs/operators';

import { ApiService } from '../services/api.service';
import { TokenService } from '../services/token.service';

interface AuthResponse {
  token: string;
  userId: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<{ id: string; email: string } | null>(null);

  constructor(
    private readonly api: ApiService,
    private readonly token: TokenService
  ) {}

  login(payload: { email: string; password: string }) {
    return this.api.post<AuthResponse>('/auth/login', payload).pipe(
      tap((res) => {
        this.token.set(res.token);
        this.user.set({ id: res.userId, email: res.email });
      })
    );
  }

  signup(payload: { email: string; password: string }) {
    return this.api.post<AuthResponse>('/auth/signup', payload);
  }

  isAuthenticated(): boolean {
    return !!this.token.get();
  }

  logout(): void {
    this.token.clear();
    this.user.set(null);
  }
}
