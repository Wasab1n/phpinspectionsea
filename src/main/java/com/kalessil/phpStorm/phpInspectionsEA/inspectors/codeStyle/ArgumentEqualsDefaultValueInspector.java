package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpeanapiEquivalenceUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) David Rodrigues <david.proweb@gmail.com>
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ArgumentEqualsDefaultValueInspector extends BasePhpInspection {
    private static final String message = "The argument can be safely dropped, as it's identical to the default value.";

    private static final Set<String> specialFunctions = new HashSet<>();
    private static final Set<String> specialConstants = new HashSet<>();
    static {
        /* in exceptions die to conflict with strict types inspection, which requires argument specification */
        specialFunctions.add("array_search");
        specialFunctions.add("in_array");

        specialConstants.add("__LINE__");
        specialConstants.add("__FILE__");
        specialConstants.add("__DIR__");
        specialConstants.add("__FUNCTION__");
        specialConstants.add("__CLASS__");
        specialConstants.add("__TRAIT__");
        specialConstants.add("__METHOD__");
        specialConstants.add("__NAMESPACE__");
    }

    @NotNull
    public final String getShortName() {
        return "ArgumentEqualsDefaultValueInspection";
    }

    @NotNull
    @Override
    public final PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, final boolean onTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                this.analyze(reference);
            }

            @Override
            public void visitPhpFunctionCall(@NotNull FunctionReference reference) {
                this.analyze(reference);
            }

            private void analyze(@NotNull FunctionReference reference) {
                final PsiElement[] arguments = reference.getParameters();
                final String functionName    = reference.getName();
                if (arguments.length > 0 && functionName != null && !specialFunctions.contains(functionName)) {
                    PsiElement reportFrom = null;
                    PsiElement reportTo   = null;

                    if (OpenapiTypesUtil.DEFAULT_VALUES.contains(arguments[arguments.length - 1].getNode().getElementType())) {
                        final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                        if (resolved instanceof Function) {
                            final Parameter[] parameters = ((Function) resolved).getParameters();
                            if (arguments.length <= parameters.length) {
                                for (int index = Math.min(parameters.length, arguments.length) - 1; index >= 0; --index) {
                                    final PsiElement value = parameters[index].getDefaultValue();
                                    /* false-positives: magic constants */
                                    if (value instanceof ConstantReference && specialConstants.contains(value.getText())) {
                                        break;
                                    }
                                    /* false-positives: unmatched values */
                                    final PsiElement argument = arguments[index];
                                    if (value == null || !OpeanapiEquivalenceUtil.areEqual(value, argument)) {
                                        break;
                                    }

                                    reportFrom = argument;
                                    if (reportTo == null) {
                                        reportTo = argument;
                                    }
                                }
                            }
                        }
                    }

                    if (reportFrom != null) {
                        problemsHolder.registerProblem(
                                problemsHolder.getManager().createProblemDescriptor(
                                        reportFrom,
                                        reportTo,
                                        message,
                                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                        onTheFly,
                                        new TheLocalFix(reportFrom, reportTo)
                                )
                        );
                    }
                }
            }
        };
    }

    private static class TheLocalFix implements LocalQuickFix {
        private final SmartPsiElementPointer<PsiElement> dropFrom;
        private final SmartPsiElementPointer<PsiElement> dropTo;

        private TheLocalFix(@NotNull PsiElement dropFrom, @NotNull PsiElement dropTo) {
            final SmartPointerManager manager = SmartPointerManager.getInstance(dropFrom.getProject());

            this.dropFrom = manager.createSmartPsiElementPointer(dropFrom);
            this.dropTo   = manager.createSmartPsiElementPointer(dropTo);
        }

        @NotNull
        @Override
        public String getName() {
            return "Drop unneeded arguments";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
            PsiElement dropFrom     = this.dropFrom.getElement();
            final PsiElement dropTo = this.dropTo.getElement();
            if (dropFrom != null && dropTo != null) {
                PsiElement   previous = dropFrom.getPrevSibling();
                IElementType prevType = previous == null ? null : previous.getNode().getElementType();
                while (
                    (prevType == PhpTokenTypes.opCOMMA && previous != null) ||
                    previous instanceof PsiWhiteSpace || previous instanceof PsiComment
                ) {
                    dropFrom = previous;
                    previous = previous.getPrevSibling();
                    prevType = previous == null ? null : previous.getNode().getElementType();
                }
                dropFrom.getParent().deleteChildRange(dropFrom, dropTo);
            }
        }
    }
}
