package org.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.hyperledger.fabric.client.ChaincodeEvent;
import org.hyperledger.fabric.client.CloseableIterator;
import org.hyperledger.fabric.client.Network;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;


@Slf4j
public class ChaincodeEventListener implements Runnable {

    final Network network;

    public ChaincodeEventListener(Network network) {
        this.network = network;

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName(this.getClass() + "chaincode_event_listener");
                return thread;
            }
        });

        executor.execute(this);
    }

    @Override
    public void run() {
        CloseableIterator<ChaincodeEvent> events = network.getChaincodeEvents("myca");
        log.info("chaincodeEvents {} " , events);


        while (events.hasNext()) {
            ChaincodeEvent event = events.next();

            log.info("receive chaincode event {} , block number {} , payload {} "
                    , event.getEventName() , event.getBlockNumber() , JSONArray.toJSONString(Base64.decodeBase64(event.getPayload())));
        }
    }
}
