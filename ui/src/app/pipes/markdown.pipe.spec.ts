import {
  it,
  describe,
  expect,
  inject,
  beforeEachProviders
} from '@angular/core/testing';
import { MarkdownPipe } from './markdown.pipe';

describe('Markdown Pipe', () => {
  beforeEachProviders(() => [MarkdownPipe]);

  it('should transform the input', inject([MarkdownPipe], (pipe: MarkdownPipe) => {
      // expect(pipe.transform(true)).toBe(null);
  }));
});
