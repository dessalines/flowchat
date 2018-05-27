import { FlowchatPage } from './app.po';

describe('flowchat App', function() {
  let page: FlowchatPage;

  beforeEach(() => {
    page = new FlowchatPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
