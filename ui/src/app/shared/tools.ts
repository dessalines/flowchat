import * as Zooming from 'zooming';

export class Tools {
  static zooming = new Zooming({
    enableGrab: false,
    preloadImage: false,
    closeOnWindowResize: true,
    transitionDuration: 0.2,
    transitionTimingFunction: 'cubic-bezier(0.4, 0, 0, 1)',
    bgColor: 'rgb(255, 255, 255)',
    bgOpacity: 1,
    scaleBase: 1.0,
    scaleExtra: 0.5,
    scrollThreshold: 40,
    zIndex: 998,
  });

  static createCookie(name: string, value: any, expireTime: number) {
    var expires = "; expires=" + new Date(9999999999999);;
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
    out = this.replaceGifv(out);
    out = this.replaceYoutubeWithEmbed(out);
    out = this.replaceImgur(out);
    out = this.replaceImages(out);
    out = this.replaceVideos(out);

    return out;
  }

  static replaceImages(text: string): string {
    return text.replace(/(https?:\/\/.*\.(?:png|jpg|jpeg|gif))/g,
      '<img class="img-fluid img-zoomable" src="$1" />');
  }

  static replaceGifv(text: string): string {
    return text.replace('.gifv', '.mp4');
  }

  static replaceVideos(text: string): string {
    return text.replace(/(https?:\/\/.*\.(?:mp4))/g,
      '<video onPlay="" class="img-fluid" muted="" loop="" poster="$1" controls><source src="$1" type="video/mp4"></source></video>');
  }

  static replaceImgur(text: string): string {
    console.log(text);
    let replaced = text.replace(/(?:https?:\/\/)(?:www\.)?(?:imgur\.com)\/?([^\s<]+)/g,
      "https://i.imgur.com/$1.jpg");
    console.log(replaced);
    return replaced;
  }

  static replaceYoutubeWithEmbed(text: string): string {
    let replaced: string = text.replace(/(?:https?:\/\/)?(?:www\.)?(?:youtube\.com|youtu\.be)\/(?:watch\?v=)?([^\s<]+)/g,
      '<div class="container px-0 py-0"><div class="embed-responsive embed-responsive-1by1"><iframe class="embed-responsive-item" src="https://www.youtube.com/embed/$1" frameborder="0" allowfullscreen controls="2"></iframe></div></div>').
      replace(/(?:https?:\/\/)?(?:www\.)?(?:vimeo\.com)\/?([^\s<]+)/g,
      '<div class="embed-responsive embed-responsive-1by1"><iframe src="//player.vimeo.com/video/$1" class="embed-responsive-item" frameborder="0" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe></div>');
    return replaced;
  }

  static isImageType(text: string): boolean {
    let str: string = text.toString();
    let match = str.match(/(https?:\/\/.*(?:png|jpg|jpeg|gifv|mp4|gif|imgur|youtube|vimeo))/g);
    if (match != null && match.length > 0) {
      return true;
    } else {
      return false;
    }
  }

  static rgex = new RegExp("[a-zA-Z0-9_-]+");
}