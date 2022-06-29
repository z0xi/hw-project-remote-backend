package main.org.hf;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import lombok.Data;
import lombok.experimental.Accessors;


@DataType
@Data
@Accessors(chain = true)
public class CaQueryResult {

    @Property
    String key;

    @Property
    Ca ca;
}

