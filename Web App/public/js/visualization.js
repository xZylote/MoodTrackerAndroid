/*********************************
 * THIS SCRIPT IS NO LONGER USED *
 *********************************/

nav("a2");
darkCheck();

var url_string = window.location.href;
var url1 = new URL(url_string);
var uid = url1.searchParams.get("q");
var startDate = url1.searchParams.get("s");
var endDate = url1.searchParams.get("e");

document.getElementById("title").innerHTML = "Your mood on " + startDate;

// Show start and end date in the right format in the inputs
if (startDate)
    document.getElementById("visStart").value = startDate.replace("/", "-").replace("/", "-");
if (endDate)
    document.getElementById("visEnd").value = endDate.replace("/", "-").replace("/", "-");




// Reload page with new start and end date
function changeDate() {
    if (document.getElementById("visStart").value != "" && document.getElementById("visEnd").value != "") {
        location.replace("https://m.hay.li/hidden.html?q=" + uid + "&s=" + document.getElementById("visStart").value.replace("-", "/").replace("-", "/") + "&e=" + document.getElementById("visEnd").value.replace("-", "/").replace("-", "/"));
    }
}

// Show or hide mood line 
function checkbox1() {
    if (document.getElementById("mood").checked) {
        var lines = document.getElementsByClassName("line1");
        lines[0].style.opacity = "1";

    } else {
        var lines = document.getElementsByClassName("line1");
        lines[0].style.opacity = "0";
    }
}

// Show or hide stress line
function checkbox2() {
    if (document.getElementById("stress").checked) {
        var lines = document.getElementsByClassName("line2");
        lines[0].style.opacity = "1";

    } else {
        var lines = document.getElementsByClassName("line2");
        lines[0].style.opacity = "0";
    }
}

function checkbox3() {
    if (!document.getElementById("situations").checked) {
        for (let index = 0; index < document.getElementsByClassName("aa").length; index++) {
            document.getElementsByClassName("aa")[index].classList.add("hidden");
            document.getElementsByClassName("bb")[index].classList.add("hidden");
        }
    } else {
        for (let index = 0; index < document.getElementsByClassName("aa").length; index++) {
            document.getElementsByClassName("aa")[index].classList.remove("hidden");
            document.getElementsByClassName("bb")[index].classList.remove("hidden");
        }
    }
}

function checkbox4() {
    if (!document.getElementById("companions").checked) {
        for (let index = 0; index < document.getElementsByClassName("cc").length; index++) {
            document.getElementsByClassName("cc")[index].classList.add("hidden");
        }
    } else {
        for (let index = 0; index < document.getElementsByClassName("cc").length; index++) {
            document.getElementsByClassName("cc")[index].classList.remove("hidden");
        }
    }
}

