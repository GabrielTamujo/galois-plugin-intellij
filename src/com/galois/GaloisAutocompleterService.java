package com.galois;

import com.galois.dto.request.PredictionAcceptedRequestDto;
import com.galois.dto.request.PredictionRequestDto;
import com.galois.dto.result.PredictionListResultDto;
import com.galois.dto.result.PredictionResultDto;
import com.galois.settings.AppSettingsState;
import com.google.gson.Gson;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GaloisAutocompleterService {

    private final AppSettingsState appSettingsState;

    private final HttpClient client;

    private final Gson gson;

    public GaloisAutocompleterService() {
        this.appSettingsState = AppSettingsState.getInstance();
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public List<PredictionResultDto> predict(@NotNull final PredictionRequestDto predictionRequestDto) {
        final String AUTOCOMPLETE_ROUTE = "autocomplete";

        try {
            return ApplicationUtil.runWithCheckCanceled(() -> {
                final HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(appSettingsState.galoisApiUrl + AUTOCOMPLETE_ROUTE))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(predictionRequestDto)))
                        .build();
                final HttpResponse<String> response = client
                        .send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200
                        && response.body() != null
                        && !response.body().isEmpty()) {
                    return gson.fromJson(response.body(), PredictionListResultDto.class).getResult();
                }
                return null;
            }, ProgressManager.getInstance().getProgressIndicator());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void reportAcceptedPrediction(@NotNull final PredictionAcceptedRequestDto predictionAcceptedRequestDto) {
        final String ACCEPTANCE_ROUTE = "acceptance";
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(appSettingsState.galoisApiUrl + ACCEPTANCE_ROUTE))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(predictionAcceptedRequestDto)))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
