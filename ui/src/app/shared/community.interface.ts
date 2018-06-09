import {Tag, User} from './';

export interface Community {
  id: number;
  name: string;
  text?: string;
  private_?: boolean;
  nsfw: boolean;
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
