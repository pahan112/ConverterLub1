package papka.pahan.converterlub.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import papka.pahan.converterlub.R;
import papka.pahan.converterlub.adapter.BankAdapter;
import papka.pahan.converterlub.db.ModelDataBaseBank;
import papka.pahan.converterlub.service.BankService;
import papka.pahan.converterlub.tools.PreferenceManager;

public class MainActivity extends AppCompatActivity implements BankAdapter.OnClickBankItemListener {

    @BindView(R.id.rv_bank_list)
    RecyclerView mRecyclerViewBank;
    @BindView(R.id.sv_bank)
    SearchView mSearchViewBank;
    @BindView(R.id.sr_bank)
    SwipeRefreshLayout mBankSwipeRefreshLayout;
    @BindView(R.id.pb_bank)
    ProgressBar mBankProgressBar;

    private List<ModelDataBaseBank> mModelDataBaseBanks = new ArrayList<>();
    private List<ModelDataBaseBank> mModelDataBaseBanksSearch = new ArrayList<>();
    private BankAdapter mBankAdapter;

    private ResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBankAdapter = new BankAdapter(mModelDataBaseBanks, this);
        mRecyclerViewBank.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewBank.setAdapter(mBankAdapter);

        mBankProgressBar.setVisibility(View.VISIBLE);
        initSwipeRefreshLayout();
        initSearch();

        Intent intent = new Intent(this, BankService.class);
        resultReceiveBank();
        intent.putExtra("receiver", mReceiver);
        startService(intent);
    }

    public void resultReceiveBank() {
        mReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 200) {
                    mBankProgressBar.setVisibility(View.INVISIBLE);
                    mModelDataBaseBanks.clear();
                    mModelDataBaseBanks.addAll(new Select().from(ModelDataBaseBank.class).queryList());
                    mBankAdapter.notifyDataSetChanged();
                    if(PreferenceManager.loadStringParam(getBaseContext(), PreferenceManager.PARAM_LAST_UPDATE).isEmpty()){
                        Toast.makeText(getBaseContext(),"Проверьте соеденение с интернетом",Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    private void initSwipeRefreshLayout() {
        mBankSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resultReceiveBank();
                mBankSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onClickPhone(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    @Override
    public void onClicksLink(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }

    @Override
    public void onClickSetting(ModelDataBaseBank modelDataBaseBank) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.BANK_DETAILS, modelDataBaseBank);
        startActivity(intent);
    }

    @Override
    public void onClickMap(ModelDataBaseBank modelDataBaseBank) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(MapActivity.BANK_MAP, modelDataBaseBank);
        startActivity(intent);
    }

    private void initSearch() {
        ((EditText) mSearchViewBank.findViewById(R.id.search_src_text)).setTextColor(Color.WHITE);
        mSearchViewBank.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    mBankAdapter.setBankList(mModelDataBaseBanks);
                } else {
                    mModelDataBaseBanksSearch.clear();
                    for (ModelDataBaseBank bank : mModelDataBaseBanks) {
                        if (bank.getTitleDb().toLowerCase().contains(newText.toLowerCase()) || bank.getCityIdDb().toLowerCase().contains(newText.toLowerCase())) {
                            mModelDataBaseBanksSearch.add(bank);
                        }
                        mBankAdapter.setBankList(mModelDataBaseBanksSearch);
                    }

                }
                return false;
            }
        });
    }
}
