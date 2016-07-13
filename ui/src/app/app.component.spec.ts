import {
  beforeEachProviders,
  describe,
  expect,
  it,
  inject
} from '@angular/core/testing';
import { FlowChatAppComponent } from '../app/flowchat.component';

beforeEachProviders(() => [FlowChatAppComponent]);

describe('App: Flowchat', () => {
  it('should create the app',
      inject([FlowChatAppComponent], (app: FlowChatAppComponent) => {
    expect(app).toBeTruthy();
  }));

  it('should have as title \'flowchat works!\'',
      inject([FlowChatAppComponent], (app: FlowChatAppComponent) => {
    expect(app.title).toEqual('flowchat-ui works!');
  }));
});
