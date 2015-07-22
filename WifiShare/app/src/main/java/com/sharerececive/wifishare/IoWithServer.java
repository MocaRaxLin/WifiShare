package com.sharerececive.wifishare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.ResponseCache;
import java.util.List;

@SuppressLint("LongLogTag")
public class IoWithServer {
    private static IoWithServer ioWithServer=null;
    private String signupURL = "http://140.115.52.120/network_sharing/sign_up.php";
    private String loginURL = "http://140.115.52.120/network_sharing/log_in.php";
    //private String loginURL = "http://140.115.203.64/androLogin.php";
   /* private String validatingURL = "http://140.115.203.64/andro_validating.php";
    private String validatingDomain = "140.115.203.64";
    private String validatingPath = "/andro_validating.php"; */
    private String validatingURL = "http://140.115.52.120/network_sharing/authentication.php";
    private String validatingDomain = "140.115.52.120";
    private String validatingPath = "/network_sharing/authentication.php";
    //private String payURL = "http://140.115.52.120/network_sharing/pay.php";
    private String payment_remainURL = "http://140.115.52.120/network_sharing/remaining.php";
    private String payment_remainPath = "/network_sharing/remaining.php";
    private String payment_deductURL = "http://140.115.52.120/network_sharing/deduction_test.php";
    private String payment_deductPath = "/network_sharing/deduction_test.php";

// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----  called by clicked event//
    public static IoWithServer getInstance(){
        if(ioWithServer==null)
            ioWithServer = new IoWithServer();
        return ioWithServer;
    }

    public void sendMobileSignUpInfo(Context context, String userId , String userPw , String userPw2){
        if(isNetAvailable(context , true))
            new mobileSignUpTask(context).execute( userId , userPw , userPw2 );
    }

    public void sendMobileLoginInfo(Context context, String userId , String userPw ){
        if(isNetAvailable(context , true))
            new mobileLoginTask(context).execute( userId , userPw );
    }

    public void checkCookieValidating(Context context){
        if(isNetAvailable(context , false))
            new cookieValidatingTask(context).execute();
    }

    public void getRemaining(Context context){
        if(isNetAvailable(context , false))
            new getRemainTask(context).execute();
    }

    public void sendPayment(Context context, String sellerID){
        if(isNetAvailable(context , false))
            new paymentTask(context).execute(sellerID);
    }

// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- //

