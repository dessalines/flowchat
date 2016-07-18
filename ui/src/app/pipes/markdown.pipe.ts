import { Pipe, PipeTransform } from '@angular/core';
import * as markdown_it from 'markdown-it';
import {Tools} from '../shared/tools';

@Pipe({
  name: 'markdown',
})
export class MarkdownPipe implements PipeTransform {

  private md: markdown_it.MarkdownIt = markdown_it();

  transform(value: string): any {
    console.log('md pipe');
    // return Tools.markdownReplacements(markdown_it().render(value));
    return this.md.render(value);
  }

}
