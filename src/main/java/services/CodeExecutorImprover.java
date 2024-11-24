package services;

import models.ExecutionResult;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import settings.PluginSettings;

public class CodeExecutorImprover {
    private final CodeExecutor codeExecutor;
    private final CodeWriterService codeWriterService;
    private final OpenAIService openAiService;
    private final boolean isKotlin;

    public CodeExecutorImprover(CodeExecutor codeExecutor,
                                Project project,
                                OpenAIService openAiService,
                                boolean isKotlin) {
        this.codeExecutor = codeExecutor;
        this.codeWriterService = new CodeWriterService(project);
        this.openAiService = openAiService;
        this.isKotlin = isKotlin;
    }

    public ExecutionResult executeAndImproveCode(@NotNull String sourceCode,
                                                 @NotNull ProgressIndicator indicator) throws Exception {
        int attempts = 0;
        int maxAttempts = PluginSettings.getInstance().getMaxIterations();
        ExecutionResult result;
        String currentCode = sourceCode;
        String lastWorkingCode = null;

        do {
            indicator.setText("Attempt " + (attempts + 1) + " of " + maxAttempts);

            result = codeExecutor.execute(currentCode, indicator, isKotlin);

            if (result.isSuccess()) {
                lastWorkingCode = currentCode;
            }

            if (!result.isSuccess() && attempts < maxAttempts) {
                currentCode = improveCode(currentCode, result.getErrors());

                attempts++;
                indicator.setFraction((double) attempts / maxAttempts);
            } else {
                break;
            }
        } while (true);

        if (lastWorkingCode != null) {
            codeWriterService.updateCodeWithImprovement(lastWorkingCode);
        }

        return result;
    }

    private String improveCode(String sourceCode, String errors) {
        String prompt = createPrompt(sourceCode, errors);
        return openAiService.sendMessageToOpenAI(prompt);
    }

    private String createPrompt(String sourceCode, String errors) {
        return String.format("""
            You will receive a code snippet and an error message.

            Your task is to:
            1. Identify and resolve the error in the code snippet.
            2. Return only the corrected code in the same style and formatting as the input, with no additional explanation or details.
            
            ### Additional Instructions:
            - Maintain the original formatting and indentation of the input code.
            
            Code:
            %s
            
            Compilation/Runtime Errors:
            %s
            
            Please provide the corrected code:
            """, sourceCode, errors);
    }
}
