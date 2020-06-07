package com.android.bluetoothmessenger;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.bluetoothmessenger.Content.MessagesLab;

import java.util.List;

import static com.android.bluetoothmessenger.Constants.Constants.BLUETOOTH_DEVICE;
import static com.android.bluetoothmessenger.Constants.Constants.CLIENT;
import static com.android.bluetoothmessenger.Constants.Constants.CONNECTION_NAME;
import static com.android.bluetoothmessenger.Constants.Constants.MESSAGE_KEY;
import static com.android.bluetoothmessenger.Constants.Constants.MESSAGE_WHAT;
import static com.android.bluetoothmessenger.Constants.Constants.RECEIVED_MESSAGE;
import static com.android.bluetoothmessenger.Constants.Constants.RECEIVED_MESSAGE_VIEW_TYPE;
import static com.android.bluetoothmessenger.Constants.Constants.SEND_MESSAGE;
import static com.android.bluetoothmessenger.Constants.Constants.SEND_MESSAGE_VIEW_TYPE;
import static com.android.bluetoothmessenger.Constants.Constants.SERVER;


public class MessagesFragment extends Fragment {
    private BluetoothConnectionService mBluetoothService;
    private BluetoothDevice mBluetoothDevice;

    private RecyclerView mMessagesRecyclerView;
    private static LinearLayoutManager sLinearLayoutManager;
    private static MessagesAdapter sMessagesAdapter;
    private EditText mMessageEditText;
    private Button mSendButton;

    private static List<String[]> sMessages;


    public static MessagesFragment newInstance(String connectionName, BluetoothDevice bluetoothDevice) {
        Bundle arg = new Bundle();
        arg.putString(CONNECTION_NAME, connectionName);
        if (connectionName.equals(CLIENT)) {
            arg.putParcelable(BLUETOOTH_DEVICE, bluetoothDevice);
        }
        MessagesFragment fragment = new MessagesFragment();
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sMessages = MessagesLab.get().getMessages();

        mBluetoothService = new BluetoothConnectionService();

        // pobieranie danych o rodzaju połaczenia (Server, Client), i dla Clienta pobieranie BluetoothDevice
        String connectionName = "";
        if (getArguments() != null) {
           connectionName = getArguments().getString(CONNECTION_NAME);
           if (connectionName.equals(CLIENT)) {
               mBluetoothDevice = getArguments().getParcelable(BLUETOOTH_DEVICE);
           }
        }

       //  startowanie odowiednich wątków Server, Client
        if (connectionName.equals(SERVER)) {
            mBluetoothService.startServer();
        } else if (connectionName.equals(CLIENT)) {
            mBluetoothService.startClient(mBluetoothDevice);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);

        mMessagesRecyclerView = v.findViewById(R.id.messages_recycler);

        // Tworzenie RecyclerView wiadomości
        sLinearLayoutManager = new LinearLayoutManager(getActivity());
        mMessagesRecyclerView.setLayoutManager(sLinearLayoutManager);
        sMessagesAdapter = new MessagesAdapter();
        mMessagesRecyclerView.setAdapter(sMessagesAdapter);

        // Pisanie wiadomości
        mMessageEditText = v.findViewById(R.id.message_edit_text);

        mSendButton = v.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text =  mMessageEditText.getText().toString();
                mBluetoothService.sendMessage(text);
                sMessages.add(new String[] {SEND_MESSAGE, text});

                mMessageEditText.setText("");

                int position = sMessages.size() - 1;
                // odśwież listę wiadomości
                sMessagesAdapter.notifyItemChanged(position);
                sLinearLayoutManager.scrollToPosition(position);
                // mMessagesRecyclerView.setLayoutManager(sLinearLayoutManager);
            }
        });

        return v;
    }


  // Przechwytywanie odebranej wiadomości z BluetoothConnectionService
    public static Handler sMessageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_WHAT) {
                String text = msg.getData().getString(MESSAGE_KEY);
                sMessages.add(new String[] {RECEIVED_MESSAGE, text});

                int position = sMessages.size() - 1;
                // odśwież listę wiadomości
                sMessagesAdapter.notifyItemChanged(position);
                sLinearLayoutManager.scrollToPosition(position);
                // mMessagesRecyclerView.setLayoutManager(sLinearLayoutManager);
            }
        }
    };


    // HOLDER obsługa poszczególnej wiadomości dla Servera(sent) i Klienta(received)
    private class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView mMyContactTextView;
        private TextView mSentMessageTextView;
        private TextView mContactTextView;
        private TextView mReceivedMessageTextView;

        public MessageViewHolder(LayoutInflater inflater, ViewGroup parent, int layoutResource, int viewType) {
            super(inflater.inflate(layoutResource, parent, false));

            // tworzenie odniesienia do odpowiedniego view type
            switch (viewType) {
                case SEND_MESSAGE_VIEW_TYPE:
                    mMyContactTextView = itemView.findViewById(R.id.my_contact_name);
                    mSentMessageTextView = itemView.findViewById(R.id.sent_message);
                    break;
                case RECEIVED_MESSAGE_VIEW_TYPE:
                    mContactTextView = itemView.findViewById(R.id.contact_name);
                    mReceivedMessageTextView = itemView.findViewById(R.id.received_message);
                    break;
            }
        }

        // wiązanie danych wysyłanych
        public void bindSendMessageView(String[] contactMessage) {
            mMyContactTextView.setText(contactMessage[0]);
            mSentMessageTextView.setText(contactMessage[1]);
        }

        //wiązanie danych odbieranych
        public void bindReceivedMessageView(String[] contactMessage) {
            mContactTextView.setText(contactMessage[0]);
            mReceivedMessageTextView.setText(contactMessage[1]);
        }

    }


    // ADAPTER obsługa listy wiadomości
    private class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private int mLayoutResourceViewHolder;

        @Override
        public int getItemViewType(int position) {
            String[] contactMessage = sMessages.get(position);
            switch (contactMessage[0]) {
                case SEND_MESSAGE:
                    mLayoutResourceViewHolder = R.layout.sent_message_item;
                    return SEND_MESSAGE_VIEW_TYPE;
                case RECEIVED_MESSAGE:
                    mLayoutResourceViewHolder = R.layout.received_message_item;
                    return RECEIVED_MESSAGE_VIEW_TYPE;
            }
            return 0;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();

            return new MessageViewHolder(inflater, parent, mLayoutResourceViewHolder, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case SEND_MESSAGE_VIEW_TYPE:
                    holder.bindSendMessageView(sMessages.get(position));
                    break;
                case RECEIVED_MESSAGE_VIEW_TYPE:
                    holder.bindReceivedMessageView(sMessages.get(position));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return sMessages.size();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothService.cancelAllThreads();

        sMessageHandler.getLooper().quit();
    }
}
