package com.galois;

import com.galois.dto.request.PredictionRequestDto;
import com.galois.dto.result.PredictionResultDto;
import com.galois.enums.PredictionType;
import com.intellij.codeInsight.CodeSmellInfo;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.codeInsight.problems.WolfTheProblemSolverImpl;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.java19modules.JavaModuleNamingInspection;
import com.intellij.codeInspection.javaDoc.JavaDocLocalInspection;
import com.intellij.codeInspection.reflectiveAccess.JavaReflectionInvocationInspection;
import com.intellij.codeInspection.ui.InspectionTree;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.CodeSmellDetector;
import com.intellij.openapi.vcs.changes.PsiChangeTracker;
import com.intellij.patterns.PsiModifierListOwnerPattern;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GaloisCompleterProvider extends CompletionProvider<CompletionParameters> {

    private final GaloisAutocompleterService galoisAutocompleterService = new GaloisAutocompleterService();

    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {

        completionResultSet.restartCompletionOnAnyPrefixChange();

        if (!isActivationValidForElement(Objects.requireNonNull(completionParameters.getOriginalPosition()))) {
            completionResultSet.runRemainingContributors(completionParameters, true);
            return;
        }

        final String contextText = getContextText(completionParameters);
        final String prefix = completionResultSet.getPrefixMatcher().getPrefix();
        final String suffix = getCursorSuffix(completionParameters);

        List<PredictionResultDto> completionResults =
                galoisAutocompleterService.predict(new PredictionRequestDto(contextText));

        //final List<PredictionResultDto> validSuggestions = deleteInvalidSuggestions(completionParameters, completionResults);

        if (completionResults != null ) {

            final List<LookupElement> lookupElements = mapLookupElement(completionResults, prefix, suffix);

            if (!lookupElements.isEmpty()) {
                completionResultSet.addAllElements(lookupElements);
            }
        }
    }

    private boolean isActivationValidForElement(@NotNull final PsiElement psiElement) {
        if (psiElement instanceof PsiJavaToken) {
            return !((PsiJavaToken) psiElement).getTokenType().equals(JavaTokenType.IDENTIFIER);
        }
        return true;
    }

    private List<PredictionResultDto> deleteInvalidSuggestions(@NotNull CompletionParameters completionParameters, List<PredictionResultDto> predictions) {
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(completionParameters.getOriginalFile().getProject());
        for (PredictionResultDto prediction : predictions) {
            PsiFile tempFile = null;
            try {
                tempFile = fileFactory.createFileFromText(JavaLanguage.INSTANCE, getFullContextText(completionParameters) + prediction);
                String text = tempFile.getText();

                //Using DaemonCode
                //DaemonCodeAnalyzer codeAnalyzer = DaemonCodeAnalyzer.getInstance(tempFile.getProject());
                //List<HighlightInfo> errors = codeAnalyzer.(tempFile, completionParameters.getEditor().getDocument(),ProgressManager.getInstance().getProgressIndicator());

                //Using specific inspectors
                //InspectionManager inspectionManager = InspectionManager.getInstance(tempFile.getProject());
                //JavaDocLocalInspection inspection = new JavaDocLocalInspection();
                //JavaReflectionInvocationInspection inspection = new JavaReflectionInvocationInspection();
                //JavaModuleNamingInspection inspection = new JavaModuleNamingInspection();
                //List<ProblemDescriptor> checked = inspection.processFile(tempFile, inspectionManager);

                //Using CodeSmell API
                List<CodeSmellInfo> codeSmells = CodeSmellDetector.getInstance(tempFile.getProject()).findCodeSmells(Collections.singletonList(tempFile.getVirtualFile()));

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        return predictions;
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
                    if (lookupElement.getType().equals(PredictionType.LONG.name())) {
                        context.getDocument().deleteString(end, end + lookupElement.getSuffix().length());
                    }
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
        final int MAX_OFFSET = 3000;
        final String START_OF_TEXT_TOKEN = "<|startoftext|>";
        final Document document = completionParameters.getEditor().getDocument();
        final int middle = completionParameters.getOffset();
        final int begin = Integer.max(0, middle - MAX_OFFSET);
        final String documentRangeText = document.getText(new TextRange(begin, middle));

        return begin == 0 ? START_OF_TEXT_TOKEN + '\n' + documentRangeText : documentRangeText;
    }

    private String getFullContextText(@NotNull final CompletionParameters completionParameters) {
        final Document document = completionParameters.getEditor().getDocument();
        return document.getText(new TextRange(0, completionParameters.getOffset()));
    }

    private String getCursorSuffix(@NotNull final CompletionParameters parameters) {
        final Document document = parameters.getEditor().getDocument();
        final int cursorPosition = parameters.getOffset();
        final int lineNumber = document.getLineNumber(cursorPosition);
        final int lineEnd = document.getLineEndOffset(lineNumber);

        return document.getText(TextRange.create(cursorPosition, lineEnd)).trim();
    }
}
