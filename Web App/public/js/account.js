nav("a5");
darkCheck();

/**
 * Change password button method
 * Read values and call changePassword below
 */

function changePwButton() {

    changePassword(document.getElementById("oldpassword").value, document.getElementById("newpassword").value);
}

/**
 * Change admin password
 * @param {string} oldPassword # Old password
 * @param {string} newPassword # New password
 */

function changePassword(oldPassword, newPassword) {

    credential = firebase.auth.EmailAuthProvider.credential("example@example.de", oldPassword);
    firebase.auth().currentUser
        .reauthenticateWithCredential(credential)
        .then(function() {
            console.log("Reauthenticated successfully");
            auth.currentUser
                .updatePassword(newPassword)
                .then(function() {
                    alert("Password change successful");
                    document.getElementById("oldpassword").value = "";
                    document.getElementById("newpassword").value = "";
                }).catch(function(error) {
                    alert("Password change failed. Make sure you entered valid characters.");
                });
        }).catch(function(error) {
            alert("Reauthentication failed. Make sure that the password you entered is correct.");
        });
}

/**
 * Log out
 */

function logout() {
    auth.signOut();
}

/**
 * Redirect to index if not logged in
 */

auth.onAuthStateChanged(firebaseUser => {
    if (!firebaseUser) {
        window.location = "index.html"
    }
});