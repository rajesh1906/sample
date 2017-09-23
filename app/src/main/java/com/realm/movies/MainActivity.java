package com.realm.movies;

import android.lib.recaptcha.ReCaptcha;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ReCaptcha.OnShowChallengeListener, ReCaptcha.OnVerifyAnswerListener {

    private static final String PUBLIC_KEY  = "6LfmyDEUAAAAAJWo_glXU8ANXggWfjNY6lqdYwis";
    private static final String PRIVATE_KEY = "6LfmyDEUAAAAALq9kDM9m95k88U3MAuSKcbQEjE8";

    private ReCaptcha reCaptcha;
    private ProgressBar progress;
    private EditText answer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.reCaptcha = (ReCaptcha)this.findViewById(R.id.recaptcha);
        this.progress  = (ProgressBar)this.findViewById(R.id.progress);
        this.answer    = (EditText)this.findViewById(R.id.answer);

        this.findViewById(R.id.verify).setOnClickListener(this);
        this.findViewById(R.id.reload).setOnClickListener(this);

        this.showChallenge();



        Map<Integer,SamplePojo> map=new HashMap<>();
        SamplePojo d1= new SamplePojo(1,"raaz","kasimkota",40,true);
        SamplePojo d2=  new SamplePojo(2,"kuamr","kasimkota",40,true);
        SamplePojo d3= new SamplePojo(3,"rajesh","kasimkota",40,true);
        SamplePojo d4= new SamplePojo(4,"rajesh kumar","kasimkota",40,true);
        map.put(d1.getKey(), d1);
        map.put(d2.getKey(), d2);
        map.put(d3.getKey(), d3);
        map.put(d4.getKey(), d4);
        Set<Integer> keySet= map.keySet();
        for(int i:keySet){
            for(String key:map.get(i).getValues().keySet()) {
                Log.e(" map values is", "<key is >>" + key + " value is " + map.get(i).getValues().get(key));
            }
            Log.e("...............","................................");
        }
    }


    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.verify:
                this.verifyAnswer();

                break;

            case R.id.reload:
                this.showChallenge();

                break;
        }
    }

    @Override
    public void onChallengeShown(final boolean shown) {
        this.progress.setVisibility(View.GONE);

        if (shown) {
            // If a CAPTCHA is shown successfully, displays it for the user to enter the words
            this.reCaptcha.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.show_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAnswerVerified(final boolean success) {
        if (success) {
            Toast.makeText(this, R.string.verification_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.verification_failed, Toast.LENGTH_SHORT).show();
        }

        // (Optional) Shows the next CAPTCHA
        this.showChallenge();
    }

    private void showChallenge() {
        // Displays a progress bar while downloading CAPTCHA
        this.progress.setVisibility(View.VISIBLE);
        this.reCaptcha.setVisibility(View.GONE);

        this.reCaptcha.setLanguageCode("en");
        this.reCaptcha.showChallengeAsync(MainActivity.PUBLIC_KEY, this);
    }

    private void verifyAnswer() {
        if (TextUtils.isEmpty(this.answer.getText())) {
            Toast.makeText(this, R.string.instruction, Toast.LENGTH_SHORT).show();
        } else {
            // Displays a progress bar while submitting the answer for verification
            this.progress.setVisibility(View.VISIBLE);
            this.reCaptcha.verifyAnswerAsync(MainActivity.PRIVATE_KEY, this.answer.getText().toString(), this);
        }
    }

    public  <T> String nameOf(T object) {
        return object.getClass().getSimpleName();
    }
}