firebase.database().ref('/Users/' + uid + '/moods').once('value').then(function(snapshot) {
    moodData = snapshot.val();
    var url = ("https://angrynerds-dac9e.firebaseio.com/Users/" + uid + "/moods.json");
    d3.json(url).then(function(data) {

        // Reading the data from the JSON-File stored in Firebase

        data = Object.values(data);
        let data2 = [];
        for (let key in data) {
            let d = data[key];
            let time = data[key].time;
            data[key].weight = 1;
            // Check whether GMT is included in the time string and remove it
            if (time.length > 30) {
                d.time = +parseTime(time.substring(0, 19) + " " + time.substring(30, 34));
                console.log("Time: " + time.substring(0, 19) + " " + time.substring(30, 34));
            } else {
                d.time = +parseTime(time.substring(0, 19) + " " + time.substring(24, 29));
                console.log("Time: " + time.substring(0, 19) + " " + time.substring(24, 29));
            }
            // Parse time to miliseconds
            if ((d.time >= parseTime2(startDate) && d.time <= parseTime2(endDate))) {
                data2.push(data[key]);
            }

            console.log("Timestamp: " + d.time);
            console.log("Mood: " + d.mood);
            console.log("Stress: " + d.stress);
            console.log("Situations: " + d.situations);
            console.log("Companions: " + d.companions);
            console.log("--------------");
        }
        data = data2;
        // TODO: Prioritize merging datapoints closer to each other on the x axis
        // MINIMUM DISTANCE NEEDS TO BE 60px
        // TODO: Text overlaps graph borders
        // Reduce the amount of datapoints to at most 10, merging neighboring ones
        while (data.length > 10) {
            for (let index = 0; index < data.length - 1; index = index + 2) {
                var a = data[index];
                var b = data[index + 1];
                a.mood = (a.mood * a.weight + b.mood * b.weight) / (a.weight + b.weight);
                a.stress = (a.stress * a.weight + b.stress * b.weight) / (a.weight + b.weight);
                a.time = (a.time * a.weight + b.time * b.weight) / (a.weight + b.weight);
                a.weight = a.weight + b.weight;

                // Merging arrays and removing duplicate situations
                var situationsArray = (a.situations).concat(b.situations);
                var uniqueSituationsArray = situationsArray.filter(function(item, pos) {
                    return situationsArray.indexOf(item) == pos;
                })
                a.situations = uniqueSituationsArray;

                // Merging arrays and removing duplicate companions
                var companionsArray = (a.companions).concat(b.companions);
                var uniqueCompanionsArray = companionsArray.filter(function(item, pos) {
                    return companionsArray.indexOf(item) == pos;
                })
                a.companions = uniqueCompanionsArray;

                // Deleting the second of the 2 to be merged datapoints, since the first now contains data of both
                data.splice(index + 1, 1);
            }
        }

        // Scale the range of the data
        x.domain(d3.extent(data, function(d) { return d.time; }));
        y.domain([1, 5]);
        // Add the valueline path.
        svg.append("path")
            .data([data])
            .attr("class", "line2")
            .attr("d", stressline);

        svg.append("path")
            .data([data])
            .attr("class", "line1")
            .attr("d", moodline);

        // Add the x Axis
        svg.append("g")
            .attr("transform", "translate(0," + height + ")")
            .attr("class", "axis")
            .call(d3.axisBottom(x).ticks(3));

        // Add the y Axis
        svg.append("g")
            .attr("class", "axis")
            .call(d3.axisLeft(y).ticks(5));

        svg.append("defs")
            .append("clipPath") //clips the indicatorline
            .attr("id", "focusClip")
            .append("rect")
            .attr("width", width)
            .attr("height", height);

        console.log(data);
        for (var i = 0; i < data.length; i++) {
            // Create lines to help the user see the connections between situations and data points
            document.getElementById("rec" + i).innerHTML = document.getElementById("rec0").innerHTML;
            document.getElementById("rec" + i).style.position = "absolute";
            var max = Math.min(y(data[i].mood), y(data[i].stress))
            document.getElementById("rec" + i).style.top = 195 + max + "px";
            document.getElementsByClassName("rectangle")[i].setAttribute("height", height - max + 250 + "px");
            document.getElementsByClassName("rectangle")[i].parentElement.setAttribute("height", height - max + 250 + "px");
            document.getElementById("rec" + i).style.left = 97.5 + x(data[i].time) + "px";

            // Setting the positions of the icons for special situations
            document.getElementById("div" + i).innerHTML = document.getElementById("div0").innerHTML;
            document.getElementById("div" + i).style.position = "absolute";
            document.getElementById("div" + i).style.top = "550px";
            if (data[i].time) {
                document.getElementById("div" + i).style.left = 82.5 + x(data[i].time) + "px";
            }
            // Create the circles with text elements for the companion names on top of the curve
            document.getElementById("comp" + i).innerHTML = document.getElementById("comp0").innerHTML;
            document.getElementById("comp" + i).style.position = "absolute";
            document.getElementById("comp" + i).style.top = "200px";
            if (data[i].time) {
                document.getElementById("comp" + i).style.left = 70 + x(data[i].time) + "px";
            }

            if (data[i].companions != "") {
                var max = Math.min(y(data[i].mood), y(data[i].stress))
                document.getElementById("comp" + i).style.top = max - 805 + "px";
                if ((measureText(data[i].companions.join()).width / 2) - max > 50) {
                    console.log(document.getElementById("comp" + i).children[0].children[0].children[1]);
                    document.getElementById("comp" + i).children[0].children[0].children[1].classList.add("alignleft");
                }
                // If there are multiple companions selected for a recording, we will put the number of companions into the circle instead of the first two letters of the name or nickname
                if (data[i].companions.length > 1) {
                    let string = data[i].companions;
                    let length2 = data[i].companions.length;
                    // You can identify circles with multiple companions by the + prefix
                    document.getElementsByClassName("compname")[i].innerHTML = "+" + length2;
                    // You can see the list of full names by clicking on the circles
                    document.getElementsByClassName("compname")[i].onclick = function() {
                        if (!this.classList.contains("clicked")) {
                            this.innerHTML = string;
                            this.classList.add("clicked");
                        } else {
                            this.innerHTML = "+" + length2;
                            this.classList.remove("clicked");
                        }
                    };

                } else {
                    // We will only show the first two letters of the name on the circle. You can see the full name by clicking on the circles
                    let other = data[i].companions[0];
                    document.getElementsByClassName("compname")[i].innerHTML = data[i].companions[0].substring(0, 2);
                    document.getElementsByClassName("compname")[i].onclick = function() {
                        if (!this.classList.contains("clicked")) {
                            this.innerHTML = other;
                            this.classList.add("clicked");
                        } else {
                            this.innerHTML = other.substring(0, 2);
                            this.classList.remove("clicked");
                        }
                    }
                }
            } else {
                // Hide the circle if there are no companions for this mood recording
                if (i != 0) {
                    document.getElementsByClassName("circeel")[i].parentElement.style.display = "none";
                }
            }
        }

        // Make the selected special situations visible
        for (var j = 0; j < data.length; j++) {
            for (var i = 0; i < data[j].situations.length; i++) {
                var svgSituations = data[j].situations[i];
                var container = document.querySelector("#div" + j);
                if (svgSituations == "Partner related (+)") {
                    container.getElementsByClassName("partnergreen")[0].style.display = "inline";
                } else if (svgSituations == "Work / Study (+)") {
                    container.getElementsByClassName("studygreen")[0].style.display = "inline";
                } else if (svgSituations == "Family (+)") {
                    container.getElementsByClassName("familygreen")[0].style.display = "inline";
                } else if (svgSituations == "Health (+)") {
                    container.getElementsByClassName("medicalgreen")[0].style.display = "inline";
                } else if (svgSituations == "Other (+)") {
                    container.getElementsByClassName("othergreen")[0].style.display = "inline";
                } else if (svgSituations == "Work / Study (-)") {
                    container.getElementsByClassName("studyred")[0].style.display = "inline";
                } else if (svgSituations == "Family (-)") {
                    container.getElementsByClassName("familyred")[0].style.display = "inline";
                } else if (svgSituations == "Partner related (-)") {
                    container.getElementsByClassName("partnerred")[0].style.display = "inline";
                } else if (svgSituations == "Health (-)") {
                    container.getElementsByClassName("medicalred")[0].style.display = "inline";
                } else if (svgSituations == "Other (-)") {
                    container.getElementsByClassName("otherred")[0].style.display = "inline";
                }
            }
        }
    });
});

