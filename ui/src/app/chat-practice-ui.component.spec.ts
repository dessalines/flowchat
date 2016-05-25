import {
  beforeEachProviders,
  describe,
  expect,
  it,
  inject
} from '@angular/core/testing';
import { ChatPracticeUiAppComponent } from '../app/chat-practice-ui.component';

beforeEachProviders(() => [ChatPracticeUiAppComponent]);

describe('App: ChatPracticeUi', () => {
  it('should create the app',
      inject([ChatPracticeUiAppComponent], (app: ChatPracticeUiAppComponent) => {
    expect(app).toBeTruthy();
  }));

  it('should have as title \'chat-practice-ui works!\'',
      inject([ChatPracticeUiAppComponent], (app: ChatPracticeUiAppComponent) => {
    expect(app.title).toEqual('chat-practice-ui works!');
  }));
});
