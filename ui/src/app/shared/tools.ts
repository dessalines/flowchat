import Zooming from 'zooming';

export class Tools {
  static zooming = new Zooming({});

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

  static linkReplacements(text: string): string {
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
    let replaced = text.replace(/(?:https?:\/\/)(?:www\.)?(?:imgur\.com)\/?([^\s<]+)/g,
      "https://i.imgur.com/$1.jpg");
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

  static isMobileOrTablet() {
  let check = false;
  ((a) => {if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor);
  return check;
};
}