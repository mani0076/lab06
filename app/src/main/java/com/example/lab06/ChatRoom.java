package com.example.lab06;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.lab06.databinding.ActivityChatRoomBinding;
import com.example.lab06.databinding.ReceivedMessageBinding;
import com.example.lab06.databinding.SentMessageBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import data.ChatMessage;
import data.ChatMessage2;
import data.ChatRoomViewModel;
import data.MessageDatabase;

public class ChatRoom extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    ArrayList<ChatMessage> messages = new ArrayList<>();
    ChatRoomViewModel chatModel ;
    private RecyclerView.Adapter myAdapter;

    ChatMessage2 mDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(),
                MessageDatabase.class, "database-name").build();
        mDAO = db.cmDAO();

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        messages = chatModel.messages.getValue();

        if(messages == null) {
            chatModel.messages.postValue( messages = new ArrayList<ChatMessage>());
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() ->
            {
                messages.addAll( mDAO.getAllMessages() ); //Once you get the data from database
                runOnUiThread( () -> binding.recyclerView.setAdapter( myAdapter )); //You can then load the RecyclerView
            });

        }

        binding.sendButton.setOnClickListener(clk->{
            String messageText = binding.textInput.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
            String currentDateandTime = sdf.format(new Date());
            ChatMessage message = new ChatMessage(messageText,currentDateandTime,true);
            messages.add(message);
            myAdapter.notifyItemInserted(messages.size()-1);
            //clear the previous text
            binding.textInput.setText("");
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(()-> mDAO.insertMessage(message));

        });

        binding.receiveButton.setOnClickListener(clk->{
            String messageText = binding.textInput.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
            String currentDateandTime = sdf.format(new Date());
            ChatMessage message = new ChatMessage(messageText,currentDateandTime,false);
            messages.add(message);
            myAdapter.notifyItemInserted(messages.size()-1);
            //clear the previous text
            binding.textInput.setText("");
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(()-> mDAO.insertMessage(message));
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                if (viewType == 0) {
                    SentMessageBinding binding = SentMessageBinding.inflate(getLayoutInflater());
                    return new MyRowHolder(binding.getRoot());
                } else {
                    ReceivedMessageBinding binding = ReceivedMessageBinding.inflate(getLayoutInflater());
                    return new MyRowHolder(binding.getRoot());
                }
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder myRowHolder, int position) {
                myRowHolder.messageText.setText("");
                myRowHolder.timeText.setText("");

                ChatMessage obj = messages.get(position);
                myRowHolder.messageText.setText(obj.getMessage());
                myRowHolder.timeText.setText(obj.getTimeSent());

            }

            @Override
            public int getItemCount() {
                return messages.size();
            }

            @Override
            public int getItemViewType(int position){
                ChatMessage obj = messages.get(position);
                return (obj.getIsSentButton() ? 0 : 1);
            }
        });
    }

    class MyRowHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        TextView timeText;
        public MyRowHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(clk ->{
                int position = getAbsoluteAdapterPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder( ChatRoom.this );
                builder.setMessage("Do you want to delete the message: "+ messageText.getText())
                        .setTitle("Question")
                        .setNegativeButton("No",(dialog, cl)->{})
                        .setPositiveButton("Yes",(dialog, cl)->{
                            ChatMessage message = messages.get(position);
                            Executor thread = Executors.newSingleThreadExecutor();
                            thread.execute(()-> mDAO.deleteMessage(message));
                            messages.remove(position);
                            myAdapter.notifyItemRemoved(position);
                            Snackbar.make(messageText, "You deleted message #" + position,Snackbar.LENGTH_LONG)
                                    .setAction("Undo", click ->{
                                        messages.add(position,message);
                                        myAdapter.notifyItemInserted(position);
                                    })
                                    .show();
                        }).create().show();
            });

            messageText = itemView.findViewById(R.id.message);
            timeText= itemView.findViewById(R.id.time);
        }
    }
}