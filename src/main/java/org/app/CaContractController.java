package org.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.HashMap;
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

        byte[] bytes = contract.submitTransaction("createCa", ca.getName(), ca.getAge(), ca.getGrade(), ca.getSubject(), ca.getUniversity(), ca.getIssue());

        result.put("payload", StringUtils.newStringUtf8(bytes));
        result.put("status", "ok");
        return result;
    }

    @PutMapping("/async")
    public Map<String, Object> createCaAsync(@RequestBody CaDTO ca) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();

        contract.newProposal("createCa")
                .addArguments(ca.getName(), ca.getAge(), ca.getGrade(), ca.getSubject(), ca.getUniversity(), ca.getIssue())
                .build()
                .endorse()
                .submitAsync();

        result.put("status", "ok");

        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> updateCa(@RequestBody CaDTO ca) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] bytes = contract.submitTransaction("updateCa", ca.getId(), ca.getName(), ca.getAge(), ca.getGrade(), ca.getSubject(), ca.getUniversity(), ca.getIssue());

        result.put("payload", StringUtils.newStringUtf8(bytes));
        result.put("status", "ok");

        return result;
    }

    @DeleteMapping("/deleteCert/{key}")
    public Map<String, Object> deleteCaByKey(@PathVariable String key) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();

//        byte[] ca = contract.submitTransaction("deleteCa" , key);

//        result.put("payload", StringUtils.newStringUtf8(ca));
        result.put("status", "ok");

        return result;
    }

    @GetMapping("/queryAll")
    @ResponseBody
    public Map<Object,Object> queryAll() throws GatewayException {

        Map<Object, Object> result = Maps.newConcurrentMap();
        byte[] ca = contract.evaluateTransaction("queryCaAll");
        String str = StringUtils.newStringUtf8(ca);
        JSONObject obj = JSON.parseObject(str);
        result.put("payload", obj);
        result.put("status", "ok");

        return result;
    }

    @GetMapping("/queryServiceList")
    @ResponseBody
    public Map<Object,Object> queryServiceList() throws GatewayException {

        Map<Object, Object> result = Maps.newConcurrentMap();
        //TODO 获取链上服务列表 服务ID 服务名字 服务所需属性组合
        Network network = gateway.getNetwork("mychannel");
        Contract verifyContract = network.getContract("verify");
        byte[] ca = verifyContract.evaluateTransaction("queryVerifyAll");
        String str = StringUtils.newStringUtf8(ca);
        JSONObject obj = JSON.parseObject(str);

        result.put("payload", obj);
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

    @GetMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadByName() throws IOException {
       Map<String, Object> result = Maps.newConcurrentMap();
        // 创建服务端socket
        ServerSocket serverSocket = new ServerSocket(8899);

        // 创建客户端socket
        Socket socket = new Socket();
        System.out.print("Oracle connected\n");
        // 监听客户端
        socket = serverSocket.accept();
        BufferedReader br=null;
        try {
            //读取服务器返回的消息
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String mess = br.readLine();
            System.out.println(mess);
            if(mess.equals("success")){
                socket.shutdownInput();
                System.out.println("Uploading");
                //TODO 这里必须弄成任意字段，别写死
                String age = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/age");
                String name = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/name");
                String grade = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/grade");
                String subject = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/subject");
                String university = encodeFile("/home/kali/Desktop/hw-project/oracle/server_folder/university");
                String issuer = "CA";
                byte[] bytes = contract.submitTransaction("createCa", name, age, grade, subject, university, issuer);
                result.put("payload", StringUtils.newStringUtf8(bytes));
                System.out.print("Upload finish\n");
            }
            else{
                System.out.print("Upload fail\n");
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

    @PostMapping("/verify")
    @ResponseBody
    public Map<String, Object> verifyProperties(@RequestParam("key") String key, @RequestParam("certid") String id, @RequestParam("paramset") String[] properties) throws GatewayException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();
        //此字符数组应为参数传入属性名集合，这里提前定义作为测试使用
//        String properties[] = new String[]{"name", "age", "grade"};//attrs
        System.out.print("12345");
        ServerSocket serverSocket = new ServerSocket(8888);
        // 创建客户端socket
        Socket socket = new Socket();
        // 监听客户端
        socket = serverSocket.accept();
        System.out.print("User connected\n");
        BufferedReader br=null;
        PrintWriter pw = null;
        System.out.print("Fetch on-chain attributes\n");
        //TODO 获取链上属性并保存为文件 enc_credential_v.json 存放到文件夹
        byte[] ca = contract.evaluateTransaction("queryCa", id);
        String[] str = StringUtils.newStringUtf8(ca).split("\"");

        Map<String, String> map = new HashMap<String, String>();
        for (int i = 3; i < str.length; i+=4) {
            map.put(str[i-2], str[i]);
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("/home/kali/Desktop/hw-project/verifier/verifier_folder/enc_credential_v.json"),"UTF-8");
        JSONObject obj = new JSONObject();
        for (String s: properties) {
            System.out.println("key="+s+","+"value="+map.get(s));//输出方便查看
            obj.put(s, map.get(s));
        }
        osw.write(obj.toString());
        osw.flush();
        osw.close();

        try {
            //发送消息给verifier说明enc_credential_v.json已经准备就绪
            pw = new PrintWriter(socket.getOutputStream());
            pw.write("FileReady");
            pw.flush();
            System.out.print("Verifying…\n");
           
            //读取服务器返回的消息
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String mess = br.readLine();
            System.out.println(mess);
            byte[] bytes = null;
            if(mess.equals("success")){
                System.out.print("Success\n");
                //TODO chaincode上链
                Network network = gateway.getNetwork("mychannel");
                Contract verifyContract = network.getContract("verify");
                bytes = verifyContract.submitTransaction("updateVerify", key,  id, "sever1", "help", "authorized", "{name=123}");
            }
            result.put("payload", StringUtils.newStringUtf8(bytes));
            result.put("status", "ok");
        } catch (GatewayException e) {
            String errorMessage = String.format("Ca: %s does not exist", id);
            System.out.println(errorMessage);
            throw new RuntimeException(errorMessage);
        }catch (CommitException e) {
            String errorMessage = String.format("Verify: %s already exists", id);
            System.out.println(errorMessage);
            throw new RuntimeException(errorMessage);
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            //关闭资源
            try {
                if(br!=null)
                    br.close();
                if(pw!=null)
                    pw.close();
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
