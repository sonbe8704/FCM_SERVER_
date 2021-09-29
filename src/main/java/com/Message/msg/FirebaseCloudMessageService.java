package com.Message.msg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.TopicManagementResponse;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirebaseCloudMessageService {
    private  final String API_URL = "https://fcm.googleapis.com/v1/projects/login-6ba8f/messages:send";
    private ObjectMapper objectMapper;


    private String getAccessToken() throws IOException{
        String firebaseConfigPath = "src/firebase/firebase_service_key.json";

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();


    }

    public void sendToTopic(String forum,String topic,String title, List<String> subs) throws FirebaseMessagingException {

        Integer size = subs.size();
        System.out.println(size);
        System.out.println(subs);

        if(subs.size()!=0) {
            TopicManagementResponse subsresponse = FirebaseMessaging.getInstance().subscribeToTopic(subs, topic);
            System.out.println(subsresponse.getSuccessCount()+" tokens were subscribe successfully\n");
            System.out.println(subsresponse.getErrors());

            Message message = Message.builder()
                    .putData("title", title)
                    .putData("body", "새 댓글이 달렸습니다.")
                    .putData("post_id",topic)
                    .putData("forum_sort",forum)
                    .setTopic(topic)
                    .build();

            // Send a message to the devices subscribed to the provided topic.
            String response = FirebaseMessaging.getInstance().send(message);
            // Response is a message ID string.
            System.out.println("Successfully sent message: " + response);
            // [END send_to_topic]
           subsresponse =  FirebaseMessaging.getInstance().unsubscribeFromTopic(subs, topic);
            System.out.println(subsresponse.getSuccessCount()+"tokens were unsubscribe successfully\n");

            System.out.println(subs);
        }
    }

}
