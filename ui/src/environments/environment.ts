// The file for the current environment will overwrite this one during build.
// Different environments can be found in ./environment.{dev|prod}.ts, and
// you can create your own and use it with the --env flag.
// The build system defaults to the dev environment.

export const environment = {
  production: false,
  endpoint: 'http://localhost:4567/',
  websocket: 'ws://localhost:4567/threaded_chat'
};
