var sock = new WebSocket("ws://localhost:9456");
var spillover = new WebSocket("ws://localhost:8022");

var currentTime = 0;
var synched = true;
var lowerBound = 1;
var upperBound = 0;
var slideno = 0;
var catchingUp = false;
var gotFirst = false;

spillover.onmessage = function (event) {
    var message = JSON.parse(event.data);
    if(message.time != slideno){
        return;
    }
    //SLIDENO MIGHT CHANGE HERE RACE CONDITION!!!
    if(message.image == null){
        return;
    }
    var im = new Image();
    im.src = message.image;
    var base64 = $("#annocanvas01_canvas")[0].toDataURL("image/jpeg");
    im.onload = function (){
        CanvasInstances["annocanvas01_canvas"].restartState();
        $('#annocanvas01_canvas')[0].getContext('2d').drawImage(im, 0, 0);
    }
}

sock.onopen = function (event) {
}

sock.onmessage = function (event) {
    console.log("got it");
    console.log(event.data.length);
    var image = new Image();
    var data = JSON.parse(event.data);
    var time = data.time;
    var command = data.command;
    var base64 = data.image;
    if(!gotFirst){
        gotFirst = true;
        setTimeImage("data:image/jpeg;base64,"+base64);
        slideno = time;
        currentTime = time;
    }
    if(catchingUp && time == currentTime){
        setTimeImage("data:image/jpeg;base64,"+base64);
        slideno = time;
        catchingUp = false;
    }
    if(!synched && command == 1){
        setTimeImage("data:image/jpeg;base64,"+base64);
        slideno = time;
    }
    if(synched && command == 0){
        setTimeImage("data:image/jpeg;base64,"+base64);
        var base64s = $("#annocanvas01_canvas")[0].toDataURL("image/jpeg");
        storeImage(slideno, base64s);
        slideno = time;
        currentTime = time;
        $('#left')[0].disabled = false;
        $('#right')[0].disabled = true;
    }
    notifyUpdate(time, time);
}

function notifyUpdate(a, b){
    if(a > lowerBound){
        //enable prev button
        $('#left')[0].disabled = false;
    }else{
        if(b < upperBound){
            //enable next button and move along if keeping up
            $('#right')[0].disabled = false;
            if(synched){
                //moveRight();
                CanvasInstances["annocanvas01_canvas"].restartState();
            }
        }
    }
    lowerBound = Math.min(lowerBound, a);
    upperBound = Math.max(upperBound, b);
}

function downloadPDF(){
    var doc = new jsPDF('l','px',[1280,720]);
    for(var i = lowerBound;i <= upperBound;i++){
        doc.addImage(getTimeImage(i), "JPG", 0,0, 1280, 720);
        if(i < upperBound){
            doc.addPage();
        }
    }
    doc.save("slides.pdf");
}

function moveLeft(){
    //var im = new Image();
    newTime = slideno - 1;
    //im.src = getTimeImage(newTime);
    getRemoteImage(newTime);
    var base64 = $("#annocanvas01_canvas")[0].toDataURL("image/png");
    storeImage(slideno, base64);
    /*im.onload = function(){
        CanvasInstances["annocanvas01_canvas"].restartState();
        $('#annocanvas01_canvas')[0].getContext('2d').drawImage(im, 0, 0);
    }*/
    slideno = newTime;
    //getRemoteAnnot(slideno);
    if(slideno == lowerBound){
        $('#left')[0].disabled = true;
    }
    $('#right')[0].disabled = false;
    if(synched){
        toggleSync()
    }
}

function moveRight(){
    //var im = new Image();
    newTime = slideno + 1;
    /*im.src = getTimeImage(newTime);*/
    getRemoteImage(newTime);
    var base64 = $("#annocanvas01_canvas")[0].toDataURL("image/png");
    storeImage(slideno, base64);
    slideno = newTime;
    getRemoteAnnot(slideno);
    /*im.onload = function (){
        CanvasInstances["annocanvas01_canvas"].restartState();
        $('#annocanvas01_canvas')[0].getContext('2d').drawImage(im, 0, 0);
    }*/
    if(slideno == upperBound){
        $('#right')[0].disabled = true;
    }
    $('#left')[0].disabled = false;
}

function getTimeImage(x){
    return sessionStorage.getItem(x);
}

function setTimeImage(src){
    //console.log("Storing at " + x);
    //sessionStorage.setItem(x, serializeSrc(src));
    $("#background")[0].src = src;
}

function storeImage(x, src){
    //sessionStorage.setItem(x, serializeSrc(src));
    var data = {command: 1, time: x, image: src};
    var str = JSON.stringify(data);
    spillover.send(str);
    CanvasInstances["annocanvas01_canvas"].restartState();
}

function serializeSrc(x){
    //return x.replace(/^data:image\/(png|jpg);base64,/, "");
    return x;
}

function toggleSync(){
    var but = $("#sync")[0];
    if(synched){
        synched = false;
        but.value = "Not Synched";
    }else{
        synched = true;
        catchUp();
        but.value = "Synched";
    }
}

function getRemoteImage(x){
    var data = {command: 1, time: x};
    var str = JSON.stringify(data);
    sock.send(str);
}

function getRemoteAnnot(x){
    var data = {command: 0, time: x};
    var str = JSON.stringify(data);
    spillover.send(str);
}

function catchUp(){
    catchingUp = true;
    synched = true;
    getRemoteImage(currentTime);
}