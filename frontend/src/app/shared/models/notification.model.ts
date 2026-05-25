export interface NotificationDto {
  id: string;
  title: string;
  message: string;
  type: 'INFO' | 'WARNING' | 'ALERT';
  createdAt: string;
}
