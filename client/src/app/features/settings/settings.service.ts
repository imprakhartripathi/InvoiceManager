import { Injectable } from '@angular/core';

import { ApiService } from '../../core/services/api.service';

export interface UserSmtpSettingsResponse {
  host: string;
  port: number;
  email: string;
  enabled: boolean;
}

export interface UserSmtpSettingsRequest {
  host: string;
  port: number;
  email: string;
  appPassword: string;
  enabled: boolean;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  constructor(private readonly api: ApiService) {}

  getSmtpSettings() {
    return this.api.get<UserSmtpSettingsResponse>('/api/user-settings');
  }

  upsertSmtpSettings(payload: UserSmtpSettingsRequest) {
    return this.api.put<UserSmtpSettingsResponse>('/api/user-settings', payload);
  }
}
