package ca.nevercoded.infra.keycloack;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class KeycloakGateway {

    private static final String REALM_URL = "http://localhost:8092/realms/NeverCoded";

    private final Gson gson;

    public KeycloakGateway(Gson gson) {
        this.gson = gson;
    }

    public PublicKey getPublicKey(String kid) throws Exception {
        final URL url = new URL(REALM_URL + "/protocol/openid-connect/certs");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(30000);
        connection.setConnectTimeout(30000);

        try (var streamReader = new InputStreamReader(connection.getInputStream())) {
            final var certs = gson.fromJson(streamReader, Certs.class);
            for (final var key : certs.keys()) {
                if (kid.equals(key.kid())) {
                    final String modulusBase64 = key.n();
                    final String exponentBase64 = key.e();
                    // Convert Base64 -> BigInt -> RSA KeySpec -> RSAPublicKey
                    final RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                            new BigInteger(1, Base64.getUrlDecoder().decode(modulusBase64)),
                            new BigInteger(1, Base64.getUrlDecoder().decode(exponentBase64))
                    );
                    return KeyFactory.getInstance("RSA").generatePublic(keySpec);
                }
            }
        }
        throw new RuntimeException("Public key not found in Keycloak");
    }
}
