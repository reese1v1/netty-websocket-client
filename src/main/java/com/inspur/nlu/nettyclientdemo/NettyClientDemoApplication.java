package com.inspur.nlu.nettyclientdemo;

import com.inspur.nlu.nettyclientdemo.client.WebsocketClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@SpringBootApplication
@Slf4j
public class NettyClientDemoApplication {

    private static final String BASE_URL = "ws://wsapi.xfyun.cn/v1/aiui";
    private static final String APIKEY = "2abe16fb4ce6a50927670473d2ac8052";
    private static final String APPID = "a2fbf158";
    private static final String param_new = "{\"auth_id\": \"9ebebca9697959bc0269be1d6bc4f51a\", \"result_level\": \"complete\",\"vad_info\":\"end\", \"data_type\": \"audio\", \"aue\": \"raw\", \"scene\": \"main_box\", \"sample_rate\": \"16000\", \"ver_type\": \"monitor\", \"close_delay\": \"200\", \"context\": \"{\\\"sdk_support\\\":[\\\"iat\\\",\\\"nlp\\\",\\\"tts\\\"]}\"}";

    public static void main(String[] args) {
        SpringApplication.run(NettyClientDemoApplication.class, args);

        try {
            URI uri = new URI(BASE_URL + getHandShakeParams());
            WebsocketClient websocketClient = new WebsocketClient(uri, 60);
            websocketClient.connect();

            // 获取结果
            String result = websocketClient.receiveResult();
            log.info("接收到结果[{}]", result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getHandShakeParams() throws UnsupportedEncodingException {
        String paramBase64 = new String(Base64.encodeBase64(param_new.getBytes("UTF-8")));
        String curtime = System.currentTimeMillis() / 1000L + "";
        String signtype = "sha256";
        String originStr = APIKEY + curtime + paramBase64;
        String checksum = getSHA256Str(originStr);
        String handshakeParam = "?appid=" + APPID + "&checksum=" + checksum + "&curtime=" + curtime + "&param=" + paramBase64 + "&signtype=" + signtype;
        return handshakeParam;
    }

    private static String getSHA256Str(String str) {
        MessageDigest messageDigest;
        String encdeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
            encdeStr = Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encdeStr;
    }
}
