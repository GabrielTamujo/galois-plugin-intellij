package com.galois;

import com.google.gson.Gson;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GaloisCompleterProvider extends CompletionProvider<CompletionParameters> {

    private GaloisAutocompleterService galoisAutocompleterService;

    private Gson gson;

    public GaloisCompleterProvider(String baseUrl) {
        this.galoisAutocompleterService = new GaloisAutocompleterService(baseUrl);
        this.gson = new Gson();
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {

        final int MAX_OFFSET = 1000;
        final String START_OF_TEXT_TOKEN = "<|startoftext|>";

        final Document document = completionParameters.getEditor().getDocument();
        final int middle = completionParameters.getOffset();
        final int begin = Integer.max(0, middle - MAX_OFFSET);
        final String text = START_OF_TEXT_TOKEN + document.getText(new TextRange(begin, middle));
        final String prefix = completionResultSet.getPrefixMatcher().getPrefix();

        final String response = galoisAutocompleterService
                .getCompletion(gson.toJson(new CompletionRequest(text)));

        completionResultSet.restartCompletionWhenNothingMatches();

        if (response != null) {

            final CompletionResult completionResult = gson.fromJson(response, CompletionResult.class);
            List<String> completionResults = completionResult.getResult();

            if (completionResults == null || completionResults.isEmpty()) {
                return;
            }

            List<LookupElement> lookupElements = mapLookupElement(completionResults, prefix);

            if (!lookupElements.isEmpty()) {
                completionResultSet.addAllElements(lookupElements);
            }
        }
    }

    private List<LookupElement> mapLookupElement(@NotNull List<String> completions, String prefix) {
        List<LookupElement> elementList = new ArrayList<>();
        completions.forEach(s -> {
            if (!s.isBlank()) {
                elementList.add(LookupElementBuilder.create(prefix.isBlank() ? s : prefix + s));
            }
        });
        return elementList;
    }

}
