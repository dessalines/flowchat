// The file for the current environment will overwrite this one during build
// Different environments can be found in config/environment.{dev|prod}.ts
// The build system defaults to the dev environment

export const environment = {
  production: false,
  endpoint: 'http://localhost:4567/',
  websocket: 'ws://localhost:4567/threaded_chat'
};
