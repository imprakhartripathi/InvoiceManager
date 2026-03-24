import { NgModule, Optional, SkipSelf } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from '../shared/shared.module';
import { AppShellComponent } from './layout/app-shell/app-shell.component';

@NgModule({
  declarations: [AppShellComponent],
  imports: [RouterModule, SharedModule],
  exports: [AppShellComponent]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule should only be imported in AppModule.');
    }
  }
}
