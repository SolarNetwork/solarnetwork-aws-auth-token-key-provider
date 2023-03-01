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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import net.solarnetwork.security.Snws2AuthorizationBuilder;

/**
 * Test cases fort the {@link TokenKeyProvider} class.
 * 
 * @author matt
 * @version 1.0
 */
public class TokenKeyProviderTests {

	@Test
	void missingSecret() {
		// GIVEN
		TokenKeyProvider provider = new TokenKeyProvider();

		// WHEN
		APIGatewayProxyResponseEvent result = provider.handleRequest(null, null);

		// THEN
		assertThat("Result provided", result, is(notNullValue()));
		assertThat("Error status", result.getStatusCode(), is(equalTo(500)));
		assertThat("Error result", result.getBody(),
				is(equalTo("{\"success\":false,\"message\":\"Token secret not avaialble.\"}")));
	}

	@Test
	void success() {
		// GIVEN
		final String tokenSecret = "foobar";
		TokenKeyProvider provider = new TokenKeyProvider(() -> tokenSecret);

		// WHEN
		APIGatewayProxyResponseEvent result = provider.handleRequest(null, null);

		// THEN
		final String signDate = AUTHORIZATION_DATE_FORMATTER.format(Instant.now()); // assume same day as when above executed
		final String key = new Snws2AuthorizationBuilder("").saveSigningKey(tokenSecret).signingKeyHex();
		assertThat("Result provided", result, is(notNullValue()));
		assertThat("Error status", result.getStatusCode(), is(equalTo(200)));
		assertThat("Error result", result.getBody(), is(equalTo(
				String.format("{\"success\":true,\"key\":\"%s\",\"date\":\"%s\"}", key, signDate))));
	}

}
