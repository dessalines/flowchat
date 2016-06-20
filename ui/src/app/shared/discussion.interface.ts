import {Tag} from './tag.interface.ts';

export interface Discussion {
  id: number;
  userId: number;
  userName: string;
  title: string;
  link?: string;
  text?: string;
  avgRank?: number;
  userRank?: number;
  numberOfVotes?: number;
  tags?: Array<Tag>
  created: number;
  modified?: number;
}