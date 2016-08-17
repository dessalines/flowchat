import {Tag} from './tag.interface.ts';
import {User} from './user.interface.ts';

export interface Community {
  id: number;
  name: string;
  text?: string;
  private_?: boolean;
  creator: User;
  moderators?: Array<User>;
  privateUsers?: Array<User>;
  blockedUsers?: Array<User>;
  deleted?: boolean;
  created: number;
  modified?: number;
}