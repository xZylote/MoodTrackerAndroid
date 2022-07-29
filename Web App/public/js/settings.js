var database = firebase.database();

getSettings();
nav("a3");

/**
 * Fetch settings
 */

function getSettings() {
    getExistingNotifications();
    getEndDate();
    getConsentForm();
    getDarkMode();
}

/**
 * Fetch dark mode setting
 */

function getDarkMode() {
    darkCheck();
    if (localStorage.getItem("darkMode") == "on") {
        document.getElementById("on").style.backgroundColor = "#007000";
    } else {
        document.getElementById("off").style.backgroundColor = "#ff7070";
    }
}

/**
 * Change synchronization setting on database
 */

function syncChange() {
    firebase.database().ref("Settings/").update({
        synchronization: document.getElementById("sync").value,
    });
}

/**
 * Fetch notification times
 */

function getExistingNotifications() {
    firebase.database().ref('/Settings/notification_times').once('value').then(function(snapshot) {
        const string = (snapshot.val());
        var x = string.split(',');
        for (var i = 1; i < x.length; i++) {
            addToList(x[i]);
        }
    });
}

/**
 * Fetch experiment cycle duration
 */

function getEndDate() {
    firebase.database().ref('/Settings/end_date').once('value').then(function(snapshot) {
        document.getElementById("endDate").innerHTML = (snapshot.val() + " Days");
        document.getElementById("endDateInput").value = snapshot.val();
    });
}

/**
 * Fetch current consent form
 */

function getConsentForm() {


    firebase.database().ref('/Settings/consent_version').once('value').then(function(snapshot1) {
        firebase.database().ref('/Settings/consentforms/consent_form' + snapshot1.val() + '/consent_text').once('value').then(function(snapshot) {
            document.getElementById("consentForm").innerHTML = (snapshot.val());
        });
    });
}

/**
 * Show experiment cycle duration editor on button click
 */

function changeEndDate() {
    document.getElementById("editDateInput").style.display = "none";
    document.getElementById("endDateInput").style.display = "inline";
    document.getElementById("endDateButton").style.display = "inline";
    document.getElementById("endDateCancel").style.display = "inline";
}

/**
 * Update experiment cycle duration on the database
 */

function setEndDate() {
    var endDate = document.getElementById("endDateInput").value;
    if (endDate != "" && endDate > 0) { // Check for Valid date
        document.getElementById("endDate").innerHTML = endDate + " Days";
        document.getElementById("editDateInput").style.display = "inline";
        document.getElementById("endDateInput").style.display = "none";
        document.getElementById("endDateButton").style.display = "none";
        document.getElementById("endDateCancel").style.display = "none";
        firebase.database().ref("Settings/").update({
            end_date: endDate,
        });
    } else {
        alert('Please make a valid input');
    }
}

/**
 * Cancel editing experiment cycle duration
 */

function cancelEndDate() {
    document.getElementById("editDateInput").style.display = "inline";
    document.getElementById("endDateInput").style.display = "none";
    document.getElementById("endDateButton").style.display = "none";
    document.getElementById("endDateCancel").style.display = "none";
}

/**
 * Show consent form editor on button click
 */

function changeConsent() {
    document.getElementById("consentInput").value = document.getElementById("consentForm").innerHTML;
    document.getElementById("editConsentInput").style.display = "none";
    document.getElementById("consentInput").style.display = "inline";
    document.getElementById("consentButton").style.display = "inline";
    document.getElementById("consentCancel").style.display = "inline";
}

/**
 * Update consent form on database
 */

function setConsent() {
    var consentInput = document.getElementById("consentInput").value;
    document.getElementById("consentForm").innerHTML = consentInput;
    document.getElementById("editConsentInput").style.display = "inline";
    document.getElementById("consentInput").style.display = "none";
    document.getElementById("consentButton").style.display = "none";
    document.getElementById("consentCancel").style.display = "none";

    firebase.database().ref('/Settings/consent_version').once('value').then(function(snapshot) {
        firebase.database().ref("Settings/consentforms/consent_form" + (snapshot.val() + 1)).update({
            consent_text: consentInput,
        });

        firebase.database().ref("Settings/").update({
            consent_version: snapshot.val() + 1
        });
    });
}

