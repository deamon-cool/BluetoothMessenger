package com.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static com.android.bluetoothmessenger.Constants.Constants.MESSAGE_KEY;
import static com.android.bluetoothmessenger.Constants.Constants.MESSAGE_WHAT;
import static com.android.bluetoothmessenger.Constants.Constants.UUID_SDP;

public class BluetoothConnectionService {
    private BluetoothAdapter mAdapter;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // wyłącza wątek ConnectedThread i ConnectThread, startuje AcceptThread dla serwera
    public void startServer() {
        cancelAllThreads();
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    // wyłącza wątek ConnectedThread i AcceptThread, startuje ConnectThread dla klienta
    public void startClient(BluetoothDevice device) {
        cancelAllThreads();
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    // wyłącza wszystkie wątki
    public void cancelAllThreads() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    // wysłanie wiadomości do zdalnego urządzenia
    public void sendMessage(String message) {
        if (message.equals("")) {
            message = " ";
        }
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        mConnectedThread.send(bytes);
    }

    // odbieranie wiadomości i przenoszenie dla MessagesFragment sMessageHandler
    private void receivedMessage(String text) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_KEY, text);

        Message message = Message.obtain();
        message.what = MESSAGE_WHAT;
        message.setData(bundle);

        MessagesFragment.sMessageHandler.sendMessage(message);
    }


    // akceptowanie połączenia z zewnątrz, Server
    private class AcceptThread extends Thread {
        private final static String SERVICE_NAME = "bluetooth_messenger";
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket temporaryServerSocket = null;

            try {
                // nasłuchiwanie na kanale RFCOMM włączone
                temporaryServerSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, UUID_SDP);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmServerSocket = temporaryServerSocket;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;

            try {
                // czeka na połączenie z zewnątrz i akceptuje   // accept() - metoda blikująca, czeka i zatrzymuje wyk. wątku
                socket = mmServerSocket.accept();

/// ...... ........ZRÓB                //??? // zrobić tu Toast  "Czekanie na połączenie przychodzące..."?

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (socket != null) {
                // połączono ! :)
                connected(socket);
            }
        }

        private void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // połączenie wychodzące na zewnątrz, Client
    private class ConnectThread extends Thread {
        private BluetoothDevice mDevice;
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
        }

        @Override
        public void run() {
            BluetoothSocket temporarySocket = null;

            try {
                // tworzy Socet, który jest gotowy do startowania wychodzącego połączenia do mDevice za pomocą UUID_SDP
                temporarySocket = mDevice.createInsecureRfcommSocketToServiceRecord(UUID_SDP);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmSocket = temporarySocket;

            // wyłączanie szukania innych urządzeń, aby pasmo bluetooth nie było przeciążone
            mAdapter.cancelDiscovery();

            try {
                // próbuje połączyć się ze zdalnym urządzeniem   // connect() - metoda blokująca, czeka i zatrzymuje wyk. wątku
                mmSocket.connect();

//... ............. ZRÓB               ??? // Zroibić tu Toast ? "Lączy z urządzeniem..."

            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            // Połączono ! :)
            connected(mmSocket);
        }

        private void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // start wątku ConnectedThread
    private void connected(BluetoothSocket socket) {
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

//.... ....... ZRÓB        ??? // Zrobić tu Toast ? "Połączono"

    }


    // połączenie nawiązane, wysyłanie/odbieranie danych byte[]
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInputStream;
        private OutputStream mmOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream temporaryInput = null;
            OutputStream temporaryOutput = null;

            try {
                temporaryInput = mmSocket.getInputStream();
                temporaryOutput = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInputStream = temporaryInput;
            mmOutputStream = temporaryOutput;
        }

        @Override
        public void run() {
            byte[] bufferStream = new byte[1024];
            int numberOfBytesRead;

            while (true) {
                try {
                    // odbieranie danych od urządzenia zdalnego typu byte[]
                    numberOfBytesRead = mmInputStream.read(bufferStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                // Wysyłanie message z BluetoothConnectionService do MessagesFragment
                receivedMessage(new String(bufferStream, 0, numberOfBytesRead));
            }
        }

        // wyślij wiadomość w postaci byte[]
        private void send(byte[] bytes) {
            try {
                // wysyłanie danych do zdalnego urządzenia typu byte[]
                mmOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
