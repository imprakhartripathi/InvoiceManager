import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared.module';
import { TemplateBuilderComponent } from './pages/template-builder/template-builder.component';
import { TemplateListComponent } from './pages/template-list/template-list.component';
import { TemplatesRoutingModule } from './templates-routing.module';

@NgModule({
  declarations: [TemplateListComponent, TemplateBuilderComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SharedModule, TemplatesRoutingModule]
})
export class TemplatesModule {}
