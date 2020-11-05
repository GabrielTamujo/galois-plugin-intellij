package com.galois;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author GabrielTamujo
 */
public class GaloisCompleterContributor extends CompletionContributor {

    public GaloisCompleterContributor() {
        extend(CompletionType.BASIC,
                psiElement(JavaTokenType.IDENTIFIER),
                new GaloisCompleterProvider("http://localhost:3030/"));
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return true;
    }

}
