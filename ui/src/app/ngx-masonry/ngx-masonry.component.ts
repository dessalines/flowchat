import {
	Component,
	OnInit,
	OnDestroy,
	Input,
	Output,
	ElementRef,
	EventEmitter,
	PLATFORM_ID,
	Inject,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

declare var require: any;
var imagesLoaded: any = undefined;
var masonryConstructor: any = undefined;

import { NgxMasonryOptions } from './ngx-masonry-options.interface';

@Component({
	selector: '[ngx-masonry], ngx-masonry',
	template: '<ng-content></ng-content>',
	styles: [`
		:host {
			display: block;
		}
	`],
})
export class NgxMasonryComponent implements OnInit, OnDestroy {
	constructor(
		@Inject(PLATFORM_ID) private platformId: any,
		private _element: ElementRef) {}
	
	public _msnry: any;

	// Inputs
	@Input() public options: NgxMasonryOptions;
	@Input() public useImagesLoaded: Boolean = false;

	// Outputs
	@Output() layoutComplete: EventEmitter<any[]> = new EventEmitter<any[]>();
	@Output() removeComplete: EventEmitter<any[]> = new EventEmitter<any[]>();

	ngOnInit() {
    		///TODO: How to load imagesloaded only if this.useImagesLoaded===true?
    		if (this.useImagesLoaded && imagesLoaded === undefined) {
      			imagesLoaded = require('imagesloaded');
    		}

		if (isPlatformBrowser(this.platformId) && masonryConstructor === undefined) {
		      masonryConstructor = require('masonry-layout');
	    	}

		// Create masonry options object
		if (!this.options) this.options = {};

		// Set default itemSelector
		if (!this.options.itemSelector) {
			this.options.itemSelector = '[ngx-masonry-item], ngx-masonry-item';
		}

		if (isPlatformBrowser(this.platformId)) {			
			// Initialize Masonry
			this._msnry = new masonryConstructor(this._element.nativeElement, this.options);

			// console.log('AngularMasonry:', 'Initialized');

			// Bind to events
			this._msnry.on('layoutComplete', (items: any) => {
				this.layoutComplete.emit(items);
			});
			this._msnry.on('removeComplete', (items: any) => {
				this.removeComplete.emit(items);
			});
		}
	}

	ngOnDestroy() {
		if (this._msnry) {
			this._msnry.destroy();
		}
	}

	public layout() {
		setTimeout(() => {
			this._msnry.layout();
		});

		// console.log('AngularMasonry:', 'Layout');
	}

	// public add(element: HTMLElement, prepend: boolean = false) {
	public add(element: HTMLElement) {
		var isFirstItem = false;

		// Check if first item
		if (this._msnry.items.length === 0) {
			isFirstItem = true;
		}

		if (this.useImagesLoaded) {
			imagesLoaded(element, (instance: any) => {
				this._element.nativeElement.appendChild(element);

				// Tell Masonry that a child element has been added
				this._msnry.appended(element);

				// layout if first item
				if (isFirstItem) this.layout();
			});

			this._element.nativeElement.removeChild(element);
		} else {
			// Tell Masonry that a child element has been added
			this._msnry.appended(element);

			// layout if first item
			if (isFirstItem) this.layout();
		}

		// console.log('AngularMasonry:', 'Brick added');
	}

	public remove(element: HTMLElement) {
		// Tell Masonry that a child element has been removed
		this._msnry.remove(element);

		// Layout items
		this.layout();

		// console.log('AngularMasonry:', 'Brick removed');
	}
}
