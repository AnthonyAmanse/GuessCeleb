package com.example.ghiea.guessceleb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class MainActivity extends AppCompatActivity {
//ca-app-pub-7028296694914621/9103296199
    TextView scoreText;
    TextView resultText;
    ImageView celebImage;

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button[] myOptions;
    View[] myViews;

    TextView statusText;
    TextView subStatusText;

    String webData;

    HashMap<String, String> celebMap;
    ArrayList<String> celebNames;
    Random random;
    int index;

    int indexOfButton;

    int totalCorrect;
    int totalAttempts;
    ImageDownloader taskImage;

    private AdView mAdView;

    public Button getButtonView(int id) {return (Button) findViewById(id);}
    public TextView getTextView(int id) {return (TextView) findViewById(id);}
    public ImageView getImageView(int id) {return (ImageView) findViewById(id);}

    public void newQuestion() {
        index = random.nextInt(100);
        Bitmap imageOfCeleb = null;
        taskImage = new ImageDownloader();
        taskImage.execute(celebMap.get(celebNames.get(index)));

        celebImage.setImageBitmap(imageOfCeleb);
        indexOfButton = random.nextInt(4);
        for(Button b: myOptions) {
            int randomChoices = random.nextInt(100);
            do {
                b.setText(celebNames.get(randomChoices));
            } while (randomChoices == index);
        }
        myOptions[indexOfButton].setText(celebNames.get(index));
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();

                while(data != -1) {
                    char current = (char) data;
                    result += current;

                    data = inputStreamReader.read();
                }

                return result;

            }
            catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }


        }

        @Override
        protected void onPostExecute(String result) {
            webData = result;
            Pattern p = Pattern.compile("<div class=\"image\">\n" +
                    "\t\t\t\t\t\t<img src=\"(.*?)\" alt=\"(.*?)\"/>");
            Matcher m = p.matcher(webData);
            while (m.find()) {
                System.out.println(m.group(1) + " - " + m.group(2));
                celebMap.put(m.group(2),m.group(1));
                celebNames.add(m.group(2));
            }
            newQuestion();
            statusText.setVisibility(View.INVISIBLE);
            subStatusText.setVisibility(View.INVISIBLE);
            for(View v: myViews) {
                v.setVisibility(View.VISIBLE);
            }


        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            celebImage.setImageBitmap(result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7028296694914621~7626562999");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        scoreText = getTextView(R.id.scoreText);
        resultText = getTextView(R.id.resultText);
        celebImage = getImageView(R.id.celebImage);
        button1 = getButtonView(R.id.button1);
        button2 = getButtonView(R.id.button2);
        button3 = getButtonView(R.id.button3);
        button4 = getButtonView(R.id.button4);
        myOptions = new Button[] {button1, button2, button3, button4};
        myViews = new View[] {button1, button2, button3, button4, scoreText, celebImage};

        statusText = getTextView(R.id.statusText);
        subStatusText = getTextView(R.id.subStatusText);

        for(View v: myViews) {
            v.setVisibility(View.INVISIBLE);
        }
        celebMap = new HashMap<>();
        celebNames = new ArrayList<>();

        DownloadTask task = new DownloadTask();

        task.execute("http://www.posh24.com/celebrities");
        random = new Random();
        index = random.nextInt(100);
        indexOfButton = random.nextInt(4);

        totalAttempts = 0;
        totalCorrect = 0;
        resultText.setText("");


    }

    public void chooseAnswer(View view) {
        Button button = (Button) view;
//        try {
//            System.out.println(celebNames.get(index));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            if (celebNames.get(index).equals(button.getText())) {
                totalCorrect++;
                totalAttempts++;
                resultText.setText("Correct!");
            } else {
                totalAttempts++;
                resultText.setText("Wrong. Right answer was: " + celebNames.get(index));
            }
            scoreText.setText(totalCorrect + "/" + totalAttempts);

            taskImage.cancel(true);
            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
