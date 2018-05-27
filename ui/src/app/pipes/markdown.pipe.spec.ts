import { TestBed, async } from '@angular/core/testing';
import { MarkdownPipe } from './markdown.pipe';

describe('Pipe: Moment', () => {
  it('create an instance', () => {
    let pipe = new MarkdownPipe(null);
    expect(pipe).toBeTruthy();
  });
});
