package com.galois;

import com.galois.dto.request.PredictionAcceptedRequestDto;
import com.galois.dto.request.PredictionRequestDto;
import com.galois.dto.result.PredictionListResultDto;
import com.galois.dto.result.PredictionResultDto;
import com.galois.enums.PredictionType;
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
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        completionResultSet.restartCompletionOnAnyPrefixChange();

        final String contextText = getContextText(completionParameters);
        final String prefix = completionResultSet.getPrefixMatcher().getPrefix();
        final String suffix = getCursorSuffix(completionParameters);

        if (!isActivationValidForElement(Objects.requireNonNull(completionParameters.getOriginalPosition()))) {
            completionResultSet.stopHere();
            return;
        }

        final String response = galoisAutocompleterService.predict(gson.toJson(new PredictionRequestDto(contextText)));

        if (response != null && !response.isEmpty()) {

            final PredictionListResultDto predictionListResultDto = gson.fromJson(response, PredictionListResultDto.class);
            final List<PredictionResultDto> completionResults = predictionListResultDto.getResult();

            if (completionResults == null || completionResults.isEmpty()) {
                completionResultSet.stopHere();
                return;
            }

            final List<LookupElement> lookupElements = mapLookupElement(completionResults, prefix, suffix);

            if (!lookupElements.isEmpty()) {
                completionResultSet.addAllElements(lookupElements);
            }
        }
    }

    private boolean isActivationValidForElement(@NotNull final PsiElement psiElement) {
        if (psiElement instanceof PsiJavaToken) {
            if (((PsiJavaToken) psiElement).getTokenType().equals(JavaTokenType.IDENTIFIER)) {
                final int textLength = psiElement.getTextLength();
                return textLength % 3 == 0 && textLength >= 3;
            }
        }
        return true;
    }

    private List<LookupElement> mapLookupElement(@NotNull final List<PredictionResultDto> completions,
                                                 @NotNull final String prefix,
                                                 @NotNull final String suffix) {

        List<LookupElement> elementList = new ArrayList<>();
        completions.forEach(prediction -> {
            if (!prediction.getPrediction().isBlank()) {
                elementList.add(createLookupElement(prediction, prefix, suffix));
            }
        });
        return elementList;
    }

    private LookupElementBuilder createLookupElement(@NotNull final PredictionResultDto prediction,
                                                     @NotNull final String prefix,
                                                     @NotNull final String suffix) {

        final String completion = prediction.getPrediction();
        final GaloisLookUpElement galoisLookUpElement = new GaloisLookUpElement(
                prefix.isBlank() ? completion : prefix + completion,
                prediction.getType(),
                suffix);

        return LookupElementBuilder.create(galoisLookUpElement, galoisLookUpElement.getInsertText())
                .withInsertHandler((context, item) -> {
                    final GaloisLookUpElement lookupElement = (GaloisLookUpElement) item.getObject();
                    final int end = context.getTailOffset();
                    if (lookupElement.getType().equals(PredictionType.MULTIPLE_TOKENS.name())) {
                        context.getDocument().deleteString(end, end + lookupElement.getSuffix().length());
                    }
                    galoisAutocompleterService.reportAcceptedPrediction(
                            gson.toJson(new PredictionAcceptedRequestDto(lookupElement.getInsertText(), lookupElement.getType())));
                }).withRenderer(new LookupElementRenderer<LookupElement>() {
                    @Override
                    public void renderElement(LookupElement lookupElement, LookupElementPresentation lookupElementPresentation) {
                        final GaloisLookUpElement prediction = (GaloisLookUpElement) lookupElement.getObject();
                        lookupElementPresentation.setItemText(prediction.getInsertText());
                        lookupElementPresentation.setItemTextBold(true);
                    }
                });
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

    private String getCursorSuffix(@NotNull final CompletionParameters parameters) {
        final Document document = parameters.getEditor().getDocument();
        int cursorPosition = parameters.getOffset();
        int lineNumber = document.getLineNumber(cursorPosition);
        int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }

}
