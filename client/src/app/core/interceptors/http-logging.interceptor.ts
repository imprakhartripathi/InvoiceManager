import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { LoggerService } from '../services/logger.service';

@Injectable()
export class HttpLoggingInterceptor implements HttpInterceptor {
  constructor(private readonly logger: LoggerService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const startedAt = performance.now();
    this.logger.info(`HTTP ${req.method} ${req.urlWithParams} - started`);

    return next.handle(req).pipe(
      tap((event) => {
        if (event instanceof HttpResponse) {
          const ms = Math.round(performance.now() - startedAt);
          this.logger.info(`HTTP ${req.method} ${req.urlWithParams} - ${event.status} (${ms} ms)`);
        }
      }),
      catchError((err: unknown) => {
        const ms = Math.round(performance.now() - startedAt);
        if (err instanceof HttpErrorResponse) {
          this.logger.error(`HTTP ${req.method} ${req.urlWithParams} - ${err.status} (${ms} ms)`, {
            statusText: err.statusText,
            message: err.message,
            error: err.error
          });
        } else {
          this.logger.error(`HTTP ${req.method} ${req.urlWithParams} - unknown error (${ms} ms)`, err);
        }
        return throwError(() => err);
      })
    );
  }
}
