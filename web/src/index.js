import "bootstrap/dist/css/bootstrap.min.css";
import "./favicon.png";
import "./example.css";

import startApp from "./example.js";

if (!window.isLoaded) {
	window.addEventListener(
		"load",
		function () {
			startApp();
		},
		false
	);
} else {
	startApp();
}
