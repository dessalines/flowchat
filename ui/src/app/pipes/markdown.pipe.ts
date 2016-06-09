import { Pipe, PipeTransform } from '@angular/core';
import * as markdown_it from 'markdown-it';

@Pipe({
  name: 'markdown'
})
export class MarkdownPipe implements PipeTransform {

  transform(value: string): any {
    return markdown_it().render(value);
  }

}
