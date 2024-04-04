package data;

import java.util.List;


    public interface ChatMessage2 {

        int id = 0;

        public long insertMessage(ChatMessage m);
        public List<ChatMessage> getAllMessages();
        void deleteMessage(ChatMessage m);

    }



