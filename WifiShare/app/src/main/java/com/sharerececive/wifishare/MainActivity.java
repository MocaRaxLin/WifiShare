package com.sharerececive.wifishare;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;


public class MainActivity extends TabActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final int STATE_NULL = 0;
    public static final int STATE_SERVER = 1;
    public static final int STATE_CLIENT = 2;
    public static final int STATE_DATA = 3;
    public static final int STATE_ACCOUNT = 4;
    public static final int STATE_SETTING = 5;
    public static final int STATE_REGIST = 6;
    public static final int STATE_LOGOUT = 7;



    private int lockState=0;
    private String filePath = "/sdcard";
    private String fileName = "wifishareCookie";
    public static String Name;
    public static String MasterID = "";
    private int Point;
    private boolean tempBoo = false;

    Button btLogin;
    Button btRegist;
    Button btSent;
    Button btCancel;
    Button btLogout;
    Button btScan;

    EditText edtUserName;
    EditText edtPassword;

    EditText edtRegisterUserName;
    EditText edtRegisterPassword;
    EditText edtRegisterPassword2;

    TextView textName;
    Animation animation;
    ImageView imvSignalWave;
    Switch swtServerOpenRod;

    ImageView imvSignalArrow;
    Switch swtClientOpenRod;

    ListView master_list;
    ListView client_list;

    Data dataList = Data.getInstance() ;
    IoWithServer ioWithServer = IoWithServer.getInstance();
    BlueToothService service;
    Handler mHandler;

    public static  boolean isConnected=false;
    public static  boolean isLogin=true;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //initial

        //initial
        initIsLoginState();
        initialId();
        initialListenner();
        initialBottomMenu();
        updateDataPage();
        cookieValidating();
        iniViewbyCookie();
    }
    private void iniViewbyCookie(){
        if (hascookie()) {
            switchPage(STATE_DATA);
        } else {
            switchPage(STATE_ACCOUNT);
        }
    }

    private void initIsLoginState(){
        if(hascookie())
            isLogin = true;
        else
            isLogin = false;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void reader() {
        // TODO Auto-generated method stub

        // 檔案輸入到buffered
        FileInputStream login = null;
        BufferedReader buffered = null;
        try {
            login = openFileInput("file.txt");
            buffered = new BufferedReader(new InputStreamReader(login));


            String input = buffered.readLine();
            edtUserName.setText(input);
            Name = input;
            input = buffered.readLine();
            edtPassword.setText(input);

            buffered.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    private void writer(String name, String pass) {
        try {
            FileOutputStream login = null;
            BufferedOutputStream buffered = null;

            login = openFileOutput("file.txt", MODE_PRIVATE);
            buffered = new BufferedOutputStream(login);

            buffered.write(name.getBytes());
            buffered.write("\n".getBytes());
            buffered.write(pass.getBytes());
            buffered.write("\n".getBytes());

            buffered.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public void updateDataPage() {
            if (hascookie()) {
                //reader();
                Name = CookieHelper.getInstance().getUserName();
                dataList.set(Name, "0");
                if(ioWithServer.isNetAvailable(this,false))
                    ioWithServer.getRemaining(MainActivity.this);
                else
                    Data.getInstance().set(Name, "Network Unavailable.");
            }
    };
/////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialId() {
        edtUserName = (EditText) findViewById(R.id.edtUsername);
        edtPassword = (EditText) findViewById(R.id.edtPass);

        edtRegisterUserName = (EditText) findViewById(R.id.edtRegistName);
        edtRegisterPassword = (EditText) findViewById(R.id.edtRegistPass);
        edtRegisterPassword2 = (EditText) findViewById(R.id.edtRegistPass2);

        textName = (TextView) findViewById(R.id.textView1);

        animation = AnimationUtils.loadAnimation(this, R.anim.anim_signal_wave);
        imvSignalWave = (ImageView) findViewById(R.id.imvSignalWave);
        swtServerOpenRod = (Switch) findViewById(R.id.swtShare);

        imvSignalArrow = (ImageView) findViewById(R.id.imvSignalArrow);
        swtClientOpenRod = (Switch) findViewById(R.id.swtClient);

        dataList.setActivity(this);

        btLogin = (Button) findViewById(R.id.btLogin);
        btRegist = (Button) findViewById(R.id.btRegist);
        btSent = (Button) findViewById(R.id.btSent);
        btCancel = (Button) findViewById(R.id.btCancel);
        btLogout = (Button) findViewById(R.id.btLogout);
        btScan = (Button) findViewById(R.id.btScan);

        master_list = (ListView) findViewById(R.id.master_paired);
        client_list = (ListView) findViewById(R.id.client_paired);

        service = new BlueToothService();
    }


    private void initialListenner() {
        btLogin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(edtUserName.getText().toString(), edtPassword.getText().toString());
                Name = edtRegisterUserName.getText().toString();
                updateDataPage();
                if(isLogin)
                    switchPage(STATE_DATA);
            }
        });
        btRegist.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPage(STATE_REGIST);
            }
        });

        btScan.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.discovery();
            }
        });

        btSent.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String p1 = edtRegisterPassword.getText().toString();
                String p2 = edtRegisterPassword2.getText().toString();
                if (!p1.equals(p2)) {
                    Toast.makeText(MainActivity.this, "Passwords should be the same!", Toast.LENGTH_LONG).show();

                } else {
                    signUp(edtRegisterUserName.getText().toString(), p1);
                    Name = edtRegisterUserName.getText().toString();
                }
            }
        });
        btCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPage(STATE_ACCOUNT);
            }
        });
        btLogout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        swtServerOpenRod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imvSignalWave.startAnimation(animation);
                    turnOnServer();
                    lockState=STATE_SERVER;
                } else {
                    turnOffServer();
                    lockState=STATE_NULL;
                }
            }
        });

        swtClientOpenRod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imvSignalArrow.startAnimation(animation);
                    turnOnClient();
                    lockState=STATE_CLIENT;
                } else {
                    turnOffClient();
                    lockState=STATE_NULL;
                }
            }
        });
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    private boolean initialBottomMenu() {
        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.main_tab_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.tabServer:
                        switchPage(STATE_SERVER);
                        break;
                    case R.id.tabClient:
                        switchPage(STATE_CLIENT);
                        break;
                    case R.id.tabData:
                        switchPage(STATE_DATA);
                        break;
                    default:
                        break;
                }
            }
        });
        ((RadioGroup)findViewById(R.id.main_tab_group)).clearCheck();
        return true;
    }

    private void switchPage(int pageSelect) {
        //if not login -> lock bottom tap
        if(pageSelect==STATE_SERVER||pageSelect==STATE_CLIENT||pageSelect==STATE_DATA) {
            if (!isLogin) {
                Toast.makeText(getApplication(), "Please login", Toast.LENGTH_SHORT).show();
                ((RadioGroup) findViewById(R.id.main_tab_group)).clearCheck();
                return;
            }
        }

        //when you are opening client -> can't switch server
        if(pageSelect==STATE_SERVER && lockState==STATE_CLIENT) {
            ((RadioGroup) findViewById(R.id.main_tab_group)).clearCheck();
            ((RadioGroup) findViewById(R.id.main_tab_group)).check(R.id.tabClient);
            Toast.makeText(getApplication(), "Please Close CLIENT First", Toast.LENGTH_SHORT).show();
            return;
        }

        //when you are opening server -> can't switch client
        if(pageSelect==STATE_CLIENT && lockState==STATE_SERVER) {
            ((RadioGroup) findViewById(R.id.main_tab_group)).clearCheck();
            ((RadioGroup) findViewById(R.id.main_tab_group)).check(R.id.tabServer);
            Toast.makeText(getApplication(), "Please Close SERVER First", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.layoutShare).setVisibility(View.GONE);
        findViewById(R.id.layoutClient).setVisibility(View.GONE);
        findViewById(R.id.layoutData).setVisibility(View.GONE);
        findViewById(R.id.layoutAccount).setVisibility(View.GONE);
        findViewById(R.id.layoutSetting).setVisibility(View.GONE);
        findViewById(R.id.layoutRegist).setVisibility(View.GONE);
        findViewById(R.id.layoutLogout).setVisibility(View.GONE);




        switch (pageSelect) {
            case STATE_SERVER:
                findViewById(R.id.layoutShare).setVisibility(View.VISIBLE);
                setTitle("SHARE");
                break;
            case STATE_CLIENT:
                findViewById(R.id.layoutClient).setVisibility(View.VISIBLE);
                setTitle("CLIENT");
                break;
            case STATE_DATA:
                findViewById(R.id.layoutData).setVisibility(View.VISIBLE);
                updateDataPage();
                setTitle("DATA");
                break;
            case STATE_ACCOUNT:
                findViewById(R.id.layoutAccount).setVisibility(View.VISIBLE);
                setTitle("ACCOUNT");
                break;
            case STATE_LOGOUT:
                findViewById(R.id.layoutLogout).setVisibility(View.VISIBLE);
                setTitle("LOGOUT");
                break;
            case STATE_SETTING:
                findViewById(R.id.layoutSetting).setVisibility(View.VISIBLE);
                setTitle("SETTING");
                break;
            case STATE_REGIST:
                EditText e = (EditText) findViewById(R.id.edtRegistName);
                EditText e2 = (EditText) findViewById(R.id.edtRegistPass);
                EditText e3 = (EditText) findViewById(R.id.edtRegistPass2);
                e.setText("");
                e2.setText("");
                e3.setText("");
                setTitle("ACCOUNT");
                findViewById(R.id.layoutRegist).setVisibility(View.VISIBLE);
                setTitle("SIGN UP");
                break;
            default:
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionAccount:
                if(isLogin)switchPage(STATE_LOGOUT);
                else switchPage(STATE_ACCOUNT);
                break;
            case R.id.actionSetting:
                switchPage(STATE_SETTING);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


//----實作接口---------------------------------------------------------------------------------------


    //左方滑動頁面的點擊function
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                //不建議在此放入function因為程式執行開始執行時，會執行case1這步驟
                Toast.makeText(this, "Battery Power is: " + getBatteryPower(), Toast.LENGTH_LONG).show();
                break;
        }
    }

    // get battery power
    private String getBatteryPower() {
        String output = "";
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        int intBatteryPct = (int) (batteryPct * 100);
        return String.valueOf(intBatteryPct) + "%";
    }


    //signUpAccount
    private void signUp(String userName, String password) {
        ioWithServer.sendMobileSignUpInfo(this, edtRegisterUserName.getText().toString(), edtRegisterPassword.getText().toString(), edtRegisterPassword2.getText().toString());
        writer(edtRegisterUserName.getText().toString(), edtRegisterPassword.getText().toString());
    }
    //login
    private void login(String userName, String password) {
        ioWithServer.sendMobileLoginInfo(this, edtUserName.getText().toString(), edtPassword.getText().toString());
        writer(edtUserName.getText().toString(), edtPassword.getText().toString());
    }
    private void cookieValidating() {
        ioWithServer.checkCookieValidating(this);
    }
    private void startGetRemainService() {
        startService(new Intent(MainActivity.this, RemainingService.class));
    }
    private void stopGetRemainService() {
        stopService(new Intent(MainActivity.this, RemainingService.class));
    }
    private void startPaymentService() {
        startService(new Intent(MainActivity.this, PaymentService.class));
    }       // 切記改seller ID !!!!!!!!!!!!!!!!!!!!!!!!!  去paymentService.java改
    private void stopPaymentService() {
        stopService(new Intent(MainActivity.this, PaymentService.class));
    }


    //turn on server
    private void turnOnServer() {
        Toast.makeText(this, "Server is opened!", Toast.LENGTH_LONG).show();
        registerReceiver(service.mPairReceiver, new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(service.mPairReceiver, new IntentFilter(
                BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(service.mPairReceiver, new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED));
        service.startBT(this, master_list);
        service.discoverable(this);
        service.EnableBTtethring_master(this);
        startGetRemainService();
    }

    //turn off server
    private void turnOffServer() {
        Toast.makeText(this, "Server is close!", Toast.LENGTH_LONG).show();
        unregisterReceiver(service.mPairReceiver);
        service.stopBT();
        stopGetRemainService();
    }

    //turn on client
    private void turnOnClient() {
        Toast.makeText(this, "Client is Opened!", Toast.LENGTH_LONG).show();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(service.mReceiver, filter);
        service.startBT(this, client_list);
        startPaymentService();
    }

    //turn off client
    private void turnOffClient() {
        Toast.makeText(this, "Client is Closed!", Toast.LENGTH_LONG).show();
        unregisterReceiver(service.mReceiver);
        service.stopBT();
        stopPaymentService();
    }

    public boolean hascookie() {
        File dirPath = new File(filePath);
        File nameFile = new File(fileName);
        File file = new File(dirPath + File.separator + nameFile);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public void logout(){
        //logout function
        stopGetRemainService();
        stopPaymentService();
        CookieHelper.getInstance().deleteFile();


        //reset state value
        ((RadioGroup)findViewById(R.id.main_tab_group)).clearCheck();
        isLogin=false;
        isConnected = false;
        lockState=STATE_NULL;
        swtServerOpenRod.setChecked(false);
        swtClientOpenRod.setChecked(false);


        //switch to login page
        switchPage(STATE_ACCOUNT);
    }

}
