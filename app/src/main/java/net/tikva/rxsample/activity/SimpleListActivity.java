package net.tikva.rxsample.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import net.tikva.rxsample.R;
import net.tikva.rxsample.adapter.SingleStringAdapter;
import net.tikva.rxsample.entities.AppInfo;
import net.tikva.rxsample.entities.AppInfoRich;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * https://www.youtube.com/watch?v=HHylV_OiILY&t=1277s
 * http://blog.danlew.net/2015/07/23/deferring-observable-code-until-subscription-in-rxjava/
 * http://www.hermosaprogramacion.com/2015/03/android-swiperefreshlayout-recyclerview/
 */
public class SimpleListActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener, SingleStringAdapter.ListClickListener {

    private static final int SAMPLE_COUNT = 12;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.recycler_swipe_refresh)
    SwipeRefreshLayout recyclerSwipeRefresh;
    SingleStringAdapter adapter;
    final Observer<AppInfo> itemObserver = new Observer<AppInfo>() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {
            adapter.clear();
        }

        @Override
        public void onNext(@NonNull AppInfo appInfo) {
            adapter.addItem(appInfo.name);
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            recyclerSwipeRefresh.setRefreshing(false);
            Toast.makeText(SimpleListActivity.this, "ERROR", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
            recyclerSwipeRefresh.setRefreshing(false);
        }
    };
    final Observer<List<AppInfo>> paginatedItemsObserver = new Observer<List<AppInfo>>() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {
            adapter.clear();
        }

        @Override
        public void onNext(@NonNull List<AppInfo> appBuffer) {
            Log.e("HERMAN", "got " + appBuffer.size());
            for (AppInfo appInfo : appBuffer) {
                adapter.addItem(appInfo.name);
            }
            adapter.notifyItemRangeInserted(adapter.getItemCount() - appBuffer.size(), appBuffer.size());
        }

        @Override
        public void onError(@NonNull Throwable e) {
            recyclerSwipeRefresh.setRefreshing(false);
            Toast.makeText(SimpleListActivity.this, "ERROR", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
            recyclerSwipeRefresh.setRefreshing(false);
        }
    };
    private int sample = SAMPLE_COUNT;

    public static Intent getStartIntent(Context from) {
        return new Intent(from, SimpleListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        ButterKnife.bind(this);

        adapter = new SingleStringAdapter(R.layout.item_sample, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerSwipeRefresh.setOnRefreshListener(this);

        runSample(sample);

        Toast.makeText(SimpleListActivity.this, "Swipe to change sample", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        if (++sample > SAMPLE_COUNT) {
            sample = 1;
        }
        runSample(sample);
    }

    @Override
    public void onItemClicked(int position) {

    }

    private void runSample(final int sample) {
        List<AppInfo> appInfos = getAppInfoList();
        switch (sample) {
            case 1:
                sample_getApps();
                break;

            case 2:
                Observable<AppInfo> observable = sample_getSimpleList(appInfos);
                observable.subscribeWith(itemObserver);
                break;

            case 3:
                sample_only3Items(appInfos);
                break;

            case 4:
                sample_rxGetAppInfoListDefer();
                break;

            case 5:
                sample_rxGetAppInfoListCallable();
                break;

            case 6:
                sample_filterStartsWith(appInfos, "2");
                break;

            case 7:
                sample_takeFirst(appInfos, 3);
                break;

            case 8:
                sample_takeLast(appInfos, 3);
                break;

            case 9:
                sample_removeDuplicates(appInfos);
                break;

            case 10:
                sample_mapToLowercase(appInfos);
                break;

            case 11:
                sample_bufferPaginate(appInfos, 3);
                break;

            case 12:
                List<AppInfo> rev = new ArrayList<>();
                for (AppInfo appInfo : appInfos) {
                    rev.add(0, appInfo);
                }
                // 0...99, 99, ..., 0
                sample_merge(appInfos, rev);
                break;
        }

        Toast.makeText(SimpleListActivity.this, "Sample " + sample, Toast.LENGTH_SHORT).show();
    }

    /**
     * Sample 1: Use default observable and emit a list, item by item
     *
     * @return
     */
    private void sample_getApps() {
        Observable<AppInfo> observable = Observable.create(new ObservableOnSubscribe<AppInfo>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<AppInfo> e) throws Exception {
                List<AppInfoRich> apps = getAppInfoRichList();
                for (AppInfoRich appInfoRich : apps) {
                    AppInfo app = new AppInfo();
                    app.name = appInfoRich.name;
                    e.onNext(app);
                }
                e.onComplete();
            }
        });

        observable.subscribeWith(itemObserver);
    }

    /**
     * Sample 2: Use Observable.fromIterable()/fromArray() to avoid manually emit
     * each item (as Sample 1)
     *
     * @return Observable
     */

    private Observable<AppInfo> sample_getSimpleList(List<AppInfo> appsList) {
        Observable<AppInfo> observable =
                Observable.fromIterable(appsList); //fromArray for [], fromIterable for collections

        //observable.subscribeWith(itemObserver);

        return observable;
    }


    /**
     * Sample 3: Emit only 3 items of the list using
     * <p>
     * Observable.just()
     *
     * @param appsList
     */
    private void sample_only3Items(@NonNull List<AppInfo> appsList) {
        Observable<AppInfo> observable =
                Observable.just(appsList.get(0), appsList.get(1), appsList.get(2));

        observable.subscribeWith(itemObserver);
    }

    /**
     * Sample 4: Create observable from "legacy" method
     * using Observable.defer() and Observable.just(legacyMethod());
     * <p>
     * None of the code inside of defer() is executed until subscription.
     * We only call Observable.just() when someone requests the data.
     */
    private void sample_rxGetAppInfoListDefer() {
        Observable<List<AppInfo>> listObservable =
                Observable.defer(() -> Observable.just(getAppInfoList()));

        // Now, show the data
        listObservable.subscribeWith(new Observer<List<AppInfo>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull List<AppInfo> appInfos) {
                Observable<AppInfo> observable = sample_getSimpleList(appInfos);
                observable.subscribeWith(itemObserver);
                onComplete();
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * Sample 5: Same as Sample 4, but using
     * Observable.fromCallable()
     */
    private void sample_rxGetAppInfoListCallable() {
        // This makes a rx version of the getAppInfoList() method
        Observable<List<AppInfo>> listObservable =
                Observable.fromCallable(this::getAppInfoList);

        // Now, show the data
        listObservable.subscribeWith(new Observer<List<AppInfo>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull List<AppInfo> appInfos) {
                Observable<AppInfo> observable = sample_getSimpleList(appInfos);
                observable.subscribeWith(itemObserver);
                onComplete();
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * Sample 6: filter the full list (see Sample 2)
     * Observable.filter();
     */
    private void sample_filterStartsWith(@NonNull List<AppInfo> appsList, final String startsWith) {
        sample_getSimpleList(appsList)
                .filter((appInfo) -> appInfo.name.startsWith(startsWith)) // apply filter operator ON OBSERVER
                .subscribeWith(itemObserver);
    }

    /**
     * Sample 7: use take operator to grab only the first n emitted items.
     * <p>
     * Observer.take(n)
     *
     * @param appsList
     * @param count
     */
    private void sample_takeFirst(@NonNull List<AppInfo> appsList, final int count) {
        sample_getSimpleList(appsList)
                .take(count) // take operator
                .subscribeWith(itemObserver);
    }

    /**
     * Sample 8: Same as Sample 7, but take last n emitted elements
     * <p>
     * Observable.take(n)
     *
     * @param appsList
     * @param count
     */
    private void sample_takeLast(@NonNull List<AppInfo> appsList, final int count) {
        sample_getSimpleList(appsList)
                .takeLast(count) // take operator
                .subscribeWith(itemObserver);
    }

    /**
     * Sample 9: Remove duplicates
     * <p>
     * Observable.distinct() operator
     */
    private void sample_removeDuplicates(@NonNull List<AppInfo> appsList) {
        sample_getSimpleList(appsList)
                .take(3)
                .repeat(3)
                // making duplicates on purpose
                .distinct() // removing duplicates
                .subscribeWith(itemObserver);
    }

    /**
     * Sample 10: Make lowercase items
     *
     * @return
     */
    private void sample_mapToLowercase(@NonNull List<AppInfo> appsList) {
        sample_getSimpleList(appsList)
                //Map returns object, if returns distinct that AppInfo, will create a new sequence and will need a new observer
                .map((AppInfo appInfo) -> {
                    appInfo.name = appInfo.name.toLowerCase();
                    return appInfo;
                })
                .subscribeWith(itemObserver);
    }

    /**
     * Sample 11: Paginate using buffer(n), Instead of items, the observable will emit
     * lists of n items (each time it gets n items).
     * <p>
     * If the list ends and buffer has < n items, it sends those items
     * <p>
     * e.g. use bufferSize = 3 and items = 100 and the last emitted buffer will have 1 item
     *
     * @param appsList
     * @param bufferSize
     */
    private void sample_bufferPaginate(@NonNull List<AppInfo> appsList, final int bufferSize) {
        sample_getSimpleList(appsList)
                .buffer(bufferSize)
                .subscribeWith(paginatedItemsObserver);
    }

    /**
     * Sample 12: Merge lists (concat lists)
     */
    private void sample_merge(@NonNull List<AppInfo> appsList1, @NonNull List<AppInfo> appsList2) {
        Observable
                .merge(sample_getSimpleList(appsList1), sample_getSimpleList(appsList2))
                .subscribeWith(itemObserver);
    }

    private List<AppInfoRich> getAppInfoRichList() {
        List<AppInfoRich> apps = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            AppInfoRich appInfo = new AppInfoRich();
            appInfo.name = String.format("%d APP", i);
            apps.add(appInfo);
        }

        return apps;
    }

    /**
     * Map AppInfoRich to AppInfo
     */
    private List<AppInfo> getAppInfoList() {
        List<AppInfoRich> appsR = getAppInfoRichList();
        List<AppInfo> apps = new ArrayList<>();

        for (AppInfoRich appInfoRich : appsR) {
            AppInfo appInfo = new AppInfo();
            appInfo.name = appInfoRich.name;
            apps.add(appInfo);
        }

        return apps;
    }
}
