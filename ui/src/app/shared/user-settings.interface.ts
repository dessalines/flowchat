export interface UserSettings {
	defaultViewTypeRadioValue: string;
	defaultSortTypeRadioValue: string;
	defaultCommentSortTypeRadioValue: string;
	readOnboardAlert: boolean;
	theme: Theme;
}

export enum Theme {
	Dark, Light
}