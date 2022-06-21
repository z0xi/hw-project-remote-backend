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
import org.hyperledger.fabric.shim.ledger.*;

import com.alibaba.fastjson.JSON;

import lombok.extern.java.Log;


@Contract(
        name = "CaContract",
        info = @Info(
                title = "Ca contract",
                description = "The hyperlegendary ca contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.ca@example.com",
                        name = "F Ca",
                        url = "https://hyperledger.example.com")))
@Default
@Log
public class CaContract implements ContractInterface {

    @Transaction
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 10; i++ ) {
            Ca ca = new Ca().setName("ca-" + i)
                    .setAge("2"+i)
                    .setGrade("201"+i)
                    .setSubject("CS")
                    .setUniversity("JNU")
                    .setHashAlgorithm("SHA246")
                    .setIssure("JNU")
                    .setSignature("ahbckjcdkn")
                    .setSignatureAlgorithm("RSA");
            stub.putStringState(ca.getName() , JSON.toJSONString(ca));
        }

    }

    @Transaction
    public Ca queryCa(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String caState = stub.getStringState(key);

        if (StringUtils.isBlank(caState)) {
            String errorMessage = String.format("Ca %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        return JSON.parseObject(caState , Ca.class);
    }


    @Transaction
    public Ca createCa(final Context ctx, final String key , String name , String age , String grade, String subject,
            String university, String hashAlgorithm, String issure, String signature, String signatureAlgorithm) {
        ChaincodeStub stub = ctx.getStub();
        String caState = stub.getStringState(key);

        if (StringUtils.isNotBlank(caState)) {
            String errorMessage = String.format("Ca %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Ca ca = new Ca().setName(name)
                .setAge(age)
                .setGrade(grade)
                .setSubject(subject)
                .setUniversity(university)
                .setHashAlgorithm(hashAlgorithm)
                .setIssure(issure)
                .setSignature(signature)
                .setSignatureAlgorithm(signatureAlgorithm);
        String json = JSON.toJSONString(ca);
        stub.putStringState(key, json);

        stub.setEvent("createCaEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return ca;
    }

    @Transaction
    public Ca updateCa(final Context ctx, final String key , String name , String age , String grade, String subject,
            String university, String hashAlgorithm, String issure, String signature, String signatureAlgorithm) {

        ChaincodeStub stub = ctx.getStub();
        String caState = stub.getStringState(key);

        if (StringUtils.isBlank(caState)) {
            String errorMessage = String.format("Ca %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Ca ca = new Ca().setName(name)
                .setAge(age)
                .setGrade(grade)
                .setSubject(subject)
                .setUniversity(university)
                .setHashAlgorithm(hashAlgorithm)
                .setIssure(issure)
                .setSignature(signature)
                .setSignatureAlgorithm(signatureAlgorithm);

        stub.putStringState(key, JSON.toJSONString(ca));

        return ca;
    }

    @Transaction
    public Ca deleteCa(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String caState = stub.getStringState(key);

        if (StringUtils.isBlank(caState)) {
            String errorMessage = String.format("Ca %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.delState(key);
        return JSON.parseObject(caState , Ca.class);
    }

    @Transaction
    public CaQueryResultList queryCaAll(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        final String startKey = "10001";
        final String endKey = "19999";
        CaQueryResultList resultList = new CaQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getStateByRange(startKey, endKey);
        List<CaQueryResult> results = Lists.newArrayList();
        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv: queryResult) {
                CaQueryResult Result = new CaQueryResult();
                Result.setKey(kv.getKey());
                Result.setCa(JSON.parseObject(kv.getStringValue() , Ca.class));
                results.add(Result);
            }
            resultList.setResultList(results);
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

