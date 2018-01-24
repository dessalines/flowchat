import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

/* -- ngx-masonry -- */
import { NgxMasonryComponent } from './ngx-masonry.component';
import { NgxMasonryDirective } from './ngx-masonry.directive';

@NgModule({
	imports: [
		CommonModule
	],
	exports: [
		NgxMasonryComponent,
		NgxMasonryDirective
	],
	declarations: [
		NgxMasonryComponent,
		NgxMasonryDirective
	]
})
export class NgxMasonryModule {}
