import {UserSettings} from './user-settings.interface';

export interface User {
  id: number;
  name: string;
  full_user_id?: number;
  login_id?: number;
  email?: string;
  created?: number;
  auth?: string;
  expire_time?: number;
  settings?: UserSettings;
}