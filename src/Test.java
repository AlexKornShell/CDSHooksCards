import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, CDSHooksCards.ChlamydiaPatient> patients = new HashMap<>();

        // Using files to test
        try (FileReader reader = new FileReader("data/request.json");
             FileWriter questionWriter = new FileWriter("data/questionRequest.json");
             FileWriter responseWriter = new FileWriter("data/cardsResponse.json");
             FileReader responseReader = new FileReader("data/questionResponse.json")) {

            // Read the request
            CDSHooksCards.Request request = gson.fromJson(reader, CDSHooksCards.Request.class);
            // Extract patient
            CDSHooksCards.ChlamydiaPatient patient = new CDSHooksCards.ChlamydiaPatient(request.prefetch.patient);
            patients.put(patient.id, patient);

            // Check condition, based on cql tree
            // This condition is wrong. There is no pregnancy field in real CQL, there is diagnosticOrder.
            if (patient.pregnancy == -1) {
                // Create question request
                CDSHooksCards.Request questionRequest = CDSHooksCards.
                        questionRequest(request.prefetch.patient, "Pregnancy", "Is the patient pregnant?", "boolean");
                // Send it (Change file writer to HTTP response)
                gson.toJson(questionRequest, questionWriter);

                // Wait

                // When new request appears, it is an answer
                CDSHooksCards.Request answerRequest = gson.fromJson(responseReader, CDSHooksCards.Request.class);
                // Fill the field
                patient.pregnancy = answerRequest.prefetch.questionnaireresponse.item[0].answer[0].valueBoolean ? 1 : 0;
            }

            // Create cards response
            CDSHooksCards.Cards responseCard = CDSHooksCards.
                    cardsResponse("Screening required", "Due to the bla-bla the screening is required.");
            // Send it (Change file writer to HTTP response)
            gson.toJson(responseCard, responseWriter);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
