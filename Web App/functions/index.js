// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);


/**
 * Returns the user ID for a unique user code, given the code is valid.
 * @param {string} code # The unique user code
 */

const codeToUid = async code => {
    let users = (await admin.database().ref("Users").once('value')).val();
    for (let uid in users) {
        let user = users[uid];
        if (user.profile.user_code == code)
            return { uid: uid };
    }
}

/**
 * Function interface for finding user ID by unique code
 * @param {object} options # Options object
 * @param {string} options.code # The unique user code
 */

exports.codeToUid = functions.https.onCall(async(data, context) => {
    return codeToUid(data.code);
});

/**
 * Request to generate a new unique code for the given user
 * @param {object} options # Options object
 * @param {string} options.uid # The user ID of the requestant
 */

exports.ucode = functions.https.onCall(async(data, context) => {
    const id = data.uid;
    let userCode;
    while (!userCode || await codeToUid(userCode)) {
        userCode = Math.random().toString(36).substr(2, 6);
    }
    await admin.database().ref("Users/" + id + "/profile").update({
        user_code: userCode
    });
});

/**
 * Request to archive recorded data
 * @param {object} options # Options object
 * @param {long} options.start # Timestamp from
 * @param {long} options.end # Timestamp to
 */

exports.archive = functions.https.onCall(async (data, context) => {
    let users = (await admin.database().ref("Users").once("value")).val();
    for(let uid in users) {
        let udata = users[uid];
        for(let timestamp in udata.moods || {}) {
            if(timestamp >= data.start && timestamp <= data.end && !udata.moods[timestamp].archived) {
                await admin.database().ref(`Users/${uid}/moods/${timestamp}`).update({
                    archived: true
                });
            }
        }
    }
});

/**
 * Add mood recording to the database
 * @param {object} options # Options object
 * @param {string} options.text # The entire mood data in text form
 */

exports.addMessage = functions.https.onCall((data, context) => {
    const datata = data.text;
    const id = data.userid;
    var date = new Date(data.date);
    const timestamp = date.valueOf();
    var dataArray = datata.split(';');
    var companionsArray = dataArray[2].split(',');
    var situationsArray = dataArray[3].split(',');
    var companionsIDArray = dataArray[4].split(',');
    var notificationTime = dataArray[5];
    var voluntaryFlag = dataArray[6];
    admin.database().ref("Users/" + id + "/moods/" + timestamp).update({
        mood: dataArray[0],
        stress: dataArray[1],
        time: data.date,
        notification_time: notificationTime,
        voluntary_flag: voluntaryFlag
    });
    for (var i = 0; i < companionsArray.length; i++) {
        admin.database().ref("Users/" + id + "/moods/" + timestamp + "/companions/").update({
            [i]: companionsArray[i]
        });
    }

    for (var i = 0; i < situationsArray.length; i++) {
        admin.database().ref("Users/" + id + "/moods/" + timestamp + "/situations/").update({
            [i]: situationsArray[i]
        });
    }

    for (var i = 0; i < companionsIDArray.length; i++) {
        admin.database().ref("Users/" + id + "/moods/" + timestamp + "/companions_id/").update({
            [i]: companionsIDArray[i]
        });
    }

    return { data123: dataArray[0] };
});

/**
 * Get the consent form
 */

exports.getConsent = functions.https.onCall(async(data, context) => {
    let sn1 = await admin.database().ref('/Settings/consent_version').once('value');
    let sn = await admin.database().ref(`/Settings/consentforms/consent_form${sn1.val()}/consent_text`).once('value');
    return { data321: sn.val() };
});

/**
 * Delete user account
 * @param {object} options # Options object
 * @param {string} options.userid # The ID of the user to delete
 */

exports.deleteUser = functions.https.onCall((data, context) => {
    userID = data.userid;
    admin.database().ref("/Users/" + userID).remove();
    return { success: "success" };
});

/**
 * Set accepted consent form version for new users
 * @param {object} options # Options object
 * @param {string} options.uid # The ID of the signed up user
 */

exports.setCSVVersion = functions.https.onCall(async(data, context) => {
    let ver = (await admin.database().ref('/Settings/consent_version').once('value')).val();
    var id = data.uid;
    admin.database().ref("Users/" + id + "/profile").update({
        consent_accepted_version: ver
    });
    console.log("Success");
    return { success: "success" };
});

/**
 * Download CSV
 * @param {object} options # Options object
 * @param {long} options.start # Timestamp from
 * @param {long} options.end # Timestamp to
 */

exports.getCSV = functions.https.onCall(async(data, context) => {
    var startdate = data.start;
    var enddate = data.end;

    var state = await admin.database().ref("/Users").once('value');
    var val = state.val(),
        output = "";
    for (let userID in val) {
        let user = val[userID];
        output += userID + ",";
        output += new Date(user.profile.joined) + ",";
        output += "-1,";
        output += user.profile.consent_accepted_version + ",";
        output += "1 (accepted),";
        output += "Personality Questionnaire,"
        var i = 0;
        for (let answer of user.profile.questionnaire) {
            output += answer.replace(",", ";") + ",";
            if (i == 14) {
                output += "Emotional contagion scale,";
            }
            i++;
        }

        var output2 = "";
        for (let companion of user.companions || []) {
            output2 += companion.uid + " / ";
            output2 += new Date(companion.start) + " / ";
            output2 += (companion.end ? new Date(companion.end) : -1) + " / ";
            output2 += companion.relationship + " -/////- ";
        }
        if (output2 != "") {
            output += output2.substring(0, output2.length - 9);
        }
        output += ",";
        for (let moodrecording in user.moods || {}) {

            if (startdate >= moodrecording || enddate <= moodrecording) {
                continue;
            }
            let mood = user.moods[moodrecording];
            output += moodrecording + " / ";
            output += mood.voluntary_flag + " / ";
            output += mood.notification_time + " / ";
            output += mood.time + " / ";
            output += mood.mood + " / ";
            output += mood.stress + " / ";
            for (let companionID of mood.companions_id) {
                output += companionID + " "
            }
            output += " / "
            for (let situation of mood.situations) {
                output += situation + " ";
            }
            output += " / "
            output += "-1 (no_gps),";
        }
        output += "\n"
    }

    return {
        data: output
    }
});