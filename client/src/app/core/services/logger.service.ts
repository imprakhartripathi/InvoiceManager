import { Injectable } from '@angular/core';

import { environment } from '../../environments/environment';

type LogLevel = 'debug' | 'info' | 'warn' | 'error';

@Injectable({ providedIn: 'root' })
export class LoggerService {
  private readonly enabled = !environment.production;

  debug(message: string, meta?: unknown): void {
    this.log('debug', message, meta);
  }

  info(message: string, meta?: unknown): void {
    this.log('info', message, meta);
  }

  warn(message: string, meta?: unknown): void {
    this.log('warn', message, meta);
  }

  error(message: string, meta?: unknown): void {
    this.log('error', message, meta);
  }

  private log(level: LogLevel, message: string, meta?: unknown): void {
    if (!this.enabled) {
      return;
    }

    const ts = new Date().toISOString();
    const prefix = `[${ts}] [${level.toUpperCase()}]`;

    if (meta === undefined) {
      console[level](`${prefix} ${message}`);
      return;
    }

    console[level](`${prefix} ${message}`, meta);
  }
}
