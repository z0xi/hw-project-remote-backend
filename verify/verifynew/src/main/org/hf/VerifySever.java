package main.org.hf;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.HashMap;

@DataType
@Data
@Accessors(chain = true)
public class VerifySever {
    @Property
    String userid;

    @Property
    String severid;

    @Property()
    String sever;

    @Property()
    String state;

    @Property()
    String properties;

}
