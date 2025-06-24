package org.mtvs.backend.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    /* Gemini API 요청 구조입니다.
      * {
        "contents": [
          {
            "parts": [
              {
                "text": "실제 프롬프트 텍스트"
              }
            ]
          }
        ]
      }
    * */


    private List<Content> contents;

    @Data
    public class Content{

        private List<Part> parts;

        public Content(String text){
            parts = new ArrayList<>();
            Part part = new Part(text);
            parts.add(part);
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public class Part{
            private String text;
        }
    }

    // 기존 prompt를 text 문자열로 제공하면 중첩 구조를 자동으로 만들어줌
    public void createGeminiReqDto(String text){
        this.contents = new ArrayList<>();
        Content content = new Content(text);
        contents.add(content);
    }
}
