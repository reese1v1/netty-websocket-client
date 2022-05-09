package com.inspur.nlu.nettyclientdemo;

import com.inspur.nlu.nettyclientdemo.client.WebsocketClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


@SpringBootApplication
@Slf4j
public class NettyClientDemoApplication {

    private static final String BASE_URL = "ws://wsapi.xfyun.cn/v1/aiui";
    private static final String APIKEY = "";
    private static final String APPID = "a2fbf158";
    private static final String param_new = "";
    private static final String END_FLAG = "--end--";

    private static final String FILE_PATH = "C:\\workspace\\inspur\\netty-client-demo\\test.pcm";

    private static final int CHUNKED_SIZE = 1280;

    public static void main(String[] args) {
        SpringApplication.run(NettyClientDemoApplication.class, args);

        try {
            URI uri = new URI(BASE_URL + getHandShakeParams());
            WebsocketClient websocketClient = new WebsocketClient(uri, 60);
            websocketClient.connect();

            // 发送消息
            byte[] bytes = new byte[CHUNKED_SIZE];

            try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
                int len = -1;
                while ((len = raf.read(bytes)) != -1) {
                    if (len < CHUNKED_SIZE) {
                        bytes = Arrays.copyOfRange(bytes, 0, len);
                    }
                    ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(8, CHUNKED_SIZE);
                    if (byteBuf.isWritable()) {
                        websocketClient.write(byteBuf.writeBytes(bytes));
                    }
                    Thread.sleep(40);
                }
                Base64.decodeBase64();
                System.out.println("发送完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
            ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(8, CHUNKED_SIZE);
            if (byteBuf.isWritable()) {
                websocketClient.write(byteBuf.writeBytes(END_FLAG.getBytes(StandardCharsets.UTF_8)));
                System.out.println("发送结束标识完成");
            }
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
        return "?appid=" + APPID + "&checksum=" + checksum + "&curtime=" + curtime + "&param=" + paramBase64 + "&signtype=" + signtype;
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
