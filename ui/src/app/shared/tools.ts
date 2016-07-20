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

  static markdownReplacements(text: string): string {
    let out: string = text;
    out = this.replaceYoutubeWithEmbed(out);
    out = this.replaceImgur(out);
    out = this.replaceImages(out);

    return out;
  }

  static replaceImages(text: string): string {
    return text.replace(/(https?:\/\/.*\.(?:png|jpg|jpeg|gifv|gif))/g,
      '<img class="img-fluid" src="$1">');
  }

  static replaceImgur(text: string): string {
    return text.replace(/(?:https?:\/\/)(?:www\.)?(?:imgur\.com)\/?([^\s<]+)/g,
      '<img class="img-fluid" src="https://i.imgur.com/$1.jpg">');
  }

  static replaceYoutubeWithEmbed(text: string): string {
    let replaced: string = text.replace(/(?:https?:\/\/)?(?:www\.)?(?:youtube\.com|youtu\.be)\/(?:watch\?v=)?([^\s<]+)/g,
      '<div class="embed-responsive embed-responsive-16by9"><iframe class="embed-responsive-item" src="https://www.youtube.com/embed/$1" frameborder="0" allowfullscreen controls="2"></iframe></div>').
      replace(/(?:https?:\/\/)?(?:www\.)?(?:vimeo\.com)\/?([^\s<]+)/g,
      '<div class="embed-responsive embed-responsive-16by9"><iframe src="//player.vimeo.com/video/$1" class="embed-responsive-item" frameborder="0" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe></div>');
    return replaced;
  }

  static isImageType(text: string): boolean {
    let str: string = text.toString();
    let match = str.match(/(https?:\/\/.*(?:png|jpg|jpeg|gifv|gif|imgur|youtube|vimeo))/g);
    if (match != null && match.length > 0) {
      return true;
    } else {
      return false;
    }
  }
}