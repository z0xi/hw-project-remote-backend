package main.org.hf;

import lombok.Data;
import java.util.List;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;


@DataType
@Data
public class CaQueryResultList {
    @Property
    List<CaQueryResult> resultList;
}
