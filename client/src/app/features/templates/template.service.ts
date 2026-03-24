import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { TemplateField } from '../../models/template-field.model';
import { ApiService } from '../../core/services/api.service';

export interface TemplateDto {
  id: string;
  userId: string;
  name: string;
  fields: TemplateField[];
  hasLineItems: boolean;
}

@Injectable({ providedIn: 'root' })
export class TemplateService {
  constructor(private readonly api: ApiService) {}

  list(): Observable<TemplateDto[]> {
    return this.api.get<TemplateDto[]>('/api/templates');
  }

  create(payload: { name: string; fields: TemplateField[]; hasLineItems: boolean }): Observable<TemplateDto> {
    return this.api.post<TemplateDto>('/api/templates', payload);
  }

  getOne(id: string): Observable<TemplateDto> {
    return this.api.get<TemplateDto>(`/api/templates/${id}`);
  }
}
