module.exports = {
    print: function (params, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "SunmiPrinter", "print", params);
    },
    printRow: function (params, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "SunmiPrinter", "printRow", params);
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
