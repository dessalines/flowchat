import { Pipe, PipeTransform } from '@angular/core';
import {DomSanitizationService, SafeHtml} from '@angular/platform-browser';
import * as markdown_it from 'markdown-it';
import {Tools} from '../shared/tools';

declare var markdownitEmoji: any;

@Pipe({
  name: 'markdown',
})
export class MarkdownPipe implements PipeTransform {

  private markdownIt: markdown_it.MarkdownIt;

  constructor(private sanitizer: DomSanitizationService) {
    this.markdownIt = markdown_it();
    this.markdownIt.use(markdownitEmoji);
  }

  transform(value: string): any {
    return this.sanitizer.bypassSecurityTrustHtml(
      Tools.markdownReplacements(
        this.markdownIt.render(value)));
  }

}
