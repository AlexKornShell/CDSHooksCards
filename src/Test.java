import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Test {

    String atRiskByAgeMessage = "Patient is at risk for chlamydia infection by age group, no screening test available in past 1 year";
    String chlamydiaScreeningTestProposal = "Laboratory Test, Result: Chlamydia Screening";

    String notAtRiskByAgeMessage = "Patient is not at risk for chlamydia infection";
    String noChlamydiaScreeningTestProposal = "No test proposal";


    String genderQuestion = "What is the patient's gender?";
    String genderType = "string";

    String ageQuestion = "What is the patient's age?";
    String ageType = "integer";

    String sexualActiveQuestion = "Is the patient sexually active?";
    String sexualActiveType = "boolean";


    String chlamydiaTestData = "Laboratory Test, Result: Chlamydia Screening";

    String chlamydiaTestQuestion = "Did the patient take a Chlamydia Screening test?";
    String chlamydiaTestType = "boolean";

    String chlamydiaTestDateQuestion = "When did the patient take the Chlamydia Screening test?";
    String chlamydiaTestDateType = "string";

    String chlamydiaTestResultQuestion = "What was the result of patient's Chlamydia Screening test?";
    String chlamydiaTestResultType = "string";


    String sexuallyTransmittedInfectionData = "Risk Evaluation, Document: Sexually Transmitted Infection";

    String sexuallyTransmittedInfectionQuestion = "Was the patient observed for sexually transmitted infection";
    String sexuallyTransmittedInfectionType = "boolean";

    String sexuallyTransmittedInfectionDateQuestion = "When was the patient observed for sexually transmitted infection";
    String sexuallyTransmittedInfectionDateType = "string";


    static boolean checkPatient(CDSHooksCards.ChlamydiaPatient p) {
        return p.gender != null && p.ageInYears != -1 && p.sexuallyActive != -1 &&
                !p.observations.isEmpty() && !p.diagnosticReports.isEmpty();
    }

    static double yearsPassed(String date) {
        return Period.between(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalDate.now()).toTotalMonths() / 12f;
    }

    static boolean isFemale(CDSHooksCards.ChlamydiaPatient p) {
        return p.gender.equals("female");
    }

    static boolean atRiskByAgeGroup(CDSHooksCards.ChlamydiaPatient p) {
        return p.ageInYears >= 16 && p.ageInYears <= 24;
    }

    static boolean hasEvidenceOfSexualActivity(CDSHooksCards.ChlamydiaPatient p) {
        return p.sexuallyActive == 1;
    }

    static boolean hasChlamydiaTest(CDSHooksCards.ChlamydiaPatient p) {
        return p.diagnosticReports.get("Laboratory Test, Result: Chlamydia Screening") != null &&
                yearsPassed(p.diagnosticReports.get("Laboratory Test, Result: Chlamydia Screening").observedAtTime) <= 1 &&
                p.diagnosticReports.get("Laboratory Test, Result: Chlamydia Screening").value != null;
    }

    static boolean hasSTIriskFactor(CDSHooksCards.ChlamydiaPatient p) {
        return // p.conditions.get("Risk Evaluation, Document: Sexually Transmitted Infection") != null &&
                // yearsPassed(p.conditions.get("Risk Evaluation, Document: Sexually Transmitted Infection").effectiveTime) <= 1 ||
                p.observations.get("Risk Evaluation, Document: Sexually Transmitted Infection") != null &&
                yearsPassed(p.observations.get("Risk Evaluation, Document: Sexually Transmitted Infection").observedAtTime) <= 1;
    }

    static boolean inAtRiskAgePopulation (CDSHooksCards.ChlamydiaPatient p) {
        return isFemale(p) && atRiskByAgeGroup(p) && hasEvidenceOfSexualActivity(p) && !hasChlamydiaTest(p);
    }

    static boolean inOtherAtRiskPopulation (CDSHooksCards.ChlamydiaPatient p) {
        return hasSTIriskFactor(p) && !hasChlamydiaTest(p);
    }

    static boolean inAgeAndOtherAtRiskPopulation  (CDSHooksCards.ChlamydiaPatient p) {
        return inAtRiskAgePopulation(p) && inOtherAtRiskPopulation(p) && !hasChlamydiaTest(p);
    }


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
            CDSHooksCards.ChlamydiaPatient patient = new CDSHooksCards.ChlamydiaPatient(request.prefetch);
            patients.put(patient.id, patient);
            System.out.println(patients.get("1288992"));
//            patient.sexuallyActive = 1;
//            System.out.println(checkPatient(patient));
//            System.out.println(inAtRiskAgePopulation(patient));
//            System.out.println(inOtherAtRiskPopulation(patient));
//            System.out.println(!hasChlamydiaTest(patient));

            // Check condition, based on cql tree
            // This condition is wrong. There is no pregnancy field in real CQL, there is diagnosticOrder.
            if (patient.sexuallyActive == -1) {
                // Create question request
                CDSHooksCards.Request questionRequest = CDSHooksCards.
                        questionRequest(request.prefetch.patient, "Sexually Active", "Is the patient sexually active?", "boolean");
                // Send it (Change file writer to HTTP response)
                gson.toJson(questionRequest, questionWriter);

                // Wait

                // When new request appears, it is an answer
                CDSHooksCards.Request answerRequest = gson.fromJson(responseReader, CDSHooksCards.Request.class);
                // Fill the field
                patient.sexuallyActive = answerRequest.prefetch.questionnaireresponse.item[0].answer[0].valueBoolean ? 1 : 0;
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
