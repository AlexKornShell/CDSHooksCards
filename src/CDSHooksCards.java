import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
    }

    static class QuestionnaireResponse {
        String resourceType;
        String id;
        Date authored;
        String status;
        Object[] contained;
        AnswerItem[] item;

        public QuestionnaireResponse(String title, AnswerItem[] item) {
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

    static class ChlamydiaPatient {
        String id;
        String gender;
        int ageInYears;
        // Has to be changed from Strings and int to classes and Maps!!!
        int pregnancy = -1; // Test field
        int sexuallyActive;
        String condition;
        String observation;
        String diagnosticOrder;
        String procedure;
        String medicationPrescription;
        String medicationTreatment;

        public ChlamydiaPatient(Patient patient) {
            this.id = patient.id;
            this.gender = patient.gender;
            this.ageInYears = patient.age();
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


