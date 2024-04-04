package data;

import androidx.room.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public abstract class ChatMessage2 {
    @Insert
    public abstract long insertMessage(ChatMessage m);

    @Query("SELECT * FROM ChatMessage")
    public abstract List<ChatMessage> getAllMessages();

    @Delete
    public abstract void deleteMessage(ChatMessage m);
}
