var sock = new WebSocket("ws://localhost:9456");

var currentTime = 0;
var synched = true;
var lowerBound = 1;
var upperBound = 0;


sock.onopen = function (event) {

}

sock.onmessage = function (event) {
    console.log("got it");
    console.log(event.data.length);
    var image = new Image();
    var data = JSON.parse(event.data);
    var time = data.time;
    var base64 = data.image;
    setTimeImage(time, "data:image/jpg;base64,"+base64);  
    notifyUpdate(time, time);
}

function notifyUpdate(a, b){
    if(b < lowerBound){
        //enable prev button
        $('#left')[0].disabled = false;
    }else{
        if(a > upperBound){
            //enable next button and move along if keeping up
            $('#right')[0].disabled = false;
            if(synched){
                moveRight();
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
    var im = new Image();
    newTime = currentTime - 1;
    im.src = getTimeImage(newTime);
    var base64 = $("#annocanvas01_canvas")[0].toDataURL();
    setTimeImage(currentTime, base64);
    im.onload = function(){
        $('#annocanvas01_canvas')[0].getContext('2d').drawImage(im, 0, 0);
        CanvasInstances["annocanvas01_canvas"].restartState();
    }
    currentTime = newTime;
    if(currentTime == lowerBound){
        $('#left')[0].disabled = true;
    }
    $('#right')[0].disabled = false;
}

function moveRight(){
    var im = new Image();
    newTime = currentTime + 1;
    im.src = getTimeImage(newTime);
    var base64 = $("#annocanvas01_canvas")[0].toDataURL();
    setTimeImage(currentTime, base64);
    currentTime = newTime;
    im.onload = function (){
        $('#annocanvas01_canvas')[0].getContext('2d').drawImage(im, 0, 0);
        CanvasInstances["annocanvas01_canvas"].restartState();
    }
    if(currentTime == upperBound){
        $('#right')[0].disabled = true;
    }
    $('#left')[0].disabled = false;
}

function getTimeImage(x){
    return sessionStorage.getItem(x);
}

function setTimeImage(x, src){
    console.log("Storing at " + x);
    sessionStorage.setItem(x, serializeSrc(src));
}

function serializeSrc(x){
    //return x.replace(/^data:image\/(png|jpg);base64,/, "");
    return x;
}