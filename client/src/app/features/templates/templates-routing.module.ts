import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TemplateBuilderComponent } from './pages/template-builder/template-builder.component';
import { TemplateListComponent } from './pages/template-list/template-list.component';

const routes: Routes = [
  { path: '', component: TemplateListComponent },
  { path: 'new', component: TemplateBuilderComponent },
  { path: 'edit/:id', component: TemplateBuilderComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TemplatesRoutingModule {}
