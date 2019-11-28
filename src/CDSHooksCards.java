import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

public class CDSHooksCards {

    private static String hook = "chlamydia-screening";
    private static String hookInstance = "1234567890";
    private static String fhirServer = "http://our_url.com";
    private static String user = "Screener";

    private static String theirHook = "patient-info";
    private static String theirHookInstance = "1234567890";
    private static String theirFhirServer = "http://their_url.com";

    private static int questionnaireId = 0;
    private static int linkId = 0;

    private static String stringQuestionnaireId() {
        return String.format("qId%s", String.valueOf(questionnaireId++));
    }

    private static String stringLinkId() {
        return String.format("lId%s", String.valueOf(linkId++));
    }

    static class Request {
        String hook;
        String hookInstance;
        String fhirServer;
        String user;
        Prefetch prefetch;

        public Request(Prefetch prefetch) {
            this.hook = CDSHooksCards.theirHook;
            this.hookInstance = CDSHooksCards.theirHookInstance;
            this.fhirServer = CDSHooksCards.theirFhirServer;
            this.user = CDSHooksCards.user;
            this.prefetch = prefetch;
        }
    }

    static class Prefetch {
        Patient patient;
        Observation observation;
        DiagnosticReport diagnosticReport;
        Questionnaire questionnaire;
        QuestionnaireResponse questionnaireresponse;

        public Prefetch(Questionnaire questionnaire) {
            this.questionnaire = questionnaire;
        }

        public Prefetch(Patient patient, Questionnaire questionnaire) {
            this.patient = patient;
            this.questionnaire = questionnaire;
        }
    }

    static class Patient {
        String resourceType;
        String gender;
        String birthDate;
        String id;
        boolean active;

        public Patient(String gender, String birthDate, String id) {
            this.resourceType = "Patient";
            this.gender = gender;
            this.birthDate = birthDate;
            this.id = id;
            this.active = true;
        }

        public int age() {
            return Period.between(LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalDate.now()).getYears();
        }
    }

    static class Questionnaire {
        String resourceType;
        String id;
        String title;
        String status;
        Object[] contained;
        Item[] item;

        public Questionnaire(String title, Item[] item) {
            this.title = title;
            this.item = item;
            this.resourceType = "Questionnaire";
            this.id = stringQuestionnaireId();
            this.status = "draft";
        }
    }

    static class Item {
        String linkId;
        String text;
        String type;

        public Item(String text, String type) {
            this.linkId = stringLinkId();
            this.text = text;
            this.type = type;
        }
    }

    static class AnswerItem {
        String linkId;
        String text;
        Answer[] answer;

        public AnswerItem(String question, Answer answer) {
            this.linkId = stringLinkId();
            this.text = question;
            this.answer = new Answer[]{answer};
        }
    }

    static class Answer {
        Boolean valueBoolean;
        String valueString;
        int valueInteger;
    }

    static class QuestionnaireResponse {
        String resourceType;
        String id;
        Date authored;
        String status;
        Object[] contained;
        AnswerItem[] item;

        public QuestionnaireResponse(AnswerItem[] item) {
            this.item = item;
            this.resourceType = "QuestionnaireResponse";
            this.id = stringQuestionnaireId();
            this.status = "completed";
        }
    }

    static class Card {
        String summary;
        String indicator;
        Object source;
        String detail;

        public Card(String summary, String detail) {
            this.summary = summary;
            this.detail = detail;
            this.indicator = "info";
            this.source = "ChlamydiaScreeningCDS.cql";
        }
    }

    static class Cards {
        Card[] cards;

        public Cards(Card[] cards) {
            this.cards = cards;
        }
    }

    // Пока что везде title - собственно, описание, что это за состояние, наблюдение и т.д..
    static class Condition {
        String title;
        String effectiveTime; // Дата постановки состояния
    }

    static class Observation {
        String resourceType;
        String status;
        String code;
        String observedAtTime; // Дата наблюдения
    }

    static class MedicationTreatment {
        String title;
        String performanceTime; // Начало лечения
    }

    static class MedicationPrescription {
        String title;
        String reason; // Причина назначения
        String orderedAtTime; // Начало назначения
    }

    static class DiagnosticOrder {
        String title;
        String orderedAtTime; // Дата назначения
    }

    static class DiagnosticReport {
        String resourceType;
        String status;
        String code;
        String observedAtTime; // Дата проведения
        String value; // Результат
    }

    static class ChlamydiaPatient {
        String id;
        String gender;
        int ageInYears = -1;
        // Has to be changed from Strings and int to classes and Maps!!!
        // int pregnancy = -1; // Test field
        int sexuallyActive = -1;
        HashMap<String, Condition> conditions;
        HashMap<String, Observation> observations = new HashMap<>();
        HashMap<String, MedicationTreatment> medicationTreatments;
        HashMap<String, MedicationPrescription> medicationPrescriptions;
        HashMap<String, DiagnosticOrder> diagnosticOrders;
        HashMap<String, DiagnosticReport> diagnosticReports = new HashMap<>();
        String procedure;

        public ChlamydiaPatient(Prefetch prefetch) {
            this.id = prefetch.patient.id;
            this.gender = prefetch.patient.gender;
            this.ageInYears = prefetch.patient.age();
            if (prefetch.observation != null) {
                this.observations.put(prefetch.observation.code, prefetch.observation);
            }
            if (prefetch.diagnosticReport != null) {
                this.diagnosticReports.put(prefetch.diagnosticReport.code, prefetch.diagnosticReport);
            }
        }
    }

    private static Questionnaire simpleQuestionnaire(String questionnaireTitle, String questionText, String questionType) {
        return new Questionnaire(questionnaireTitle, new Item[]{new Item(questionText, questionType)});
    }

    public static Request questionRequest(Patient patient, String questionnaireTitle, String questionText, String questionType) {
        return new Request(new Prefetch(patient, simpleQuestionnaire(questionnaireTitle, questionText, questionType)));
    }

    public static Cards cardsResponse(String summary, String detail) {
        return new Cards(new Card[]{new Card(summary, detail)});
    }

}


