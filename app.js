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
  res.sendFile(__dirname + "/public/main_html_css_files/main.html");
});
