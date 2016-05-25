export class ChatPracticeUiPage {
  navigateTo() {
    return browser.get('/');
  }

  getParagraphText() {
    return element(by.css('chat-practice-ui-app h1')).getText();
  }
}
