import {Tag} from './tag.interface.ts';
import {User} from './user.interface.ts';

export interface Community {
  id: number;
  name: string;
  text?: string;
  private_?: boolean;
  creator: User;
  modifiedByUser: User;
  moderators?: Array<User>;
  privateUsers?: Array<User>;
  blockedUsers?: Array<User>;
  avgRank?: number;
  userRank?: number;
  numberOfVotes?: number;
  tags?: Array<Tag>;
  deleted?: boolean;
  created: number;
  modified?: number;
}