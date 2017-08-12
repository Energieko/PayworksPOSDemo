package ch.chaz.payworksposdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//PAYWORKS IMPORTS
import java.math.BigDecimal;
import java.util.EnumSet;
import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;

import static ch.chaz.payworksposdemo.R.id.amountfield;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    Button btn[] = new Button[14];
    Button buttonpay;
    Button buttonrefund;
    EditText userinput;
    //this will hold the payamount for the paybutton transaction parameters
    BigDecimal payamount;
    //this will remember the last charged amount
    BigDecimal refundamount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //register the  keypad buttons for the grid
        btn[0] = (Button) findViewById(R.id.button1);
        btn[1] = (Button) findViewById(R.id.button2);
        btn[2] = (Button) findViewById(R.id.button3);
        btn[3] = (Button) findViewById(R.id.button4);
        btn[4] = (Button) findViewById(R.id.button5);
        btn[5] = (Button) findViewById(R.id.button6);
        btn[6] = (Button) findViewById(R.id.button7);
        btn[7] = (Button) findViewById(R.id.button8);
        btn[8] = (Button) findViewById(R.id.button9);
        btn[9] = (Button) findViewById(R.id.button0);
        btn[10] = (Button) findViewById(R.id.button00);
        btn[11] = (Button) findViewById(R.id.buttondecimal);
        btn[12] = (Button) findViewById(R.id.buttondelete);
        btn[13] = (Button) findViewById(R.id.buttonclear);

        //register onClick event for keypad buttons
        for (int i = 0; i < 14; i++) {
            btn[i].setOnClickListener(this);
        }

        //register pay and refund buttons
        buttonpay = (Button) findViewById(R.id.buttonpay);
        buttonpay.setOnClickListener(MainActivity.this);

        buttonrefund = (Button) findViewById(R.id.buttonrefund);
        buttonrefund.setOnClickListener(MainActivity.this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonpay:
                paymentButtonClicked();
                break;
            case R.id.buttonrefund:
                refundButtonClicked();
                break;
            case R.id.button1:
                addtoarray("1");
                break;
            case R.id.button2:
                addtoarray("2");
                break;
            case R.id.button3:
                addtoarray("3");
                break;
            case R.id.button4:
                addtoarray("4");
                break;
            case R.id.button5:
                addtoarray("5");
                break;
            case R.id.button6:
                addtoarray("6");
                break;
            case R.id.button7:
                addtoarray("7");
                break;
            case R.id.button8:
                addtoarray("8");
                break;
            case R.id.button9:
                addtoarray("9");
                break;
            case R.id.button0:
                addtoarray("0");
                break;
            case R.id.button00:
                addtoarray("00");
                break;
            case R.id.buttondecimal:
                addtoarray(".");
                break;
            case R.id.buttondelete:

                //get the length of input
                int slength = userinput.length();

                if(slength > 0){

                    //get the last character of the input
                    String selection = userinput.getText().toString().substring(slength-1, slength);
                    String result = userinput.getText().toString().replace(selection, "");
                    userinput.setText(result);
                    userinput.setSelection(userinput.getText().length());

                }

                break;
            case R.id.buttonclear:
                userinput = (EditText)findViewById(amountfield);
                userinput.setText("");
                break;
            default:

        }
    }

    public void addtoarray(String numbers){
        //register TextBox
        userinput = (EditText)findViewById(amountfield);
        userinput.append(numbers);
        //convert whats in the field to payamount variable in TranascationParameters
        EditText inputTxt = (EditText) findViewById(amountfield);
        String payamountstring = inputTxt.getText().toString();
        payamount = new BigDecimal(payamountstring);
    }

//THIS IS THE PAYWORKS CODE
    //PAY BUTTON
   public void paymentButtonClicked() {

        MposUi ui = MposUi.initialize(this, ProviderMode.MOCK,
                "merchantIdentifier", "merchantSecretKey");

        ui.getConfiguration().setSummaryFeatures(EnumSet.of(
                // Add this line, if you do want to offer printed receipts
                // MposUiConfiguration.SummaryFeature.PRINT_RECEIPT,
                MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL)
        );

        // Start with a mocked card reader:
        AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MOCK)
                .mocked()
                .build();
        ui.getConfiguration().setTerminalParameters(accessoryParameters);

        TransactionParameters transactionParameters = new TransactionParameters.Builder()
                .charge(payamount, io.mpos.transactions.Currency.USD)
                .subject("Double Cheeseburger")
                .customIdentifier("yourReferenceForTheTransaction")
                .build();

        Intent intent = ui.createTransactionIntent(transactionParameters);
        startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
       //CHAZ CODE null out the pay amount and prep for refund
        refundamount=payamount;
        payamount=null;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MposUi.REQUEST_CODE_PAYMENT) {
            if (resultCode == MposUi.RESULT_CODE_APPROVED) {
                // Transaction was approved
                Toast.makeText(this, "Transaction approved", Toast.LENGTH_LONG).show();
            } else {
                // Card was declined, or transaction was aborted, or failed
                // (e.g. no internet or accessory not found)
                Toast.makeText(this, "Transaction was declined, aborted, or failed",
                        Toast.LENGTH_LONG).show();
            }
            // Grab the processed transaction in case you need it
            // (e.g. the transaction identifier for a refund).
            // Keep in mind that the returned transaction might be null
            // (e.g. if it could not be registered).
            Transaction transaction = MposUi.getInitializedInstance().getTransaction();

            //CHAZ CODE Clear field for next transaction
            userinput = (EditText)findViewById(amountfield);
            userinput.setText("");
        }
    }
    //REFUND BUTTON
    public void refundButtonClicked() {
        MposUi ui = MposUi.initialize(this, ProviderMode.MOCK,
                "merchantIdentifier", "merchantSecretKey");

        ui.getConfiguration().setSummaryFeatures(EnumSet.of(
                // Add this line, if you do want to offer printed receipts
                // MposUiConfiguration.SummaryFeature.PRINT_RECEIPT,
                MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL)
        );

        // Start with a mocked card reader:
        AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MOCK)
                .mocked()
                .build();
        ui.getConfiguration().setTerminalParameters(accessoryParameters);

        // Add this line if you would like to collect the customer signature on the receipt (as opposed to the digital signature)
        // ui.getConfiguration().setSignatureCapture(MposUiConfiguration.SignatureCapture.ON_RECEIPT);



        TransactionParameters transactionParameters = new TransactionParameters.Builder()
                .refund("<transactionIdentifer>")
                // For partial refunds, specify the amount to be refunded
                // and the currency from the original transaction
                .amountAndCurrency(refundamount, io.mpos.transactions.Currency.USD)
                .build();
        Intent intent = ui.createTransactionIntent(transactionParameters);
        startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
        //CHAZ CODE null out the refund amount
        refundamount=null;
    }
}

