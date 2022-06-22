package org.app;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

@RestController
@RequestMapping("/myca")
@Slf4j
@AllArgsConstructor
@CrossOrigin
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
    public Map<String, Object> createCaAsync(@RequestBody CaDTO ca) throws Exception {
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

    @GetMapping("/test")
    public Map<String, Object> test() throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();
        System.out.print("TEST");
        result.put("status", "ok");

        return result;
    }

    @PutMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadByName(@RequestBody String id) throws IOException {
       Map<String, Object> result = Maps.newConcurrentMap();
        // 创建服务端socket
        ServerSocket serverSocket = new ServerSocket(8899);

        // 创建客户端socket
        Socket socket = new Socket();
        System.out.print("Oracle connected");
        // 监听客户端
        socket = serverSocket.accept();
        InputStream is=null;
        InputStreamReader isr=null;
        BufferedReader br=null;
        try {
            //读取服务器返回的消息
            is = socket.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String mess = br.readLine();
            System.out.println(mess);
            if(mess.equals("success")){
                socket.shutdownInput();
                //TODO 这里必须弄成任意字段，别写死
                String age = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/age");
                String name = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/name");
                String grade = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/grade");
                String subject = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/subject");
                String university = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/university");
                String hashAlgorithm = "SHA256";
                String issuer = "CA";
                String signature = "NO";
                String signatureAlgorithm = "RSA";
                //TODO 这里会有bug，key不应该在这里赋值，应该在链码里赋值，因为这个sping一挂count就得重新开始然后触发错误
                byte[] bytes = contract.submitTransaction("createCa", id, name,  age, grade, subject, university, hashAlgorithm, issuer, signature, signatureAlgorithm);
                result.put("payload", StringUtils.newStringUtf8(bytes));
                System.out.print("Upload finish");
            }
            else{
                System.out.print("Upload fail");
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
        } catch (CommitException e) {
            throw new RuntimeException(e);
        } catch (CommitStatusException e) {
            throw new RuntimeException(e);
        } finally{
            //关闭资源
            try {
                if(br!=null)
                    br.close();
                if(isr!=null)
                    isr.close();
                if(is!=null)
                    is.close();
                if(socket!=null){
                    socket.close();
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @GetMapping("/verify")
    public Map<String, Object> verifyProperties() throws GatewayException, IOException {
       Map<String, Object> result = Maps.newConcurrentMap();
        System.out.print("debug");
        Socket s = new Socket("127.0.0.1",8899);
        InputStream is = s.getInputStream();
        OutputStream os = s.getOutputStream();
        PrintWriter pw=null;
        System.out.print("debug");
        //TODO 获取链上属性并保存为文件enc_credential_v.json
//            for (String property:properties) {
//                byte[] readFileToByteArray = FileUtils.readFileToByteArray(new File("./server_folder/" + property));
//                String codes = readFileToByteArray.toString();
//                decodeFile(codes, "./verifier_folder/" + property);
//            }
        try {

            pw = new PrintWriter(os);
            pw.write(1);
            pw.flush();
            System.out.print("File is not ready");


            //读取服务器返回的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String mess = br.readLine();
            if(mess.equals("success")){
                System.out.print("Success");
                //TODO chaincode上链
            }
            result.put("status", "ok");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
