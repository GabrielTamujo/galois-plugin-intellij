package com.galois;

import com.galois.dto.request.PredictionAcceptedRequestDto;
import com.galois.dto.request.PredictionRequestDto;
import com.galois.dto.result.PredictionResultDto;
import com.galois.enums.PredictionType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GabrielTamujo
 */
public class GaloisCompleterContributor extends CompletionContributor {

    private final GaloisAutocompleterService galoisAutocompleterService = new GaloisAutocompleterService();

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet) {

        completionResultSet.restartCompletionOnAnyPrefixChange();

        final String contextText = getContextText(completionParameters);
        final String prefix = completionResultSet.getPrefixMatcher().getPrefix();
        final String suffix = getCursorSuffix(completionParameters);

        final List<PredictionResultDto> completionResults =
                galoisAutocompleterService.predict(new PredictionRequestDto(contextText));

        if (completionResults != null) {

            final List<LookupElement> lookupElements = mapLookupElement(completionResults, prefix, suffix);

            if (!lookupElements.isEmpty()) {
                completionResultSet.addAllElements(lookupElements);
            }
        }

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
                            new PredictionAcceptedRequestDto(lookupElement.getInsertText(), lookupElement.getType()));
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
        final int cursorPosition = parameters.getOffset();
        final int lineNumber = document.getLineNumber(cursorPosition);
        final int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }

}
