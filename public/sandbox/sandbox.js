console.log("Sandbox script loaded");

let matrix = [
  [    0,  5871, 8916, 2868],
  [ 1951, 10048, 2060, 6171],
  [ 8010, 16145, 8090, 8045],
  [ 1013,   990,  940, 6907]
];

d3.select("body")
    .append("table")
    .selectAll("tr")
    .data(matrix)
    .join("tr")
    .selectAll("td")
    .data(d => d)
    .join("td")
      .text(d => d);

function dataJoinMatrix(matrix) {
    let s1 = d3.selectAll("tr");
    let s2 = s1.data(matrix);
    let s3 = s2.join("tr");
    let s4 = s3.selectAll("td");
    let s5 = s4.data(d => d);
    let s6 = s5.join("td");
    s6.text(d => d);

    /*
    d3
                   .selectAll("tr")
                   .data(matrix)
                   .join("tr")
                   .selectAll("td")
                   .data(d => d)
                   .join("td")
                     .text(d => d);
                     */
    console.log("s1", s1);
    console.log("s2", s2);
    console.log("s3", s3);
    console.log("s4", s4);
    console.log("s5", s5);
    console.log("s6", s6);
  return s6;
}

let counter = 1;

setTimeout(() => {
    for (let i = 0; i < matrix.length; i++) {
        for (let j = 0; j < matrix[i].length; j++) {
            if (i == 1 && j == 1) {
                matrix[i][j] = 666;
            }
        }
    }
    //matrix = matrix.map(row => row.map(value => value + 1));
    //matrix.push(matrix[0].map(value => value + 1));
    dataJoinMatrix(matrix);
    //console.log("Sandbox script executed after " + counter + " second delay");
    counter++;
}, 1000);

const data = [
  {name: "Locke", number: 4},
  {name: "Reyes", number: 8},
  {name: "Ford", number: 15},
  {name: "Jarrah", number: 16},
  {name: "Shephard", number: 23},
  {name: "Kwon", number: 42}
];

d3.selectAll("div")
  .data(data, function(d) { return d ? d.name : this.id; })
    .text(d => d.number);

const thingsWithColors = [
    {name: "John", order: 1, color: "red"},
    {name: "Jane", order: 2, color: "blue"},
    {name: "Doe", order: 3, color: "green"},
    {name: "Smith", order: 4, color: "yellow"}
]

const things = [
    {name: "John", order: 1},
    {name: "Jane", order: 2},
    {name: "Doe", order: 3},
    {name: "Smith", order: 4}
]

const circles = [
    {id: "A", cx: 50, cy: 50, color: "red"},
    {id: "B", cx: 150, cy: 50, color: "blue"},
    {id: "C", cx: 50, cy: 150, color: "green"},
    {id: "D", cx: 150, cy: 150, color: "yellow"}
]