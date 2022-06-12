package org.app;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/myca")
@Slf4j
@AllArgsConstructor
public class CaContractController {

    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;


    @GetMapping("/{key}")
    public Map<String, Object> queryCaByKey(@PathVariable String key) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] ca = contract.evaluateTransaction("queryCa", key);

        result.put("payload", StringUtils.newStringUtf8(ca));
        result.put("status", "ok");

        return result;
    }

    @PutMapping("/add")
    public Map<String, Object> createCa(@RequestBody CaDTO ca) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();

        byte[] bytes = contract.submitTransaction("createCa", ca.getKey(), ca.getName(), ca.getAge(), ca.getGrade(), ca.getSubject(), ca.getUniversity(), ca.getHashAlgorithm(), ca.getIssure(), ca.getSignature(), ca.getSignatureAlgorithm());

        result.put("payload", StringUtils.newStringUtf8(bytes));
        result.put("status", "ok");
        return result;
    }

    @PutMapping("/async")
    public Map<String, Object> createCatAsync(@RequestBody CaDTO ca) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();

        contract.newProposal("createCa")
                .addArguments(ca.getKey(), ca.getKey(), ca.getName(), ca.getAge(), ca.getGrade(), ca.getSubject(), ca.getUniversity(), ca.getHashAlgorithm(), ca.getIssure(), ca.getSignature(), ca.getSignatureAlgorithm())
                .build()
                .endorse()
                .submitAsync();

        result.put("status", "ok");

        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> updateCa(@RequestBody CaDTO ca) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] bytes = contract.submitTransaction("updateCa", ca.getKey(), ca.getName(), ca.getAge(), ca.getGrade(), ca.getSubject(), ca.getUniversity(), ca.getHashAlgorithm(), ca.getIssure(), ca.getSignature(), ca.getSignatureAlgorithm());

        result.put("payload", StringUtils.newStringUtf8(bytes));
        result.put("status", "ok");

        return result;
    }

    @DeleteMapping("/{key}")
    public Map<String, Object> deleteCaByKey(@PathVariable String key) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();

        byte[] ca = contract.submitTransaction("deleteCa" , key);

        result.put("payload", StringUtils.newStringUtf8(ca));
        result.put("status", "ok");

        return result;
    }

}
