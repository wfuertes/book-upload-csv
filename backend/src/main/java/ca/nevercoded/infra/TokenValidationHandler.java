package ca.nevercoded.infra;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.keycloak.jose.jws.JWSInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

public class TokenValidationHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TokenValidationHandler.class);

    private final HttpHandler next;
    private final Gson gson;

    public TokenValidationHandler(HttpHandler next, Gson gson) {
        this.next = next;
        this.gson = gson;
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

                // Fetch public key from Keycloak to verify the token
                String realmUrl = "http://localhost:8092/realms/NeverCoded";
                PublicKey publicKey = getPublicKeyFromKeycloak(realmUrl, kid);

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

    private PublicKey getPublicKeyFromKeycloak(String realmUrl, String kid) throws Exception {
        URL url = new URL(realmUrl + "/protocol/openid-connect/certs");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(30000);
        connection.setConnectTimeout(30000);

        try (var streamReader = new InputStreamReader(connection.getInputStream())) {
            final Keys keys = gson.fromJson(streamReader, Keys.class);

            for (KeyEntry key : keys.keys) {
                if (kid.equals(key.kid)) {
                    String modulusBase64 = key.n;
                    String exponentBase64 = key.e;

                    // Convert Base64 -> BigInt -> RSA KeySpec -> RSAPublicKey
                    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                            new BigInteger(1, Base64.getUrlDecoder().decode(modulusBase64)),
                            new BigInteger(1, Base64.getUrlDecoder().decode(exponentBase64))
                    );
                    return KeyFactory.getInstance("RSA").generatePublic(keySpec);
                }
            }
        }
        throw new RuntimeException("Public key not found in Keycloak");
    }

    private static class KeyEntry {
        String kid;
        String n;
        String e;
    }

    private static class Keys {
        List<KeyEntry> keys;
    }
}
