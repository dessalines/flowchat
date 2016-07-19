import { Pipe, PipeTransform } from '@angular/core';
import * as markdown_it from 'markdown-it';

declare var markdownitEmoji: any;

@Pipe({
  name: 'markdown'
})
export class MarkdownPipe implements PipeTransform {

  private markdownIt: markdown_it.MarkdownIt;

  constructor() {
    this.markdownIt = markdown_it();
    this.markdownIt.use(markdownitEmoji);
  }

  transform(value: string): any {
    return this.markdownIt.render(value);
  }

}
