export interface Comment {
	id: number;
	discussionId: number;
	parentId: number;
	topParentId: number;
	text: string;
	pathLength: number;
	numOfParents: number;
	numOfChildren: number;
	created: number;
	embedded: Array<Comment>;
	breadcrumbs: Array<number>;
}