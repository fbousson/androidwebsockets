package be.ordina.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class HelloAndroidActivity extends Activity {

    private static final String TAG = "WS-AUTBAHN";


    private EditText _serverLocation;
    private ToggleButton _connectButton;
    private TextView _reveiveText;
    private Button _sendTextButton;
    private EditText _textToSend;


    private final WebSocketConnection mConnection = new WebSocketConnection();


    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _serverLocation = (EditText) findViewById(R.id.server_location);

        _connectButton = (ToggleButton) findViewById(R.id.connectButton);
        _connectButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    String serverUrl = _serverLocation.getText().toString();
                    connectWebSocket(serverUrl);
                } else {
                    closeWebsocket();
                }
            }
        });

        _textToSend = (EditText) findViewById(R.id.text_to_send);

        _sendTextButton = (Button) findViewById(R.id.send_text_button);
        _sendTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = _textToSend.getText().toString();
                try {
                    if(mConnection != null){
                        mConnection.sendTextMessage(text);
                    }else{
                        quickToast("No websocket connection available");
                    }
                } catch (WebsocketNotConnectedException e) {
                    quickToast("Websocket not connected");
                }

            }
        });

        _reveiveText = (TextView) findViewById(R.id.received_text);
    }

    private void closeWebsocket() {
        if(mConnection != null){
            mConnection.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private void connectWebSocket(final String wsuri) {

        try {
            mConnection.connect(wsuri, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.d(TAG, "Status: Connected to " + wsuri);
                    quickToast("Connected");
                    mConnection.sendTextMessage("Hello, world! Autobahn is live");
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(TAG, "Got echo: " + payload);
                    final String message = payload;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _reveiveText.setText(message);
                        }
                    });
                }
                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Connection lost.");
                }
            });
        } catch (WebSocketException e) {
            Log.d(TAG, e.toString());
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        closeWebsocket();
    }

    private void quickToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

}

