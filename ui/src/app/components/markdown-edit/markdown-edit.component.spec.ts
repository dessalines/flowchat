import {
  beforeEach,
  beforeEachProviders,
  describe,
  expect,
  it,
  inject,
} from '@angular/core/testing';
import { ComponentFixture, TestComponentBuilder } from '@angular/compiler/testing';
import { Component } from '@angular/core';
import { By } from '@angular/platform-browser';
import { MarkdownEditComponent } from './markdown-edit.component';

describe('Component: MarkdownEdit', () => {
  let builder: TestComponentBuilder;

  beforeEachProviders(() => [MarkdownEditComponent]);
  beforeEach(inject([TestComponentBuilder], function (tcb: TestComponentBuilder) {
    builder = tcb;
  }));

  it('should inject the component', inject([MarkdownEditComponent],
      (component: MarkdownEditComponent) => {
    expect(component).toBeTruthy();
  }));

  it('should create the component', inject([], () => {
    return builder.createAsync(MarkdownEditComponentTestController)
      .then((fixture: ComponentFixture<any>) => {
        let query = fixture.debugElement.query(By.directive(MarkdownEditComponent));
        expect(query).toBeTruthy();
        expect(query.componentInstance).toBeTruthy();
      });
  }));
});

@Component({
  selector: 'test',
  template: `
    <app-markdown-edit></app-markdown-edit>
  `,
  directives: [MarkdownEditComponent]
})
class MarkdownEditComponentTestController {
}

