import { Injectable } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router } from '@angular/router';

import { LoggerService } from './logger.service';

@Injectable({ providedIn: 'root' })
export class RouterLoggerService {
  constructor(
    private readonly router: Router,
    private readonly logger: LoggerService
  ) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.logger.info(`Route -> ${event.url}`);
      }
      if (event instanceof NavigationEnd) {
        this.logger.info(`Route settled -> ${event.urlAfterRedirects}`);
      }
      if (event instanceof NavigationCancel) {
        this.logger.warn(`Route canceled -> ${event.url}`, event.reason);
      }
      if (event instanceof NavigationError) {
        this.logger.error(`Route error -> ${event.url}`, event.error);
      }
    });
  }
}
