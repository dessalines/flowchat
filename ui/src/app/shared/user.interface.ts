export interface User {
  id: number;
  name: number;
  full_user_id?: number;
  login_id?: number;
  email?: string;
  created?: number;
  auth?: string;
  expire_time?: number;
}