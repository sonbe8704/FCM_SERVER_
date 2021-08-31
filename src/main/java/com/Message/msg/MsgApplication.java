package com.Message.msg;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.EventListener;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class MsgApplication {

	private static Firestore db;
	public static void main(String[] args) throws FileNotFoundException, ExecutionException, InterruptedException, FirebaseMessagingException {

		SpringApplication.run(MsgApplication.class, args);
		FileInputStream serviceAccount = new FileInputStream("src/firebase_service_key.json");

		FirebaseOptions options = null;
		try {
			options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://login-6ba8f-default-rtdb.firebaseio.com")
					.build();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FirebaseApp.initializeApp(options);
		FirebaseCloudMessageService firebaseCloudMessageService = new FirebaseCloudMessageService();
		db = FirestoreClient.getFirestore();


		CollectionReference mlist = db.collection("message");
		mlist.addSnapshotListener(new EventListener<QuerySnapshot>() {
			@Override
			public void onEvent(@Nullable QuerySnapshot value, @Nullable FirestoreException error) {
				System.out.println("detect");
				List<QueryDocumentSnapshot> qmsgs = value.getDocuments();
				System.out.println(qmsgs.size());

				ArrayList<Msg> msgs = new ArrayList<>();
				System.out.println(qmsgs);

				for(QueryDocumentSnapshot qmsg : qmsgs){
					System.out.println("...");
					Map data =new HashMap();
					data =qmsg.getData();
					System.out.println(data);
					Msg temp = new Msg();
					temp.setForum(data.get("forum").toString());
					temp.setPost_id(data.get("post_id").toString());
					temp.setTimestamp((Timestamp)data.get("timestamp"));
					temp.setWho(data.get("who").toString());
					msgs.add(temp);
				}
				System.out.println("sort");

				msgs.sort(Msg::compartor);

				System.out.println(msgs);
				int size=  msgs.size();
				if(size!=0) {
					Msg msg = msgs.get(size-1);


					String who = msg.getWho();
					String post_id = msg.getPost_id();
					String forum_sort = msg.getForum();

					ArrayList<String> subs = null;
					try {
						subs = (ArrayList<String>) db.collection(forum_sort).document(post_id).get().get().get("subscriber");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					subs.remove(who);
					System.out.println("delete");

					String title = null;
					try {
						title = (String) db.collection(forum_sort).document(post_id).get().get().get("title");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}


					try {
						System.out.println("send");

						firebaseCloudMessageService.sendToTopic(forum_sort,post_id, title, subs);
					} catch (FirebaseMessagingException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}


}

