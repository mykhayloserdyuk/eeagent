package services;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import settings.PluginSettings;

public class OpenAIService {

    // Interface für OpenAI-Interaktionen
    interface OpenAiInteractionService {
        @SystemMessage("You are a Java programming expert. Fix the code errors in the provided code.\n" +
                "        Only respond with the corrected code, no explanations.\n" +
                "        Maintain the original structure and intent of the code while fixing the errors.")
        String sendMessage(@UserMessage String message);
    }

    private final OpenAiInteractionService openAiInteractionService;

    // Konstruktor zur Initialisierung des OpenAI-Services
    public OpenAIService(String apiKey) {
        String LLM = PluginSettings.getInstance().getLLM();
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(LLM) // Modellname, kann angepasst werden
                .timeout(java.time.Duration.ofSeconds(30)) // Timeout für API-Aufrufe
                .build();
        this.openAiInteractionService = AiServices.create(OpenAiInteractionService.class, chatModel);
    }

    /**
     * Sendet eine Nachricht an OpenAI und gibt die Antwort zurück.
     *
     * @param message Die Nachricht, die an OpenAI gesendet werden soll.
     * @return Die Antwort von OpenAI.
     */
    public String sendMessageToOpenAI(String message) {
        try {
            return openAiInteractionService.sendMessage(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with OpenAI: " + e.getMessage(), e);
        }
    }
}
