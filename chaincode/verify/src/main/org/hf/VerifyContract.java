package main.org.hf;

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

    public static int count = 10000;

    @Transaction
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 10; i++ ) {
            VerifyServer verify = new VerifyServer()
                    .setUserid("user" + i)
                    .setServerid("201"+i)
                    .setServer("CS")
                    .setState("JNU")
                    .setProperties( "{age=12,name=tom}");
            stub.putStringState(Integer.toString(count++) , JSON.toJSONString(verify));
        }

    }

    @Transaction
    public VerifyServer createVerify(final Context ctx, String userid, String serverid, String server, String state, String properties) {
        String key = Integer.toString(count++);
        ChaincodeStub stub = ctx.getStub();
        String verifyState = stub.getStringState(key);

        if (StringUtils.isNotBlank(verifyState)) {
            String errorMessage = String.format("VerifyServer %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        VerifyServer verify = new VerifyServer()
                .setUserid(userid)
                .setServerid(serverid)
                .setServer(server)
                .setState(state)
                .setProperties(properties);
        String json = JSON.toJSONString(verify);
        stub.putStringState(key, json);

        stub.setEvent("createVerifyServerEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return verify;
    }

    @Transaction
    public VerifyServer queryVerify(final Context ctx, final String id) {

        ChaincodeStub stub = ctx.getStub();
        String verifyState = stub.getStringState(id);

        if (StringUtils.isBlank(verifyState)) {
            String errorMessage = String.format("VerifyServer %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        return JSON.parseObject(verifyState, VerifyServer.class);
    }

    @Transaction
    public VerifyServer deleteVerify(final Context ctx, final String id) {

        ChaincodeStub stub = ctx.getStub();
        String verifyState = stub.getStringState(id);

        if (StringUtils.isBlank(verifyState)) {
            String errorMessage = String.format("VerifyServer %s does not exist", id);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.delState(id);
        return JSON.parseObject(verifyState , VerifyServer.class);
    }

    @Transaction
    public VerifyServer updateVerify(final Context ctx, final String key , String state) {

        ChaincodeStub stub = ctx.getStub();
        String verifyState = stub.getStringState(key);

        if (StringUtils.isBlank(verifyState)) {
            String errorMessage = String.format("VerifyServer %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        VerifyServer verify=JSON.parseObject(verifyState , VerifyServer.class);
        verify.setState(state);
        stub.putStringState(key, JSON.toJSONString(verify));

        return verify;
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
                Result.setVerify(JSON.parseObject(kv.getStringValue() , VerifyServer.class));
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
