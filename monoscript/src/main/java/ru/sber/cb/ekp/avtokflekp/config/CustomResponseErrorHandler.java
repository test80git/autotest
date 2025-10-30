package ru.sber.cb.ekp.avtokflekp.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {


    @Override
    public void handleError(@NotNull ClientHttpResponse response) throws IOException {
        if (!isHandled(response.getStatusCode().value())) {
            super.handleError(response);
        }
    }

    private boolean isHandled(int statusCode) {
        return statusCode == 400 || statusCode == 404 || statusCode == 500;
    }
}
