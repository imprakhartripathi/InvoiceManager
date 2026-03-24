import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from '../../shared/shared.module';
import { SmtpSettingsComponent } from './pages/smtp-settings/smtp-settings.component';
import { SettingsRoutingModule } from './settings-routing.module';

@NgModule({
  declarations: [SmtpSettingsComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, SettingsRoutingModule]
})
export class SettingsModule {}
