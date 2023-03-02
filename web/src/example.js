/* eslint-env es6, browser, commonjs */
"use strict";

import {
	Configuration,
	AuthorizationV2Builder,
	HttpHeaders,
} from "solarnetwork-api-core";
import Hex from "crypto-js/enc-hex";

var app;

const exampleApp = function (options) {
	const self = { version: "1.0.0" };
	const config = Object.assign({}, options);

	/** @type HTMLFormElement */
	var settingsForm;

	/** @type HTMLElement */
	var resultContainer;

	/** @type HTMLButtonElement */
	var submitButton;

	function start() {
		settingsForm = document.getElementById("settings");
		settingsForm.onsubmit = formSubmit;
		submitButton = settingsForm.getElementsByTagName("button")[0];

		resultContainer = document.getElementById("results-container");
		return self;
	}

	function stop() {
		return self;
	}

	function resetResult() {
		while (resultContainer.firstChild) {
			resultContainer.removeChild(resultContainer.lastChild);
		}
		resultContainer.classList.add("hidden");
		resultContainer.classList.remove("alert-success");
		resultContainer.classList.remove("alert-warning");
	}

	function showError(msg) {
		resultContainer.innerText = msg;
		resultContainer.classList.add("alert-warning");
		resultContainer.classList.remove("hidden");
	}

	function formSubmit(event) {
		event.preventDefault();

		resetResult();

		const url = settingsForm.elements.url.value;
		if (!url) {
			showError("Please provide the service URL.");
			return;
		}

		const token = settingsForm.elements.token.value;
		if (!token) {
			showError("Please provide the security token.");
			return;
		}

		submitButton.disabled = true;

		// make signing key provider request
		fetch(url)
			.then((response) => {
				submitButton.disabled = false;
				if (!response.ok) {
					const msg = `HTTP error returned: status ${response.status}`;
					showError(msg);
					throw new Error(msg);
				}
				return response.json();
			})
			.then((signingKeyResponse) => {
				if (!signingKeyResponse.success) {
					showError(
						`Service unsuccessful: ${signingKeyResponse.message}`
					);
					return;
				}

				// make SN API request using signing key
				const auth = new AuthorizationV2Builder(token)
					.snDate(true)
					.key(Hex.parse(signingKeyResponse.key))
					.path("/solaruser/api/v1/sec/whoami");
				const headers = {};
				headers[HttpHeaders.AUTHORIZATION] = auth.buildWithSavedKey();
				headers[HttpHeaders.X_SN_DATE] = auth.requestDateHeaderValue;

				fetch(
					"https://data.solarnetwork.net/solaruser/api/v1/sec/whoami",
					{ headers }
				)
					.then((response) => {
						submitButton.disabled = false;
						if (!response.ok) {
							const msg =
								response.status === 403
									? "SolarNetwork authentication failed. Make sure the Lamda function is configured to use the secret associated with the Token you used."
									: `HTTP error returned from SolarNetwork API: status ${response.status}`;
							showError(msg);
							throw new Error(msg);
						}
						return response.json();
					})
					.then((snResponse) => {
						if (!snResponse.success) {
							showError(
								`Service unsuccessful: ${snResponse.message}`
							);
							return;
						}
						resultContainer.innerHTML = `<h4>Signing Key Response</h4><p>${JSON.stringify(
							signingKeyResponse
						)}</p><h4>SolarNetwork API Response</h4><p>${JSON.stringify(
							snResponse
						)}</p>`;
						resultContainer.classList.add("alert-success");
						resultContainer.classList.remove("hidden");
					});
			});
		return false;
	}

	return Object.defineProperties(self, {
		start: { value: start },
		stop: { value: stop },
	});
};

export default function startApp() {
	const config = new Configuration();

	app = exampleApp(config).start();

	window.onbeforeunload = function () {
		app.stop();
	};

	return app;
}
