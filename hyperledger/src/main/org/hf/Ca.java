package main.org.hf;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import lombok.Data;
import lombok.experimental.Accessors;

@DataType
@Data
@Accessors(chain = true)
public class Ca {

    @Property
    String name;

    @Property
    String age;

    @Property()
    String grade;

    @Property()
    String subject;

    @Property()
    String university;

    @Property()
    String hashAlgorithm;

    @Property()
    String issure;

    @Property()
    String signature;

    @Property()
    String signatureAlgorithm;
}

