package com.changgou.business.listener;

import okhttp3.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdListener {

    @RabbitListener(queues = "ad_update_queue")
    public void reciveMessage(String message) {
        System.out.println("收到消息为：" + message);

        OkHttpClient client = new OkHttpClient();

        String url = "http://akokko.com:25565/ad_update?position=" + message;
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("请求成功！" + response.message());
            }
        });
    }
}
