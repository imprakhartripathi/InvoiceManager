import { Component, OnInit, signal } from '@angular/core';

import { TemplateDto, TemplateService } from '../../template.service';

@Component({
  standalone: false,
  selector: 'app-template-list',
  templateUrl: './template-list.component.html',
  styleUrls: ['./template-list.component.scss']
})
export class TemplateListComponent implements OnInit {
  readonly templates = signal<TemplateDto[]>([]);
  loading = false;
  error = '';

  constructor(private readonly templateService: TemplateService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.templateService.list().subscribe({
      next: (data) => {
        this.error = '';
        this.templates.set(data);
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load templates.';
        this.loading = false;
      }
    });
  }

  deleteTemplate(template: TemplateDto): void {
    if (template.inUse) {
      this.error = 'This template is in use by existing invoices and cannot be deleted.';
      return;
    }

    const confirmed = window.confirm(`Delete template "${template.name}"?`);
    if (!confirmed) {
      return;
    }

    this.templateService.delete(template.id).subscribe({
      next: () => this.refresh(),
      error: (err) => {
        this.error = err?.error?.message ?? 'Unable to delete template.';
      }
    });
  }
}
