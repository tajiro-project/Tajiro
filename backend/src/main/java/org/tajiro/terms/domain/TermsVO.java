package org.tajiro.terms.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TermsVO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String version;
    private Boolean required;
    private LocalDateTime effectiveAt;
}
