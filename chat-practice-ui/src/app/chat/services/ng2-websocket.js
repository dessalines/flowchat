System.register(['angular2/core', 'angular2/src/facade/lang', "rxjs/Subject"], function(exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
        var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
        if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
        else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
        return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
    var __metadata = (this && this.__metadata) || function (k, v) {
        if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
    var core_1, lang_1, Subject_1;
    var $WebSocket;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (lang_1_1) {
                lang_1 = lang_1_1;
            },
            function (Subject_1_1) {
                Subject_1 = Subject_1_1;
            }],
        execute: function() {
            $WebSocket = (function () {
                function $WebSocket(url, protocols, config) {
                    this.url = url;
                    this.protocols = protocols;
                    this.config = config;
                    this.reconnectAttempts = 0;
                    this.sendQueue = [];
                    this.onOpenCallbacks = [];
                    this.onMessageCallbacks = [];
                    this.onErrorCallbacks = [];
                    this.onCloseCallbacks = [];
                    this.readyStateConstants = {
                        'CONNECTING': 0,
                        'OPEN': 1,
                        'CLOSING': 2,
                        'CLOSED': 3,
                        'RECONNECT_ABORTED': 4
                    };
                    this.normalCloseCode = 1000;
                    this.reconnectableStatusCodes = [4000];
                    var match = new RegExp('wss?:\/\/').test(url);
                    if (!match) {
                        throw new Error('Invalid url provided');
                    }
                    this.config = config || { initialTimeout: 500, maxTimeout: 300000, reconnectIfNotNormalClose: false };
                    this.dataStream = new Subject_1.Subject();
                }
                $WebSocket.prototype.connect = function (force) {
                    var _this = this;
                    if (force === void 0) { force = false; }
                    var self = this;
                    if (force || !this.socket || this.socket.readyState !== this.readyStateConstants.OPEN) {
                        self.socket = this.protocols ? new WebSocket(this.url, this.protocols) : new WebSocket(this.url);
                        self.socket.onopen = function (ev) {
                            //    console.log('onOpen: %s', ev);
                            _this.onOpenHandler(ev);
                        };
                        self.socket.onmessage = function (ev) {
                            //   console.log('onNext: %s', ev.data);
                            self.onMessageHandler(ev);
                            _this.dataStream.next(ev);
                        };
                        this.socket.onclose = function (ev) {
                            //     console.log('onClose, completed');
                            self.onCloseHandler(ev);
                            _this.dataStream.complete();
                        };
                        this.socket.onerror = function (ev) {
                            //    console.log('onError', ev);
                            self.onErrorHandler(ev);
                            _this.dataStream.error(ev);
                        };
                    }
                };
                $WebSocket.prototype.send = function (data) {
                    var self = this;
                    if (this.getReadyState() != this.readyStateConstants.OPEN && this.getReadyState() != this.readyStateConstants.CONNECTING) {
                        this.connect();
                    }
                    return new Promise(function (resolve, reject) {
                        if (self.socket.readyState === self.readyStateConstants.RECONNECT_ABORTED) {
                            reject('Socket connection has been closed');
                        }
                        else {
                            self.sendQueue.push({ message: data });
                            self.fireQueue();
                        }
                    });
                };
                ;
                $WebSocket.prototype.getDataStream = function () {
                    return this.dataStream;
                };
                $WebSocket.prototype.onOpenHandler = function (event) {
                    this.reconnectAttempts = 0;
                    this.notifyOpenCallbacks(event);
                    this.fireQueue();
                };
                ;
                $WebSocket.prototype.notifyOpenCallbacks = function (event) {
                    for (var i = 0; i < this.onOpenCallbacks.length; i++) {
                        this.onOpenCallbacks[i].call(this, event);
                    }
                };
                $WebSocket.prototype.fireQueue = function () {
                    while (this.sendQueue.length && this.socket.readyState === this.readyStateConstants.OPEN) {
                        var data = this.sendQueue.shift();
                        this.socket.send(lang_1.isString(data.message) ? data.message : JSON.stringify(data.message));
                    }
                };
                $WebSocket.prototype.notifyCloseCallbacks = function (event) {
                    for (var i = 0; i < this.onCloseCallbacks.length; i++) {
                        this.onCloseCallbacks[i].call(this, event);
                    }
                };
                $WebSocket.prototype.notifyErrorCallbacks = function (event) {
                    for (var i = 0; i < this.onErrorCallbacks.length; i++) {
                        this.onErrorCallbacks[i].call(this, event);
                    }
                };
                $WebSocket.prototype.onOpen = function (cb) {
                    this.onOpenCallbacks.push(cb);
                    return this;
                };
                ;
                $WebSocket.prototype.onClose = function (cb) {
                    this.onCloseCallbacks.push(cb);
                    return this;
                };
                $WebSocket.prototype.onError = function (cb) {
                    this.onErrorCallbacks.push(cb);
                    return this;
                };
                ;
                $WebSocket.prototype.onMessage = function (callback, options) {
                    if (!lang_1.isFunction(callback)) {
                        throw new Error('Callback must be a function');
                    }
                    this.onMessageCallbacks.push({
                        fn: callback,
                        pattern: options ? options.filter : undefined,
                        autoApply: options ? options.autoApply : true
                    });
                    return this;
                };
                $WebSocket.prototype.onMessageHandler = function (message) {
                    var pattern;
                    var self = this;
                    var currentCallback;
                    for (var i = 0; i < self.onMessageCallbacks.length; i++) {
                        currentCallback = self.onMessageCallbacks[i];
                        currentCallback.fn.apply(self, [message]);
                    }
                };
                ;
                $WebSocket.prototype.onCloseHandler = function (event) {
                    this.notifyCloseCallbacks(event);
                    if ((this.config.reconnectIfNotNormalClose && event.code !== this.normalCloseCode) || this.reconnectableStatusCodes.indexOf(event.code) > -1) {
                        this.reconnect();
                    }
                };
                ;
                $WebSocket.prototype.onErrorHandler = function (event) {
                    this.notifyErrorCallbacks(event);
                };
                ;
                $WebSocket.prototype.reconnect = function () {
                    this.close(true);
                    var backoffDelay = this.getBackoffDelay(++this.reconnectAttempts);
                    var backoffDelaySeconds = backoffDelay / 1000;
                    // console.log('Reconnecting in ' + backoffDelaySeconds + ' seconds');
                    setTimeout(this.connect(), backoffDelay);
                    return this;
                };
                $WebSocket.prototype.close = function (force) {
                    if (force || !this.socket.bufferedAmount) {
                        this.socket.close();
                    }
                    return this;
                };
                ;
                // Exponential Backoff Formula by Prof. Douglas Thain
                // http://dthain.blogspot.co.uk/2009/02/exponential-backoff-in-distributed.html
                $WebSocket.prototype.getBackoffDelay = function (attempt) {
                    var R = Math.random() + 1;
                    var T = this.config.initialTimeout;
                    var F = 2;
                    var N = attempt;
                    var M = this.config.maxTimeout;
                    return Math.floor(Math.min(R * T * Math.pow(F, N), M));
                };
                ;
                $WebSocket.prototype.setInternalState = function (state) {
                    if (Math.floor(state) !== state || state < 0 || state > 4) {
                        throw new Error('state must be an integer between 0 and 4, got: ' + state);
                    }
                    this.internalConnectionState = state;
                };
                /**
                 * Could be -1 if not initzialized yet
                 * @returns {number}
                 */
                $WebSocket.prototype.getReadyState = function () {
                    if (this.socket == null) {
                        return -1;
                    }
                    return this.internalConnectionState || this.socket.readyState;
                };
                $WebSocket = __decorate([
                    core_1.Injectable(), 
                    __metadata('design:paramtypes', [String, Array, Object])
                ], $WebSocket);
                return $WebSocket;
            }());
            exports_1("$WebSocket", $WebSocket);
        }
    }
});
//# sourceMappingURL=ng2-websocket.js.map