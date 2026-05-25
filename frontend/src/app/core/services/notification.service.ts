import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { NotificationDto } from '../../shared/models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiService = inject(ApiService);
  private readonly ENDPOINT = '/notifications';

  getNotifications(): Observable<NotificationDto[]> {
    return this.apiService.get<NotificationDto[]>(this.ENDPOINT);
  }
}