    public boolean isNetAvailable(Context context , boolean hintFlag){
        ConnectivityManager conxMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNwInfo = conxMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNwInfo   = conxMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(!((mobileNwInfo== null? false : mobileNwInfo.isConnected() )|| (wifiNwInfo == null? false : wifiNwInfo.isConnected()) || MainActivity.isConnected)) {
            if(hintFlag)
                Toast.makeText(context, "Please make sure your network connection is active.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- //

    private class mobileSignUpTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;

        public mobileSignUpTask(Context context){
            mContext = context;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            if (success == true) {
                Log.d("%%%%%% SignUp Msg %%%%%%", "SignUp seccess");
                Toast.makeText(mContext, "Successfully signed up !", Toast.LENGTH_LONG).show();
            } else {
                Log.d("%%%%%% SignUp Msg %%%%%%", "SignUp failed ...");
                Toast.makeText(mContext, "Sign up failed ...", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... login) {
            String responseStr = null;
            String id = login[0];
            String pw = login[1];
            String pw2 = login[2];

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(signupURL + "/?useremail=" + id + "&password=" + pw + "&passwordretype=" + pw2 );

            try {
                HttpResponse response = httpclient.execute(httpget);
                responseStr = EntityUtils.toString(response.getEntity());
                Log.d("%%%%%% SignUpResponse %%%%%%", responseStr);
                //  Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (responseStr.trim().equals("1")) {
                return true;
            } else {
                return false;
            }
        }
    }

 // ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- //
    private class mobileLoginTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;

        public mobileLoginTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success == true) {
                MainActivity.isLogin = true;
                Log.d("%%%%%% Login Msg %%%%%%", "login success");
                Toast.makeText(mContext, "Successfully log in and set-Cookie!", Toast.LENGTH_LONG).show();
            } else {
                Log.d("%%%%%% Login Msg %%%%%%", "login failed ...");
                Toast.makeText(mContext, "Log in failed ...", Toast.LENGTH_LONG).show();
                MainActivity.isLogin = false;
            }
        }

        @Override
        protected Boolean doInBackground(String... login) {

            String responseStr = null;
            String id = login[0];
            String pw = login[1];

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(loginURL + "/?useremail=" + id + "&password=" + pw);

        /*  HttpPost httppost = new HttpPost(url);                                        // This block is used to send http/POST request.
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", id));
            nameValuePairs.add(new BasicNameValuePair("pw", pw));
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                return false;
            }   */

            try {
                HttpResponse response = httpclient.execute(httpget);
                responseStr = EntityUtils.toString(response.getEntity());
                Log.d("%%%%%% LogInResponse %%%%%%", responseStr);

                List<Cookie> cookies = httpclient.getCookieStore().getCookies();
                if (cookies.isEmpty()) {
                    Log.d("&&& Cookies received MSG: &&&","no cookies received...");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        if(cookies.get(i).getName().contentEquals("user_email")) {
                            CookieHelper.getInstance().writeCookieTofile(cookies.get(i));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (responseStr.trim().equals("1")) {
                return true;
            } else {
                return false;
            }
        }
    }

// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- //

    private class cookieValidatingTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;
        private String  validatingMsg;

        public cookieValidatingTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success == true) {
                Log.d("%%%%%% cookieValidating Msg %%%%%%", "cookieValidating success");
                Toast.makeText(mContext, "Welcome back , "+ validatingMsg +" !", Toast.LENGTH_LONG).show();
            } else {
                Log.d("%%%%%% cookieValidating Msg %%%%%%", "cookieValidating failed ...");
                Toast.makeText(mContext,  validatingMsg  , Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... login) {

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(validatingURL);
            BasicCookieStore store = new BasicCookieStore();
            HttpContext ctx = new BasicHttpContext();

            CookieHelper.getInstance().retrieveCookie(store , validatingDomain , validatingPath);           // retrive cookie info from sdcard
            ctx.setAttribute(ClientContext.COOKIE_STORE, store);

            try {
                HttpResponse response = httpclient.execute(httpget , ctx);
                validatingMsg = EntityUtils.toString(response.getEntity());
                Log.d("%%%%%% cookieValidating Response %%%%%%", validatingMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (validatingMsg.trim().equals("1")) {
                validatingMsg = store.getCookies().get(0).getValue();
                return true;
            } else {
                return false;
            }
        }
    }

// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- //

    private class getRemainTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;
        private String responseStr = "0";

        public getRemainTask(Context context){
            mContext = context;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            if (success == true) {
                Log.d("%%%%%% GetRemain Msg %%%%%%", "Get success");
                //Toast.makeText(mContext, "Remaining : "+responseStr, Toast.LENGTH_SHORT).show();
                Data.getInstance().set(MainActivity.Name, responseStr);
            } else {
                Log.d("%%%%%% GetRemain Msg %%%%%%", "Get failed ...");
                //Toast.makeText(mContext, "Get Remaing failed ...", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... msg) {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(payment_remainURL);
            BasicCookieStore store = new BasicCookieStore();
            HttpContext ctx = new BasicHttpContext();

            CookieHelper.getInstance().retrieveCookie(store , validatingDomain , payment_remainPath);
            ctx.setAttribute(ClientContext.COOKIE_STORE, store);

            try {
                HttpResponse response = httpclient.execute(httpget , ctx);
                responseStr = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!responseStr.trim().equals("-1")) {
                return true;
            } else {
                return false;
            }
        }
    }

// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- //

private class paymentTask extends AsyncTask<String, Void, Boolean> {
    private Context mContext;
    private String responseStr = "0";

    public paymentTask(Context context){
        mContext = context;
    }


    @Override
    protected void onPostExecute(final Boolean success) {
        if (success == true) {
            Log.d("%%%%%% Payment Msg %%%%%%", "Pay success");
            //Toast.makeText(mContext, "Remaining : "+responseStr, Toast.LENGTH_SHORT).show();
            Data.getInstance().set(MainActivity.Name, responseStr);
        } else {
            Log.d("%%%%%% Payment Msg %%%%%%", "Pay failed ...");
            //Toast.makeText(mContext, "Pay failed ...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected Boolean doInBackground(String... msg) {
        String sellerID = msg[0];

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(payment_deductURL + "/?selleremail=" + sellerID);
        BasicCookieStore store = new BasicCookieStore();
        HttpContext ctx = new BasicHttpContext();

        CookieHelper.getInstance().retrieveCookie(store , validatingDomain , payment_deductPath);
        ctx.setAttribute(ClientContext.COOKIE_STORE, store);

        try {
            HttpResponse response = httpclient.execute(httpget , ctx);
            responseStr = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("####SLAVE print SELLER ID#####", sellerID);
        Log.d("^^^^SLAVE print response^^^^^", responseStr);

        if (!responseStr.trim().equals("-1")) {
            return true;
        } else {
            return false;
        }
    }
}

// ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- ----- //
}
