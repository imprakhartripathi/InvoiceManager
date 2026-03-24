import { Injectable } from '@angular/core';

import { ApiService } from './api.service';

export interface UserProfile {
  fullName: string;
  businessName: string;
  defaultDisplayName: string;
  savedCustomDisplayNames: string[];
}

export interface UserProfileUpdateRequest {
  fullName: string;
  businessName: string;
  defaultDisplayName: string;
  newCustomDisplayName?: string;
}

@Injectable({ providedIn: 'root' })
export class UserProfileService {
  constructor(private readonly api: ApiService) {}

  getMe() {
    return this.api.get<UserProfile>('/api/users/me');
  }

  updateMe(payload: UserProfileUpdateRequest) {
    return this.api.put<UserProfile>('/api/users/me', payload);
  }
}