// set the dimensions and margins of the graph
var margin = { top: 20, right: 100, bottom: 50, left: 100 },
    width = window.innerWidth - margin.left - margin.right,
    height = 600 - margin.top - margin.bottom;

// parse the date / time
// var parseTime = d3.timeParse("%Y-%m-%d#%H:%M:%S");
var parseTime = d3.timeParse("%a %b %d %H:%M:%S %Y");
var parseTime2 = d3.timeParse("%Y/%m/%d");
var format = d3.timeFormat("%a %b %d %H:%M:%S %Y");

// set the ranges
var x = d3.scaleTime().range([0, width]);
var y = d3.scaleLinear().range([height, 0]);

var curve = d3.curveMonotoneX;

// define the line
var moodline = d3.line()
    .x(function(d) {
        return x(d.time);
    })
    .y(function(d) {
        return y(d.mood);
    }).curve(curve);

// define the line
var stressline = d3.line()
    .x(function(d) {
        return x(d.time);
    })
    .y(function(d) {
        return y(d.stress);
    }).curve(curve);

// append the svg obgect to the body of the page
// appends a 'group' element to 'svg'
// moves the 'group' element to the top left margin
var svg = d3.select("#my_dataviz").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform",
        "translate(" + margin.left + "," + margin.top + ")");

// Get the data

function measureText(pText) {
    var lDiv = document.createElement('div');

    document.body.appendChild(lDiv);

    lDiv.style.fontSize = "40px";
    lDiv.style.position = "absolute";
    lDiv.style.left = -1000;
    lDiv.style.top = -1000;

    lDiv.innerHTML = pText;

    var lResult = {
        width: lDiv.clientWidth,
        height: lDiv.clientHeight
    };

    document.body.removeChild(lDiv);
    lDiv = null;

    return lResult;
}