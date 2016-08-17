import {Tag} from './tag.interface.ts';
import {User} from './user.interface.ts';

export interface Discussion {
  id: number;
  creator: User;
  title: string;
  link?: string;
  text?: string;
  avgRank?: number;
  userRank?: number;
  numberOfVotes?: number;
  tags?: Array<Tag>;
  private_?: boolean;
  privateUsers?: Array<User>;
  blockedUsers?: Array<User>;
  deleted?: boolean;
  created: number;
  modified?: number;
}