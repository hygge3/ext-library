package ext.library.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * BaseAuth
 */
@RequiredArgsConstructor
public class BaseAuthenticator implements Authenticator {

    private final String userName;

    private final String password;

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String credential = Credentials.basic(userName, password, StandardCharsets.UTF_8);
        return response.request().newBuilder().header("Authorization", credential).build();
    }

}
