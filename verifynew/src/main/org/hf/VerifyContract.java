package main.org.hf;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.*;

import lombok.extern.java.Log;


@Contract(
        name = "VerifyContract",
        info = @Info(
                title = "Verify contract",
                description = "The hyperlegendary verify contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.verify@example.com",
                        name = "F Verify",
                        url = "https://hyperledger.example.com")))
@Default
@Log
public class VerifyContract implements ContractInterface {

    @Transaction
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 10; i++ ) {
            VerifySever verify = new VerifySever()
                    .setUserid("user" + i)
                    .setSeverid("201"+i)
                    .setSever("CS")
                    .setState("JNU")
                    .setProperties( "{age=12,name=tom}");
            String id = Integer.toString(10000 + i);
            stub.putStringState(id , JSON.toJSONString(verify));
        }

    }

    @Transaction
    public VerifySever createVerify(final Context ctx, final String id, String userid , String severid, String sever,
                                    String state, String properties) {
        ChaincodeStub stub = ctx.getStub();
        String caState = stub.getStringState(id);

        if (StringUtils.isNotBlank(caState)) {
            String errorMessage = String.format("VerifySever %s already exists", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        VerifySever verify = new VerifySever()
                .setUserid(userid)
                .setSeverid(severid)
                .setSever(sever)
                .setState(state)
                .setProperties(properties);
        String json = JSON.toJSONString(verify);
        stub.putStringState(id, json);

        stub.setEvent("createVerifySeverEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return verify;
    }
 


    @Transaction
    public VerifySever queryVerify(final Context ctx, final String id) {

        ChaincodeStub stub = ctx.getStub();
        String verifyState = stub.getStringState(id);

        if (StringUtils.isBlank(verifyState)) {
            String errorMessage = String.format("VerifySever %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        return JSON.parseObject(verifyState, VerifySever.class);
    }

    @Transaction
    public VerifySever deleteVerify(final Context ctx, final String id) {

        ChaincodeStub stub = ctx.getStub();
        String verifyState = stub.getStringState(id);

        if (StringUtils.isBlank(verifyState)) {
            String errorMessage = String.format("VerifySever %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.delState(id);
        return JSON.parseObject(verifyState , VerifySever.class);
    }

    @Transaction
    public VerifyQueryResultList queryVerifyAll(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        final String startKey = "10000";
        final String endKey = "99999";
        VerifyQueryResultList resultList = new VerifyQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getStateByRange(startKey, endKey);
        List<VerifyQueryResult> results = Lists.newArrayList();
        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv: queryResult) {
                VerifyQueryResult Result = new VerifyQueryResult();
                Result.setKey(kv.getKey());
                Result.setVerify(JSON.parseObject(kv.getStringValue() , VerifySever.class));
                results.add(Result);
            }
            resultList.setList(results);
        }
        return resultList;
    }

    @Override
    public void beforeTransaction(Context ctx) {
        log.info("*************************************** beforeTransaction ***************************************");
    }

    @Override
    public void afterTransaction(Context ctx, Object result) {
        log.info("*************************************** afterTransaction ***************************************");
        System.out.println("result --------> " + result);
    }

}
