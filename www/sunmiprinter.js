module.exports = {
    print: function (params, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "SunmiPrinter", "print", params);
    },
    print2: function (params, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "SunmiPrinter", "print2", params);
    },
    init: function (params, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "SunmiPrinter", "init", []);
    },
    deinit: function (params, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "SunmiPrinter", "deinit", []);
    },
    ping: function (){
        console.warn("PONG!");
    }
};
