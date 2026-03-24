import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { RouterLoggerService } from './core/services/router-logger.service';

@Component({
  standalone: true,
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App {
  constructor(private readonly routerLogger: RouterLoggerService) {
    void this.routerLogger;
  }
}
