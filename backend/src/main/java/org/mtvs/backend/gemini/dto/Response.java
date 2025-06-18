package org.mtvs.backend.gemini.dto;

import lombok.Data;
import java.util.List;

@Data
public class Response {

    private List<Content> contents;

    @Data
    public class Content {
        private List<Parts> parts;
        private String role;

    }

    @Data
    public class Parts {
        private String text;
    }
}
