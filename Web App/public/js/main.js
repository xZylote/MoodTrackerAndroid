class Admin {
    constructor(email, password) {
        this.email = email, this.password = password;
    }
}

/**
 * Handle dark mode
 */

function darkCheck() {
    if (localStorage.getItem("darkMode") == "on") {

        inputs = document.getElementsByTagName("input");
        for (i = 0; i < inputs.length; i++) {
            inputs[i].style.backgroundColor = "#999999";
            inputs[i].style.borderRadius = "5px";
        }
        document.getElementById("menu").style.background = "#002e4d";
        a = document.getElementsByTagName("a");
        for (i = 0; i < a.length; i++) {
            a[i].style.color = "gainsboro";
            a[i].classList.add("dark");
        }
        document.getElementById("body").classList.add("dark");

    } else {

        inputs = document.getElementsByTagName("input");
        for (i = 0; i < inputs.length; i++) {
            inputs[i].style.backgroundColor = "";
            inputs[i].style.borderRadius = "0";
        }
        document.getElementById("menu").style.background = "";
        a = document.getElementsByTagName("a");
        for (i = 0; i < a.length; i++) {
            a[i].style.color = "";
            a[i].classList.remove("dark");
        }
        document.getElementById("body").classList.remove("dark");
    }
}

function nav(x) {
    if (localStorage.getItem("darkMode") == "on") {
        document.getElementById(x).style.background = "#00497a";
        document.getElementById(x).style.borderRadius = "10px";
    } else {
        document.getElementById(x).style.background = "#c8ebe6";
        document.getElementById(x).style.borderRadius = "0";
    }

}

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

//If you are not logged in, you will be redirected to the login page
firebase.auth().onAuthStateChanged(firebaseUser => {
    if (!firebaseUser) {
        window.location = "index.html";
    }
});