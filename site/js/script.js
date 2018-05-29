var sock = new WebSocket("ws://localhost:9456");

var currentTime = -1;

sock.onopen = function (event) {

}

sock.onmessage = function (event) {
    console.log("got it");
    console.log(event.data.length);
    /*var imgData = event.data
    var uInt8Array = imgData;
    var i = uInt8Array.length;
    var binaryString = [i];
    while (i--) {
        binaryString[i] = String.fromCharCode(uInt8Array[i]);
    }
    var data = binaryString.join('');

    var base64 = window.btoa(data);*/
    var base64 = event.data;
    var data = JSON.parse(event.data);
    var time = data.time;
    currentTime = time;
    var base64 = data.image;
    $("#Frame")[0].src = "data:image/jpg;base64,"+ base64;


}