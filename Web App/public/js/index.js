var firebaseConfig = {
    apiKey: "AIzaSyAX7S7J8QOPW8KqLxx6a1jQrxKnVaQnieY",
    authDomain: "angrynerds-dac9e.firebaseapp.com",
    databaseURL: "https://angrynerds-dac9e.firebaseio.com",
    projectId: "angrynerds-dac9e",
    storageBucket: "angrynerds-dac9e.appspot.com",
    messagingSenderId: "1060747338361",
    appId: "1:1060747338361:web:48cca16f95da80e1812aa8",
    measurementId: "G-4J5Z6KYMJC"
};

firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();

/**
 * Login (button)
 * Reads password and performs log in
 * Dummy email address is used
 */

function login() {
    //var email = document.getElementById("email").value;
    var email = "example@example.de";
    var password = document.getElementById("pw").value;
    auth.signInWithEmailAndPassword(email, password)
        .catch(e => {
            console.log(e.message);
            document.getElementById("error").innerHTML = e.message;
        });
}

/**
 * Redirect to account page once logged in
 */

auth.onAuthStateChanged(firebaseUser => {
    if (firebaseUser) {
        console.log(firebaseUser);
        window.location = "account.html"
    }
});