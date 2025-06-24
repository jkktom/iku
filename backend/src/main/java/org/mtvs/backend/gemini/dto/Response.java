package org.mtvs.backend.gemini.dto;

import lombok.Data;
import java.util.List;

@Data
public class Response {

    private List<Candidate> candidates;

    @Data
    public static class Candidate {
        private Content content;
        private String finishReason;
    }

    @Data
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Data
    public static class Part {
        private String text;
    }
}