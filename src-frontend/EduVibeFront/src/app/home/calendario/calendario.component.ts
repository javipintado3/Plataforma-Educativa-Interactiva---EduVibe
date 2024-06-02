import { Component } from '@angular/core';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import esLocale from '@fullcalendar/core/locales/es';


@Component({
  selector: 'app-calendario',
  templateUrl: './calendario.component.html',
  styleUrls: ['./calendario.component.css'] // 'styleUrl' debería ser 'styleUrls'
})
export class CalendarioComponent {

  calendarOptions: CalendarOptions = {
    initialView: 'dayGridMonth',
    plugins: [dayGridPlugin],
    locale: esLocale,
    themeSystem: 'standard', // Cambia al tema estándar
    eventColor: '#2D9596', // Cambia el color de los eventos

  };
}
