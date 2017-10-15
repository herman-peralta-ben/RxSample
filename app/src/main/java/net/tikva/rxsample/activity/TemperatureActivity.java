package net.tikva.rxsample.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.tikva.rxsample.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by herman on 14/10/17.
 */

public class TemperatureActivity extends AppCompatActivity {
    @BindView(R.id.text_received_temperature)
    TextView textReceivedTemperature;
    @BindView(R.id.text_emitted_temperature)
    TextView textEmittedTemperature;
    private Observable<Float> tempSensorObservable;
    private Observer<Float> temperatureObserver = new Observer<Float>() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {

        }

        @Override
        public void onNext(@NonNull Float temp) {
            Log.e("HERMAN", "got temp " + temp);
            textReceivedTemperature.setText(String.valueOf(temp));
        }

        @Override
        public void onError(@NonNull Throwable e) {

        }

        @Override
        public void onComplete() {
            Toast.makeText(TemperatureActivity.this, "No more temperatures", Toast.LENGTH_LONG).show();
        }
    };

    public static Intent getStartIntent(Context from) {
        return new Intent(from, TemperatureActivity.class);
    }

    private static Float getRandomFloat(Random rand, float min, float max) {
        return rand.nextFloat() * (max - min) + min;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        createTimerObservable(100);

        /*
        tempSensorObservable
                //.sample(5, TimeUnit.SECONDS)
                .subscribeWith(temperatureObserver);
        */
    }

    private void createTimerObservable(final int tempItemsCount) {

        List<Float> temperatures = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < tempItemsCount; i++) {
            final float temp = getRandomFloat(rand, 10.5f, 50.5f);
            temperatures.add(temp);
            Log.e("HERMAN", "" + temp);
        }

        // Makes a observable which emits an item each second
        tempSensorObservable = Observable.zip(
                Observable.interval(0, 1, TimeUnit.SECONDS),
                Observable.fromIterable(temperatures),
                (aLong, temp) -> temp)
                .doOnNext(currentTemp -> textEmittedTemperature.setText(String.valueOf(currentTemp)))
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        Log.e("HERMAN", "start");
                    }
                });
    }
}
