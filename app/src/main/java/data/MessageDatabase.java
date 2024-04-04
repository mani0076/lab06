package data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ChatMessage.class}, version=1)
public abstract class MessageDatabase extends RoomDatabase {
    public abstract ChatMessage2 cmDAO();

    public ChatMessage2 getMessage2() {
        return null;
    }
    };
