import express from 'express';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
app.use(express.urlencoded({ extended: true }));
app.use(express.static("public"));

app.listen(process.env.PORT || "3000", function () {
  console.log("Server listening on port 3000.");
}); 

app.get("/", function (req, res) {
  res.sendFile(__dirname + "/public/easter_eggs/index.html");
});

app.get("/tools", function (req, res) {
  res.sendFile(__dirname + "/public/canvas_tools/tools.html");
});

app.get("/tools_d3", function (req, res) {
  res.sendFile(__dirname + "/public/d3_tools/tools_d3.html");
});
