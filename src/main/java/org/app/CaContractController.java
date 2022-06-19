package org.app;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/myca")
@Slf4j
@AllArgsConstructor
public class CaContractController {

    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;
    public static int count = 1;


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
                .addArguments(ca.getKey(), ca.getName(), ca.getAge(), ca.getGrade(), ca.getSubject(), ca.getUniversity(), ca.getHashAlgorithm(), ca.getIssure(), ca.getSignature(), ca.getSignatureAlgorithm())
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

    @GetMapping("/queryAll")
    public Map<String, Object> queryAll() throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] ca = contract.evaluateTransaction("queryCaAll");

        result.put("payload", StringUtils.newStringUtf8(ca));
        result.put("status", "ok");

        return result;
    }

    @PutMapping("/upload")
    public Map<String, Object> uploadByName(@RequestBody String name) throws IOException {
       Map<String, Object> result = Maps.newConcurrentMap();

        try {
            Socket s = new Socket("127.0.0.1",8899);
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            //读取服务器返回的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String mess = br.readLine();
            if(mess.equals("success")){
                String key = "ca-" + count;
                String age = encodeFile("./server_folder/age");
                String grade = encodeFile("./server_folder/grade");
                String subject = encodeFile("./server_folder/subject");
                String university = encodeFile("./server_folder/university");
                String hashAlgorithm = encodeFile("./server_folder/hashAlgorithm");
                String issure = encodeFile("./server_folder/issure");
                String signature = encodeFile("./server_folder/signature");
                String signatureAlgorithm = encodeFile("./server_folder/signatureAlgorithm");
                contract.newProposal("createCa")
                        .addArguments(key, id, age, grade, subject, university, hashAlgorithm, issure, signature, signatureAlgorithm)
                        .build()
                        .endorse()
                        .submitAsync();
            }
            result.put("status", "ok");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EndorseException e) {
            throw new RuntimeException(e);
        } catch (SubmitException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @PutMapping("/verify")
    public Map<String, Object> verifyProperties(@RequestBody String[] properties) throws IOException {
        Map<String, Object> result = Maps.newConcurrentMap();
        for (String property:properties) {
            byte[] readFileToByteArray = FileUtils.readFileToByteArray(new File("./server_folder/" + property));
            String codes = readFileToByteArray.toString();
            decodeFile(codes, "./verifier_folder/" + property);
        }
        result.put("status", "ok");
        return result;
    }


    public static String encodeFile(File file) throws IOException {
        byte[] readFileToByteArray = FileUtils.readFileToByteArray(file);
        return Base64.encodeBase64String(readFileToByteArray);
    }

    public static String encodeFile(String filePath) throws IOException {
        return encodeFile(new File(filePath));
    }

    public static void decodeFile(String codes, File file) throws IOException {
        byte[] decodeBase64 = Base64.decodeBase64(codes);
        FileUtils.writeByteArrayToFile(file, decodeBase64);
    }

    public static void decodeFile(String codes, String filePath) throws IOException {
        decodeFile(codes, new File(filePath));
    }

}
