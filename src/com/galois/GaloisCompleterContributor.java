package com.galois;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author GabrielTamujo
 */
public class GaloisCompleterContributor extends CompletionContributor {
    public GaloisCompleterContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(JavaLanguage.INSTANCE),
                new GaloisCompleterProvider("http://localhost:3030/"));
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        final char[] triggers = {
                ' ',
                '_',
                '.',
                '(',
                ')',
                '{',
                '}',
                '[',
                ']',
                ',',
                ':',
                '\'',
                '"',
                '=',
                '<',
                '>',
                '/',
                '\\',
                '+',
                '-',
                '|',
                '&',
                '*',
                '%',
                '=',
                '$',
                '#',
                '@',
                '!'};

        for (char trigger : triggers) {
            if (typeChar == trigger) {
                return true;
            }
        }
        return false;
    }
}
