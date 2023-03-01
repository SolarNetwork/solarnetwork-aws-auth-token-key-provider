/* ==================================================================
 * Copyright 2022 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.s10k.aws.lamda.security.keyprovider;

import static net.solarnetwork.security.AuthorizationUtils.AUTHORIZATION_DATE_FORMATTER;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.function.Supplier;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import net.solarnetwork.security.Snws2AuthorizationBuilder;
import net.solarnetwork.util.ObjectUtils;

/**
 * Very basic signing key provider for an environment-provided token secret.
 * 
 * <p>
 * The purpose of this lamda function is to provide a SolarNetwork token signing
 * key to clients, without exposing the actual token secret. This implementation
 * serves as an example only, where the token secret is provided to the function
 * via an environment variable. A more secure method of handling the token
 * secret would be to make use of AWS's encrypted environment variables or
 * Secret Manager.
 * </p>
 * 
 * <p>
 * Returns a JSON response like:
 * </p>
 * 
 * <pre>{@code {
 *   "success": true,
 *   "key": "hex string signing key",
 *   "date": "YYYYMMDD date string"
 * }}</pre>
 * 
 * @author matt
 * @version 1.0
 */
public class TokenKeyProvider
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final Supplier<String> secretSupplier;

	/**
	 * Default constructor.
	 * 
	 * <p>
	 * Uses a token secret provided by the <code>SN_TOKEN_SECRET</code>
	 * environment variable.
	 * </p>
	 */
	public TokenKeyProvider() {
		this(() -> System.getenv("SN_TOKEN_SECRET"));
	}

	/**
	 * Constructor.
	 * 
	 * @param secretProvider
	 *        the token secret supplier
	 */
	public TokenKeyProvider(Supplier<String> secretSupplier) {
		this.secretSupplier = ObjectUtils.requireNonNullArgument(secretSupplier, "secretSupplier");
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event,
			Context context) {
		final APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setIsBase64Encoded(false);
		response.setHeaders(Collections.singletonMap("Content-Type", "application/json"));

		String tokenSecret = secretSupplier.get();
		if ( tokenSecret == null || tokenSecret.isBlank() ) {
			response.setStatusCode(500);
			response.setBody("{\"success\":false,\"message\":\"Token secret not avaialble.\"}");
		} else {
			Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
			String signingKeyHex = new Snws2AuthorizationBuilder("").date(now)
					.saveSigningKey(tokenSecret).signingKeyHex();
			response.setStatusCode(200);
			response.setBody(String.format("{\"success\":true,\"key\":\"%s\",\"date\":\"%s\"}",
					signingKeyHex, AUTHORIZATION_DATE_FORMATTER.format(now)));
		}

		return response;
	}

}
