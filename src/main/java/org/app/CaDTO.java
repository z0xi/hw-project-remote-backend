package org.app;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class CaDTO {
    String key;

    String name;

    String age;

    String grade;

    String subject;

    String university;

    String hashAlgorithm;

    String issure;

    String signature;

    String signatureAlgorithm;
}
