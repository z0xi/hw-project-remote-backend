package main.org.hf;

import lombok.Data;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;

@DataType
@Data
public class VerifyQueryResultList {
    @Property
    List<VerifyQueryResult> list;
}
