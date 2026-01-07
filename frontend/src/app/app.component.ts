import { Component } from '@angular/core';
import {BenefitsComponent} from "./components/benefits/benefits.component";

@Component({
  selector: 'app-root',
  imports: [BenefitsComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'beneficios-app';
}
