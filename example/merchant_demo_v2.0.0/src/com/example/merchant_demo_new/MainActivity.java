package com.example.merchant_demo_new;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.ipaynow.mhtdemo.R;
import com.ipaynow.plugin.api.IpaynowPlugin;
import com.ipaynow.plugin.manager.route.dto.ResponseParams;
import com.ipaynow.plugin.manager.route.impl.ReceivePayResult;
import com.ipaynow.plugin.utils.PreSignMessageUtil;
import com.ipaynow.plugin.view.IpaynowLoading;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends Activity implements ReceivePayResult {
    // TODO 签名接口，仅供测试时使用，结束请替换成自己的签名生成接口地址
    @SuppressWarnings("unused")
    private static final String GETORDERMESSAGE_URL = "http://posp.ipaynow.cn/ZyPluginPaymentTest_PAY/api/pay2.php";
    // TODO 测试账号仅供开发者测试时使用
    private static final String mAppID = "1408709961320306";
    private static final String mKey = "0nqIDgkOnNBD6qoqO5U68RO1fNqiaisg";

    private EditText mAppIdET;
    private EditText mAppKeyET;
    private EditText mAmtET;
    private EditText mReservedET;
    private EditText mOrderDatailET;
    private EditText mUrlET;
    private EditText mOrderNameET;
    private RadioGroup mLimitPayRG;

    private IpaynowPlugin mIpaynowplugin;
    private IpaynowLoading mLoadingDialog;
    private PreSignMessageUtil mPreSign = new PreSignMessageUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIpaynowplugin = IpaynowPlugin.getInstance().init(this);// 1.插件初始化
        mIpaynowplugin.unCkeckEnvironment();// 无论微信、qq安装与否，网关页面都显示渠道按钮。
        initUI();
    }

    private void initUI() {
        mLoadingDialog = mIpaynowplugin.getDefaultLoading();
        mAmtET = (EditText) findViewById(R.id.et_amt);
        mAppIdET = (EditText) findViewById(R.id.et_appid);
        mAppIdET.setText(mAppID);
        mAppKeyET = (EditText) findViewById(R.id.et_appKey);
        mAppKeyET.setText(mKey);
        mReservedET = (EditText) findViewById(R.id.et_resever);
        mUrlET = (EditText) findViewById(R.id.et_url);
        mOrderNameET = (EditText) findViewById(R.id.et_name);
        mOrderDatailET = (EditText) findViewById(R.id.et_detal);
        mLimitPayRG = (RadioGroup) findViewById(R.id.rg_limitpay);
    }
    /**
     * 处理点击支付按钮事件
     * 
     * @param v
     */
    public void onClick(View v) {
        if (!checkNetInfo()) {
            return;
        }
        //显示Loading界面
        showProgressDialog();
        String no = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        //创建订单
        creatPayMessage(no, no);
        switch (v.getId()) {
        case R.id.button1:
            mPreSign.payChannelType = ""; // 通过网关页面选择支付方式
            break;
        case R.id.button2:
            mPreSign.payChannelType = "12"; // 支付宝支付
            break;
        case R.id.button3:
            mPreSign.payChannelType = "11"; // 银联支付
            break;
        case R.id.button4:
            mPreSign.payChannelType = "1310";// 微信插件支付
            break;
        case R.id.button5:
            mPreSign.payChannelType = "13"; // 微信wap版支付
            break;
        case R.id.button6:
            mPreSign.payChannelType = "50"; // 百度支付
            break;
        case R.id.button7:
            mPreSign.payChannelType = "25"; // qq支付
            break;
        default:
            return;
        }
        // 生成请求参数并调起支付
        GetMessage gm = new GetMessage();
        gm.execute(mPreSign.generatePreSignMessage());
    }

    private boolean checkNetInfo() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            Toast.makeText(MainActivity.this, "请检查网络连接状态", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * 
     */
    private void showProgressDialog() {
        mLoadingDialog.setLoadingMsg("正在生成订单");
        mLoadingDialog.show();
    }

    /**
     * 创建订单
     * 
     * @param mhtOrderNo  商户订单号
     * @param mhtOrderStartTime 创建订单时间
     */
    private void creatPayMessage(String mhtOrderNo, String mhtOrderStartTime) {

        mPreSign.appId = mAppIdET.getText().toString();
        mPreSign.mhtOrderNo = mhtOrderNo;
        mPreSign.mhtOrderName = mOrderNameET.getText().toString();
        mPreSign.mhtOrderAmt = mAmtET.getText().toString();
        mPreSign.mhtOrderDetail = mOrderDatailET.getText().toString();
        mPreSign.mhtOrderStartTime = mhtOrderStartTime;
        mPreSign.mhtReserved = mReservedET.getText().toString();
        mPreSign.notifyUrl = mUrlET.getText().toString();
        mPreSign.mhtOrderType = "01";
        mPreSign.mhtCurrencyType = "156";
        mPreSign.mhtOrderTimeOut = "3600";
        mPreSign.mhtCharset = "UTF-8";
        mPreSign.payChannelType = "13";
        mPreSign.consumerId = "456123";
        mPreSign.consumerName = "yuyang";
        if(mLimitPayRG.getCheckedRadioButtonId()==R.id.rb_sup){
            mPreSign.mhtLimitPay = null;
        }else{
            mPreSign.mhtLimitPay = "no_credit";
        }
    }

    public class GetMessage extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... params) {
            // 待签名串
            String msg = params[0];
            // 生成签名串；请在自己服务器进行生成签名，具体请看服务器签名文档
            // String signStr = HttpUtil.post(GETORDERMESSAGE_URL, "paydata=" +
            // MerchantTools.urlEncode(mPreSignStr));
            // 支付接口请求参数格式：
            // String requestMessage = mPreSignStr + "&" + signStr;
            // 注意:前端生成签名串只用于演示
            String needcheckmsg = "mhtSignature="
                    + Md5Util.md5(msg + "&" + Md5Util.md5(mAppKeyET.getText().toString())) + "&mhtSignType=MD5";// 0nqIDgkOnNBD6qoqO5U68RO1fNqiaisg
            needcheckmsg = msg + "&" + needcheckmsg;
            return needcheckmsg;
        }

        protected void onPostExecute(String requestMessage) {
            mLoadingDialog.dismiss();
            // 如商户保留域mhtReserved包含特殊字符，在调起插件前对value做一次utf8编码
            if (!TextUtils.isEmpty(mPreSign.mhtReserved) && requestMessage.contains(mPreSign.mhtReserved))
                requestMessage = requestMessage.replace(mPreSign.mhtReserved, URLEncoder.encode(mPreSign.mhtReserved));
            // 设置支付结果回调接口，同时调起支付请求
            Log.i("TAG", requestMessage);
            mIpaynowplugin.setCustomLoading(mLoadingDialog).setCallResultReceiver(MainActivity.this)
                    .pay(requestMessage);
        }
    }

    @Override
    public void onIpaynowTransResult(ResponseParams arg0) {
        String respCode = arg0.respCode;
        String errorCode = arg0.errorCode;
        String errorMsg = arg0.respMsg;
        StringBuilder temp = new StringBuilder();
        if (respCode.equals("00")) {
            temp.append("交易状态:成功");
        } else if (respCode.equals("02")) {
            temp.append("交易状态:取消");
        } else if (respCode.equals("01")) {
            temp.append("交易状态:失败").append("\n").append("错误码:").append(errorCode).append("原因:" + errorMsg);
        } else if (respCode.equals("03")) {
            temp.append("交易状态:未知").append("\n").append("原因:" + errorMsg);
        } else {
            temp.append("respCode=").append(respCode).append("\n").append("respMsg=").append(errorMsg);
        }
        Toast.makeText(this, "onIpaynowTransResult:" + temp.toString(), Toast.LENGTH_LONG).show();
    }
}
