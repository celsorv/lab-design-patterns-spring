package br.softhouse.gof.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(Include.NON_NULL)
@Getter @Builder
public class Occurrence {

    private String title;
    private String type;
    private Integer status;
    private String detail;
    private String userMessage;
    private List<Description> descriptions;
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @Getter @Builder
    public static class Description {
        private String name;
        private String userMessage;
    }

}
