import { ChatPracticeUiPage } from './app.po';

describe('chat-practice-ui App', function() {
  let page: ChatPracticeUiPage;

  beforeEach(() => {
    page = new ChatPracticeUiPage();
  })

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('chat-practice-ui works!');
  });
});
