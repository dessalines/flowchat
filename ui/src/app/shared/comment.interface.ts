export interface Comment {
	id: number;
	userId: number;
	userName: string;
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
}