package com.galois;

import com.galois.dto.*;
import com.google.gson.Gson;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GaloisCompleterProvider extends CompletionProvider<CompletionParameters> {

    private final GaloisAutocompleterService galoisAutocompleterService;

    private final Gson gson;

    public GaloisCompleterProvider(String baseUrl) {
        this.galoisAutocompleterService = new GaloisAutocompleterService(baseUrl);
        this.gson = new Gson();
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {

        final String contextText = getContextText(completionParameters);
        final String prefix = completionResultSet.getPrefixMatcher().getPrefix();
        final String suffix = getCursorSuffix(completionParameters);

        final String response = galoisAutocompleterService
                .createPredictions(gson.toJson(new PredictionRequestDto(contextText)));

        completionResultSet.restartCompletionWhenNothingMatches();

        if (response != null) {

            final PredictionListResultDto predictionListResultDto = gson.fromJson(response, PredictionListResultDto.class);
            List<PredictionResultDto> completionResults = predictionListResultDto.getResult();

            if (completionResults == null || completionResults.isEmpty()) {
                return;
            }

            List<LookupElement> lookupElements = mapLookupElement(completionResults, prefix, suffix);

            if (!lookupElements.isEmpty()) {
                completionResultSet.addAllElements(lookupElements);
            }
        }
    }

    private String getContextText(@NotNull final CompletionParameters completionParameters) {
        final int MAX_OFFSET = 1000;
        final String START_OF_TEXT_TOKEN = "<|startoftext|>";
        final Document document = completionParameters.getEditor().getDocument();
        final int middle = completionParameters.getOffset();
        final int begin = Integer.max(0, middle - MAX_OFFSET);
        final String documentRangeText = document.getText(new TextRange(begin, middle));
        return begin == 0 ? START_OF_TEXT_TOKEN + '\n' + documentRangeText : documentRangeText;
    }

    private List<LookupElement> mapLookupElement(@NotNull List<PredictionResultDto> completions, String prefix, String suffix) {
        List<LookupElement> elementList = new ArrayList<>();
        completions.forEach(prediction -> {
            if (!prediction.getPrediction().isBlank()) {
                elementList.add(createLookupElement(prediction, prefix, suffix));
            }
        });
        return elementList;
    }

    private LookupElementBuilder createLookupElement(PredictionResultDto prediction, String prefix, String suffix) {
        GaloisCompletionDto completionDto = new GaloisCompletionDto(
                prediction.getPrediction(),
                prediction.getType(),
                prefix,
                suffix
        );
        return LookupElementBuilder.create(completionDto, completionDto.getPrediction())
                .withInsertHandler((context, item) -> {
                    final GaloisCompletionDto lookupElement = (GaloisCompletionDto) item.getObject();
                    final int end = context.getTailOffset();
                    if (lookupElement.getType().equals("MULTIPLE_TOKENS")) {
                        context.getDocument().deleteString(end, end + lookupElement.getSuffix().length());
                    }
                    galoisAutocompleterService.saveAcceptedPrediction(
                            gson.toJson(new PredictionAcceptedRequestDto(lookupElement.getPrediction(), lookupElement.getType())));
                }).withRenderer(new LookupElementRenderer<LookupElement>() {
                    @Override
                    public void renderElement(LookupElement lookupElement, LookupElementPresentation lookupElementPresentation) {
                        final GaloisCompletionDto prediction = (GaloisCompletionDto) lookupElement.getObject();
                        final String prefix = prediction.getPrefix();
                        final String lookupString = prediction.getPrediction();
                        lookupElementPresentation.setItemTextBold(true);
                        lookupElementPresentation.setItemText(prefix.isBlank() ? lookupString : prefix + lookupString);
                    }
                });
    }

    private String getCursorSuffix(CompletionParameters parameters) {
        Document document = parameters.getEditor().getDocument();
        int cursorPosition = parameters.getOffset();
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }

}
