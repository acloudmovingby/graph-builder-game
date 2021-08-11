const express = require("express");
const app = express();

app.use(express.urlencoded({ extended: true }));
app.use(express.static("public"));

app.listen(process.env.PORT || "3000", function () {
  console.log("Server listening on port 3000.");
}); 

app.get("/", function (req, res) {
  res.sendFile("index.html");
});

app.get("/tools", function (req, res) {
  res.sendFile(__dirname + "/public/tools.html");
});
