/***********************************************************************************************
 * User Configuration.
 **********************************************************************************************/
/** Map relative paths to URLs. */
const map: any = {
    'jquery': 'vendor/jquery/dist/jquery.min.js',
    'tether': 'vendor/tether/dist/js/tether.min.js',
    'bootstrap': 'vendor/bootstrap/dist/js/bootstrap.min.js',
    'moment': 'vendor/moment/moment.js',
    'markdown-it': 'vendor/markdown-it/dist/markdown-it.min.js',
    'autosize': 'vendor/autosize/dist/autosize.min.js',
    'ng2-bootstrap': 'vendor/ng2-bootstrap',
    'angular2-toaster': 'vendor/angular2-toaster'
};

/** User packages configuration. */
const packages: any = {
    'moment': {
        format: 'cjs'
    }
};

////////////////////////////////////////////////////////////////////////////////////////////////
/***********************************************************************************************
 * Everything underneath this line is managed by the CLI.
 **********************************************************************************************/
const barrels: string[] = [
  // Angular specific barrels.
  '@angular/core',
  '@angular/common',
  '@angular/compiler',
  '@angular/http',
  '@angular/router-deprecated',
  '@angular/platform-browser',
  '@angular/platform-browser-dynamic',

  // Thirdparty barrels.
  'rxjs',
  'vendor/ng2-bootstrap',
  'vendor/angular2-toaster',

  // App specific barrels.
  'app',
  'app/shared',
  'app/chat',
  'app/comment',
  'app/markdown-textarea',
  'app/markdown',
  'app/markdown-edit',
  'app/navbar',
  'app/home',
  'app/chat-list',
  /** @cli-barrel */
];

const cliSystemConfigPackages: any = {};
barrels.forEach((barrelName: string) => {
  cliSystemConfigPackages[barrelName] = { main: 'index' };
});

/** Type declaration for ambient System. */
declare var System: any;

// Apply the CLI SystemJS configuration.
System.config({
  map: {
    '@angular': 'vendor/@angular',
    'rxjs': 'vendor/rxjs',
    'main': 'main.js',
  },
  packages: cliSystemConfigPackages
});

// Apply the user's configuration.
System.config({ map, packages });
