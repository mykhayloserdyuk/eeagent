package services;

import models.ExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CodeExecutor {

    public ExecutionResult execute(String sourceCode, ProgressIndicator indicator, boolean isKotlin)
            throws IOException, ExecutionException {
        File tempDir = createTempDirectory(indicator);

        try {
            File sourceFile = createSourceFile(tempDir, sourceCode, indicator, isKotlin);

            if (!compileCode(sourceFile, tempDir, indicator, isKotlin)) {
                String error = "Compilation failed with no specific error message.";
                return new ExecutionResult(false, "", error);
            }

            return runCode(tempDir, indicator, isKotlin);
        } finally {
            FileUtil.delete(tempDir);
        }
    }

    private boolean compileCode(File sourceFile, File tempDir, ProgressIndicator indicator, boolean isKotlin)
            throws ExecutionException {
        indicator.setFraction(0.4);

        AtomicReference<StringBuilder> compilationError = new AtomicReference<>(new StringBuilder());
        AtomicBoolean compilationComplete = new AtomicBoolean(false);

        GeneralCommandLine compileCmd = new GeneralCommandLine();
        compileCmd.setExePath(isKotlin ? "kotlinc" : "javac");
        compileCmd.addParameter(sourceFile.getAbsolutePath());
        compileCmd.setWorkDirectory(tempDir);

        if (isKotlin) {
            compileCmd.addParameter("-d");
            compileCmd.addParameter("output.jar");
        }

        OSProcessHandler compileHandler = new OSProcessHandler(compileCmd);
        compileHandler.addProcessListener(createCompileListener(compilationError, compilationComplete));
        compileHandler.startNotify();

        waitForCompletion(compilationComplete, indicator);

        if (indicator.isCanceled()) {
            return false;
        }

        int exitCode = compileHandler.getProcess().exitValue();
        return exitCode == 0;
    }

    private File createTempDirectory(ProgressIndicator indicator) throws IOException {
        indicator.setText("Creating temporary files...");
        indicator.setFraction(0.2);
        return FileUtil.createTempDirectory("codeexecution", "temp", true);
    }

    private File createSourceFile(File tempDir, String sourceCode, ProgressIndicator indicator, boolean isKotlin)
            throws IOException {
        String fileName = isKotlin ? "Main.kt" : "Main.java";
        File sourceFile = new File(tempDir, fileName);
        FileUtil.writeToFile(sourceFile, sourceCode);
        return sourceFile;
    }

    private ExecutionResult runCode(File tempDir, ProgressIndicator indicator, boolean isKotlin)
            throws ExecutionException {
        indicator.setText("Running code...");
        indicator.setFraction(0.6);

        StringBuilder errors = new StringBuilder();
        StringBuilder output = new StringBuilder();
        AtomicBoolean executionComplete = new AtomicBoolean(false);

        GeneralCommandLine runCmd = createRunCommand(tempDir, isKotlin);
        OSProcessHandler runHandler = new OSProcessHandler(runCmd);
        runHandler.addProcessListener(createRunListener(errors, output, executionComplete));
        runHandler.startNotify();

        waitForCompletion(executionComplete, indicator);

        if (indicator.isCanceled()) {
            runHandler.destroyProcess();
            return new ExecutionResult(false, "", "Operation cancelled by user");
        }

        int exitCode = runHandler.getProcess().exitValue();
        boolean success = exitCode == 0;
        String errorMsg = errors.toString().isEmpty() ? "Execution failed with no errors." : errors.toString();

        return new ExecutionResult(success, output.toString(), errorMsg);
    }

    private GeneralCommandLine createRunCommand(File tempDir, boolean isKotlin) {
        GeneralCommandLine runCmd = new GeneralCommandLine();
        if (isKotlin) {
            runCmd.setExePath("java");
            runCmd.addParameter("-jar");
            runCmd.addParameter("output.jar");
        } else {
            runCmd.setExePath("java");
            runCmd.addParameter("-cp");
            runCmd.addParameter(tempDir.getAbsolutePath());
            runCmd.addParameter("Main");
        }
        runCmd.setWorkDirectory(tempDir);
        return runCmd;
    }

    private ProcessAdapter createCompileListener(AtomicReference<StringBuilder> compilationError,
                                                 AtomicBoolean compilationComplete) {
        return new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                if (outputType == ProcessOutputTypes.STDERR) {
                    compilationError.get().append(event.getText());
                }
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                compilationComplete.set(true);
            }
        };
    }

    private ProcessAdapter createRunListener(StringBuilder errors, StringBuilder output, AtomicBoolean executionComplete) {
        return new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                if (outputType == ProcessOutputTypes.STDERR) {
                    errors.append(event.getText());
                } else if (outputType == ProcessOutputTypes.STDOUT) {
                    output.append(event.getText());
                }
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                executionComplete.set(true);
            }
        };
    }

    private void waitForCompletion(AtomicBoolean complete, ProgressIndicator indicator) {
        while (!complete.get() && !indicator.isCanceled()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
