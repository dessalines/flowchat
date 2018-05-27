import { Pipe, PipeTransform } from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
// import * as markdown_it from 'markdown-it'; TODO
import {Tools} from '../shared/tools';

declare var markdownitEmoji: any;
var MarkdownIt = require('markdown-it');

@Pipe({
  name: 'markdown',
})
export class MarkdownPipe implements PipeTransform {

  private markdownIt: any;

  constructor(private sanitizer: DomSanitizer) {
    this.markdownIt = new MarkdownIt();
    this.markdownIt.use(markdownitEmoji);
  }

  transform(value: string, link: boolean = false): any {
    let out: string = value;
    if (link) {
      out = Tools.linkReplacements(out);
    } else {
      out = Tools.linkReplacements(out);
      out = this.markdownIt.render(out);
    }
    return this.sanitizer.bypassSecurityTrustHtml(out);
  }

}
