package ca.nevercoded.infra.http.core;

import ca.nevercoded.infra.keycloack.KeycloakGateway;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.keycloak.jose.jws.JWSInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;

public class TokenValidationHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TokenValidationHandler.class);

    private final HttpHandler next;
    private final KeycloakGateway keycloakGateway;

    public TokenValidationHandler(HttpHandler next, KeycloakGateway keycloakGateway) {
        this.next = next;
        this.keycloakGateway = keycloakGateway;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);  // Remove "Bearer " prefix

            // Verify the token
            try {
                JWSInput jws = new JWSInput(token);
                String kid = jws.getHeader().getKeyId();
                PublicKey publicKey = keycloakGateway.getPublicKey(kid);
                JwtParser parser = Jwts.parser();
                Jws<Claims> claimsJws = parser.setSigningKey(publicKey).parseClaimsJws(token);

                // Token is valid; now you can also inspect the token claims if necessary
                Claims claims = claimsJws.getBody();

                // Continue with your business logic...
                next.handle(exchange);

            } catch (Throwable err) {
                LOG.error("Unauthorized", err);
                exchange.sendResponseHeaders(401, 0);
                OutputStream os = exchange.getResponseBody();
                os.write("Unauthorized".getBytes());
                os.close();
            }
        } else {
            exchange.sendResponseHeaders(400, 0);
            OutputStream os = exchange.getResponseBody();
            os.write("Bad Request".getBytes());
            os.close();
        }
    }
}
