package com.sharerececive.wifishare;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by WangQin on 2015/3/7.
 */
public class BlueToothService {
    public BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private DeviceListAdapter mAdapter;
    private ProgressDialog mProgressDlg;

    public ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    int theMaster = 0;
    Context mcontext;
    public AcceptThread mAcceptThread;
    private SConnectedThread SConnectedThread;

    public void startBT(Context context, ListView mListView) {
        mcontext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
//            showToast(context,"Bluetooth is unsupported by this device");
        } else {
            if (!mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.enable();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            if (pairedDevices == null || pairedDevices.size() == 0) {
            } else {
                for (BluetoothDevice bluetoothDevice : pairedDevices) {
                    unpairDevice(bluetoothDevice);
                }
            }
        }
        mDeviceList = new ArrayList<BluetoothDevice>();
        mAdapter = new DeviceListAdapter(context);
        mAdapter.setData(mDeviceList);
        mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position) {
                BluetoothDevice device = mDeviceList.get(position);
                theMaster = position;
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device);
//                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
//                            .getBondedDevices();
//                    mDeviceList.clear();
//                    mDeviceList.addAll(pairedDevices);
//                    mAdapter.notifyDataSetChanged();
                } else {
//                    showToast(context,"Pairing...");
                    pairDevice(device);
//                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
//                            .getBondedDevices();
//                    mDeviceList.clear();
//                    mDeviceList.addAll(pairedDevices);
//                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mListView.setAdapter(mAdapter);
        mProgressDlg = new ProgressDialog(context);
        mProgressDlg.setMessage("Scan Devices...");
    }

    public void stopBT() {
        mDeviceList.clear();
        mAdapter.notifyDataSetChanged();
        if (mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.disable();
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
                .show();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond");
            method.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond",
                    (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void EnableBTtethring_master(Context context) {
        try {
            String sClassName = "android.bluetooth.BluetoothPan";

            Class<?> classBluetoothPan = Class.forName(sClassName);

            Constructor<?> ctor = classBluetoothPan.getDeclaredConstructor(
                    Context.class, BluetoothProfile.ServiceListener.class);
            ctor.setAccessible(true);
            Object instance = ctor.newInstance(context,
                    new BTPanServiceListener_master(context));
            // Set Tethering ON
            Class[] paramSet = new Class[1];
            paramSet[0] = boolean.class;

            Method setTetheringOn = classBluetoothPan.getDeclaredMethod(
                    "setBluetoothTethering", paramSet);

            setTetheringOn.invoke(instance, true);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BTPanServiceListener_master implements
            BluetoothProfile.ServiceListener {

        private final Context context;

        public BTPanServiceListener_master(final Context context) {
            this.context = context;
        }

        @Override
        public void onServiceConnected(final int profile,
                                       final BluetoothProfile proxy) {
        }

        @Override
        public void onServiceDisconnected(final int profile) {
        }
    }

    public void EnableBTtethring_client(Context context) {
        try {
            String sClassName = "android.bluetooth.BluetoothPan";

            Class<?> classBluetoothPan = Class.forName(sClassName);

            Constructor<?> ctor = classBluetoothPan.getDeclaredConstructor(
                    Context.class, BluetoothProfile.ServiceListener.class);
            ctor.setAccessible(true);
            Object instance = ctor.newInstance(context,
                    new BTPanServiceListener_client(context));

            Method setTetheringOn = classBluetoothPan.getDeclaredMethod(
                    "connect", BluetoothDevice.class);
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    try {
                        setTetheringOn.invoke(instance, device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class BTPanServiceListener_client implements
            BluetoothProfile.ServiceListener {

        private final Context context;

        public BTPanServiceListener_client(final Context context) {
            this.context = context;
        }

        @Override
        public void onServiceConnected(final int profile,
                                       final BluetoothProfile proxy) {

        }

        @Override
        public void onServiceDisconnected(final int profile) {
        }
    }

    public void discoverable(Context context) {
        Intent di = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        di.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        context.startActivity(di);
    }

    public void closediscoverable(Context context) {
        Intent di = new Intent(
                BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        di.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
        context.startActivity(di);
    }

    public void discovery() {
        mBluetoothAdapter.startDiscovery();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mBluetoothAdapter.cancelDiscovery();
            }
        }, 10000);
    }

    public final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    mAcceptThread = new AcceptThread();
                    mAcceptThread.start();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent
                        .getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED
                        && prevState == BluetoothDevice.BOND_BONDING) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                            .getBondedDevices();
                    MainActivity.isConnected = true;
                    showToast(context, "isConnected = true");
                    mDeviceList.clear();
                    mDeviceList.addAll(pairedDevices);
                    mAdapter.notifyDataSetChanged();
                } else if (state == BluetoothDevice.BOND_NONE
                        && prevState == BluetoothDevice.BOND_BONDED) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                            .getBondedDevices();
                    mDeviceList.clear();
                    mDeviceList.addAll(pairedDevices);
                    mAdapter.notifyDataSetChanged();
                }
                mAdapter.notifyDataSetChanged();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                MainActivity.isConnected = false;
                showToast(context, "isConnected = false");
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                        .getBondedDevices();
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                unpairDevice(device);
                mDeviceList.clear();
                mDeviceList.addAll(pairedDevices);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showToast(context, "Bluetooth is On");
                    discovery();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent
                        .getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED
                        && prevState == BluetoothDevice.BOND_BONDING) {
                    MainActivity.isConnected = true;
                    showToast(context, "isConnected = true");
                    BluetoothDevice device = mDeviceList.get(theMaster);
                    mConnectThread = new ConnectThread(device);
                    mConnectThread.start();
                    EnableBTtethring_client(context);
                    showToast(context, "Paired");
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                            .getBondedDevices();
                    mDeviceList.clear();
                    mDeviceList.addAll(pairedDevices);
                    mAdapter.notifyDataSetChanged();
                } else if (state == BluetoothDevice.BOND_NONE
                        && prevState == BluetoothDevice.BOND_BONDED) {
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                            .getBondedDevices();
                    mDeviceList.clear();
                    mDeviceList.addAll(pairedDevices);
                    mAdapter.notifyDataSetChanged();
                }

                mAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList.clear();
                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                mProgressDlg.dismiss();
                mAdapter.notifyDataSetChanged();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);

                showToast(context, "Found device " + device.getName());
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                MainActivity.isConnected = false;
                showToast(context, "isConnected = false");
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                        .getBondedDevices();
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                unpairDevice(device);
                mDeviceList.clear();
                mDeviceList.addAll(pairedDevices);
                mAdapter.notifyDataSetChanged();
            }
        }
    };
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            // get a BluetoothSocket to connect with the given BluetoothDevice
            // randomUUID is the app's UUID String, also used by the server code
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {

            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            mBluetoothAdapter.cancelDiscovery();// cancel the discovery because
            // it will
            // slow down the connection
            try {
                mmSocket.connect();// connect the device through the socket .
                // This
                // will block until it succeeds or throws an
                // exception
            } catch (IOException connectionException) {
                try {
                    mmSocket.close();// unable to connect ;close the socket and
                    // get
                    // out
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }

            // do some work to communication
            connected(mmSocket);
        }

        public void cancel() {
            // TODO Auto-generated method stub

        }
    }

    public void connected(BluetoothSocket socket) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            long trx = 0,ttx = 0;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream

                    mmInStream.read(buffer);
                    String input = new String(buffer);
                    String[] temp = input.split("");
                    int index = 0;
                    for (int i = 0;i<temp.length;i++){
                        if(temp[i].equals("#")){
                            index = i;
                        }
                    }
                    input = input.substring(0,index- 1);
                    MainActivity.MasterID = input;
                    Log.d("1111111111111",MainActivity.MasterID);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter
                        .listenUsingRfcommWithServiceRecord(
                                "test",
                                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
                    // do some work to communication
                    Sconnected(socket);
                    break;
                }

            }
        }

        private void cancel() {
            // TODO Auto-generated method stub
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Sconnected(BluetoothSocket socket) {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        SConnectedThread = new SConnectedThread(socket);
        SConnectedThread.start();
    }

    private class SConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;

        public SConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmOutStream = tmpOut;
        }

        public void run() {

            // Keep listening to the InputStream while connected
            // Read from the InputStream
            while (true) try {
//                    String msg = String.valueOf(rxBytes) + " " + String.valueOf(txBytes) + " ";
                write((MainActivity.Name + "#").getBytes());
                this.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer
         *            The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
