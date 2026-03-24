import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SmtpSettingsComponent } from './pages/smtp-settings/smtp-settings.component';

const routes: Routes = [{ path: '', component: SmtpSettingsComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule {}
