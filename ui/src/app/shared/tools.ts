export class Tools {
  static createCookie(name: string, value: any, expireTime: number) {
    if (expireTime) {
      var expires = "; expires=" + new Date(expireTime);
    } else var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
  }

  static readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == ' ') c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
  }

  static eraseCookie(name) {
    this.createCookie(name, "", -1);
  }

  static removeQuotes(text: string) {
    return text.replace(/['"]+/g, '');
  }

  static parseImageThumbnail(link: string) {
    // Check to see if its already a link
    if (link.match(/\.(jpeg|jpg|gif|png)/) != null) {
      return link;
    }

    else if (link.match(/(imgur.com)/) != null) {
      // Extract its id:
      let imgurId = link.split('/').pop();
      let imgurLink = 'http://i.imgur.com/' + imgurId + '.jpg';
      return imgurLink;
    }

    return null;
  }
}