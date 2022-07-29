nav("a1");
darkCheck();

/**
 * Get CSV from time frame (button)
 * Read values and call getCSVTimeframe2 below
 */

function getCSVTimeframe() {
    getCSVTimeframe2(document.getElementById("DLStart").value, document.getElementById("DLEnd").value);
}

/**
 * Get CSV for time frame
 * @param {string} downloadStart # Timestamp from (formatted)
 * @param {string} downloadEnd # Timestamp to (formatted)
 */

function getCSVTimeframe2(downloadStart, downloadEnd) {
    var getCSV = firebase.functions().httpsCallable('getCSV');
    getCSV({ start: new Date(downloadStart).valueOf(), end: new Date(downloadEnd).valueOf() }).then(function(result) {
        var result1 =
            "user_id,join_date,exit_date,consent_form_version,consent_accepted,questionnaire_1_id,questionnaire_1_Q_1,questionnaire_1_Q_2,questionnaire_1_Q_3,questionnaire_1_Q_4,questionnaire_1_Q_5,questionnaire_1_Q_6,questionnaire_1_Q_7,questionnaire_1_Q_8,questionnaire_1_Q_9,questionnaire_1_Q_10,questionnaire_1_Q_11,questionnaire_1_Q_12,questionnaire_1_Q_13,questionnaire_1_Q_14,questionnaire_1_Q_15,questionnaire_2_id,questionnaire_2_Q_1,questionnaire_2_Q_2,questionnaire_2_Q_3,questionnaire_2_Q_4,questionnaire_2_Q_5,questionnaire_2_Q_6,questionnaire_2_Q_7,questionnaire_2_Q_8,questionnaire_2_Q_9,questionnaire_2_Q_10,questionnaire_2_Q_11,questionnaire_2_Q_12,questionnaire_2_Q_13,questionnaire_2_Q_14,questionnaire_2_Q_15,relationships, moodrecordings" + "\n" +
            result.data.data;
        downloadFile('data.csv', result1);
    }).catch(function(error) {
        console.log(error);
    });

}

/**
 * Archive data for time frame (perform function call)
 * @param {string} start # Timestamp from (formatted)
 * @param {string} end # Timestamp to (formatted)
 */

archiveJob = (start, end) => {
    var archive = firebase.functions().httpsCallable("archive");
    archive({
        start: new Date(start).valueOf(),
        end: new Date(end).valueOf()
    });
    alert("Data marked as archived!");
}

/**
 * Request file download from memory
 * @param {string} fileName # Name of the file
 * @param {*} urlData # Data to store in the file
 */

function downloadFile(fileName, urlData) {
    var csv = urlData
    var downloadLink = document.createElement("a");
    var blob = new Blob(["\ufeff", csv]);
    var url = URL.createObjectURL(blob);
    downloadLink.href = url;
    downloadLink.download = fileName;

    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
}

/**
 * Get CSV for any time (button)
 * Calls getCSVTimeframe2 above with huge time frame
 */

function getCSV() {
    getCSVTimeframe2("01-01-2000", "01-01-3000");
}

/**
 * Archive data for timeframe (button)
 * Reads values and calls archiveJob above
 */

function archiveTimeframe() {
    var archiveStart = document.getElementById("ArchStart").value;
    var archiveEnd = document.getElementById("ArchEnd").value;

    if (archiveStart != "" && archiveEnd != "") {
        console.log("call archive(" + archiveStart + ", " + archiveEnd + ")");
        archiveJob(archiveStart, archiveEnd);
    } else {
        alert("Invalid date inputs");
    }
}