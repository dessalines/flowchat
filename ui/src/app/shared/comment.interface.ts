import {User} from './';

export interface Comment {
	id: number;
	user: User;
	modifiedByUser: User;
	discussionId: number;
	parentId: number;
	topParentId: number;
	text: string;
	pathLength: number;
	numOfParents: number;
	numOfChildren: number;
	created: number;
	modified?: number;
	embedded: Array<Comment>;
	breadcrumbs: Array<number>;
	avgRank?: number;
	userRank?: number;
	numberOfVotes?: number;
	deleted?: boolean;
}