/**
 * Cancel editing consent form
 */

function cancelConsent() {
    document.getElementById("editConsentInput").style.display = "inline";
    document.getElementById("consentInput").style.display = "none";
    document.getElementById("consentButton").style.display = "none";
    document.getElementById("consentCancel").style.display = "none";
}

/**
 * Get notification setting input fields
 */

var input1 = document.getElementById("notiTime");
var input2 = document.getElementById("notiTime2");
var input3 = document.getElementById("notiTime3");

/**
 * Process notification input on button click
 * Call addToList below
 */

function setNotification() {
    // This will only write the selected timeframes and notification count into the DB, the randomization part should be done in the app because it needs to be different for every user
    if (input1.value != "" && input2.value != "" && input3.value != "" && input3.value != 0) {
        var concat = input1.value + "-" + input2.value + " - " + input3.value;

        var timestart = Number(input1.value.substring(0, 2) * 60) + Number(input1.value.substring(3, 5));
        var timeend = Number(input2.value.substring(0, 2) * 60) + Number(input2.value.substring(3, 5));
        var duration;
        if (timeend > timestart) {
            duration = (timeend - timestart);
        } else if (timeend <= timestart) { //23:45 - 1:15 for example
            duration = (timeend - timestart + 1440);
        }
        if ((duration + 30) / input3.value < 30) {
            alert("It is impossible to separate this amount of notifications by 30 minutes or more");
        } else {
            firebase.database().ref('/Settings/notification_times').once('value').then(function(snapshot) {
                if (!snapshot.val().includes(concat.substring(0, 11))) {
                    if (snapshot.val().length > 2) { // We will use a leading comma, so "," is no entry in the DB
                        firebase.database().ref("Settings/").update({
                            notification_times: snapshot.val() + "," + concat
                        });
                    } else {
                        firebase.database().ref("Settings/").update({
                            notification_times: snapshot.val() + concat
                        });
                    }
                    addToList(concat);
                } else {
                    alert('This entry exists already!');
                }
            });
        }
    } else {
        alert('Please make valid inputs')
    }
}

/**
 * Add notification time data to database
 */

function addToList(input) {
    var entry = document.createElement("li");

    setTimeout(function() {
        entry.className = "show";
    }, 5);
    entry.appendChild(document.createTextNode(input));
    if (input != "") {
        document.getElementById("todoList").appendChild(entry);
    } else {
        console.log('There are no notification times set in the database.')
    }
    input1.value = "";
    input2.value = "";
    input3.value = "";

    var xbutton = document.createElement("span");
    xbutton.className = "delete";
    xbutton.appendChild(document.createTextNode("x"));
    entry.appendChild(xbutton);
    var close = document.getElementsByClassName("delete");
    for (var i = 0; i < close.length; i++) {
        close[i].onclick = function() {
            var entries = this.parentElement;
            entries.className = "hide";
            setTimeout(function() {
                var removedTime = entries.innerHTML.substring(0, 15);
                firebase.database().ref('/Settings/notification_times').once('value').then(function(snapshot) {
                    if (snapshot.val().includes(removedTime)) {
                        firebase.database().ref("Settings/").update({
                            notification_times: snapshot.val().replace("," + removedTime, "")
                        });
                    } else {
                        alert('This should not happen. The time you want to delete is not in the Firebase DB, check your internet connection.');
                    }
                });
                entries.parentNode.removeChild(entries);
            }, 500);
        }
    }
}


/**
 * Delete user by ID from database on button click
 */

function deleteUser() {
    var uID = document.getElementById("IDInput").value;
    var deleteUser = firebase.functions().httpsCallable('deleteUser');
    deleteUser({ userid: uID }).then(function(result) {
        alert("User deleted " + (result.data.success) + "fully");
        document.getElementById("IDInput").value = "";
    }).catch(function(error) {
        console.log(error);
        alert("Could not delete user");
    });
}

/**
 * Turn on dark mode on button click
 */

function darkOn() {
    localStorage.setItem("darkMode", "on");
    getDarkMode();
    nav("a3");
}

/**
 * Turn off dark mode on button click
 */

function darkOff() {
    localStorage.setItem("darkMode", "off");
    getDarkMode();
    nav("a3");
}