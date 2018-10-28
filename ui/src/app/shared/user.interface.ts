import { UserSettings } from './user-settings.interface';

export interface User {
  id: number;
  name: string;
  jwt: string;
  fullUser?: boolean;
  email?: string;
  created?: number;
  expire_time?: number;
  settings?: UserSettings;
}