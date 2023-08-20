package ca.nevercoded.infra.keycloack;

import ca.nevercoded.infra.TokenValidationHandler;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class KeycloakGateway {

    private PublicKey getPublicKeyFromKeycloak(String realmUrl, String kid) throws Exception {
        URL url = new URL(realmUrl + "/protocol/openid-connect/certs");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(30000);
        connection.setConnectTimeout(30000);

        try (var streamReader = new InputStreamReader(connection.getInputStream())) {
            final TokenValidationHandler.Keys keys = gson.fromJson(streamReader, TokenValidationHandler.Keys.class);

            for (TokenValidationHandler.KeyEntry key : keys.keys) {
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
}
