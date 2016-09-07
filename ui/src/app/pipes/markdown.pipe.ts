import { Pipe, PipeTransform } from '@angular/core';
import {DomSanitizationService, SafeHtml} from '@angular/platform-browser';
// import * as markdown_it from 'markdown-it'; TODO
import {Tools} from '../shared/tools';

declare var markdownitEmoji: any;
var MarkdownIt = require('markdown-it');

@Pipe({
  name: 'markdown',
})
export class MarkdownPipe implements PipeTransform {

  private markdownIt: any;

  constructor(private sanitizer: DomSanitizationService) {
    this.markdownIt = new MarkdownIt();
    this.markdownIt.use(markdownitEmoji);
  }

  transform(value: string): any {
    return this.sanitizer.bypassSecurityTrustHtml(
      Tools.markdownReplacements(
        this.markdownIt.render(value)));
  }

}
