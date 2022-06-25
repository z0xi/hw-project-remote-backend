package main.org.hf;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class VerifyServer {
    @Property
    String userid;

    @Property
    String serverid;

    @Property()
    String sever;

    @Property()
    String state;

    @Property()
    String properties;

}
