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
}
