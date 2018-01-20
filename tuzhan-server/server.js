const http = require("http");
const url = require("url");
const qs = require("querystring");
const admin = require("firebase-admin");

// setup firebase admin-sdk
const serviceAccount = require("./serviceAccountKey.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://tuzhan-6585f.firebaseio.com"
});
const rootRef = admin.database().ref();
const usersRef = rootRef.child("Users");
const userStatesRef = rootRef.child("UsersStates")

var onlineUsers = [];
var offlineUsers = [];

userStatesRef.on("child_added", (snapshot, uselessKey)=>{
  if(snapshot.val()) onlineUsers.push(snapshot.key);
  else offlineUsers.push(snapshot.key);
});

userStatesRef.on("child_changed", (snapshot)=>{
  let email = snapshot.key;
  if(snapshot.val()){
    let index = offlineUsers.indexOf(email);
    if(index >= 0) offlineUsers.splice(index, 1);
    onlineUsers.push(email);
  }
  else{
    let index = onlineUsers.indexOf(email);
    if(index >= 0) onlineUsers.splice(index, 1);
    offlineUsers.push(email);
  }
});

// userStatesRef.on("child_changed", snapshot=>{
//   console.log(snapshot.val());
// })

const data = require("./data.json");

// generate random integer between 0...upperBound-1
function randomInt(upperBound){
  return Math.floor(Math.random() * upperBound);
}

function randomElement(arr){
  return arr[randomInt(arr.length)];
}

const cardsPerMatch = 2;
// randomly sample a theme and 10 cards from that theme
function sampleMatchParams(){
  let themeIndex = randomInt(data.themes.length);
  var cards = {};
  let upperBound = data.cardCounts[themeIndex] - cardsPerMatch + 1;
  while(Object.keys(cards).length < cardsPerMatch){
    cards[randomInt(upperBound)] = true;
  }
  return {
    "theme"   : data.themes[themeIndex],
    "cardIds" : Object.keys(cards).join("-"),
    "players" : {}
  };
}

let defaultState = {"state" : "dns"};

function createMatch(email1, email2, res){

  let newMatchRef = rootRef.child("Matches").push();
  let matchID = newMatchRef.key;

  var matchParams = sampleMatchParams();
  matchParams["players"][email1] = defaultState;
  matchParams["players"][email2] = defaultState;

  newMatchRef.set(matchParams, error=>{

    var updateParams = {};
    updateParams[email1 + "/matches/" + matchID] = true;
    updateParams[email2 + "/matches/" + matchID] = true;
    usersRef.update(updateParams, error2=>{
      res.end(matchID);
    });

  });

}


function findMatch(email, res){

  // little hack to make sampling simpler + fix wrong user status, restore immediately after sampling
  let thisUserIndex = onlineUsers.indexOf(email);
  if(thisUserIndex >= 0) onlineUsers.splice(thisUserIndex, 1);
  else{
    let i = offlineUsers.indexOf(email);
    if(i >= 0) offlineUsers.splice(i, 1);
  }

  if(onlineUsers.length > 0){
    let otherEmail = randomElement(onlineUsers);
    onlineUsers.push(email);
    createMatch(email, otherEmail, res);
  }
  else if(offlineUsers.length > 0){
    let otherEmail = randomElement(offlineUsers);
    onlineUsers.push(email);
    createMatch(email, otherEmail, res);
  }
  else{
    res.end("NO_MATCH");
  }
}

http.createServer((req, res)=>{

  let query = url.parse(req.url, true).query;

  if(!query || !query.email){
    res.writeHead(403, {"Content-Type" : "text/html"});
    res.end("<!doctype html><html><head><title>403</title></head><body>403: Missing user 'email' parameter in request!</body></html>");
  }
  else{
    res.writeHead(200, {"Content-Type" : "text/plain"});
    findMatch(query.email.replace(/\./g, ","), res);
  }

}).listen(process.env.PORT || 8080);

// ping itself every 10 mins to prevent sleep
setInterval(function() {
    http.get("http://shielded-anchorage-95513.herokuapp.com");
}, 10 * 60 * 1000);
