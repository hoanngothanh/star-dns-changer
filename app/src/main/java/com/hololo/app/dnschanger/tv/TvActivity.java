package com.hololo.app.dnschanger.tv;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.hololo.app.dnschanger.DNSChangerApp;
import com.hololo.app.dnschanger.R;
import com.hololo.app.dnschanger.dnschanger.DNSModule;
import com.hololo.app.dnschanger.dnschanger.DaggerDNSComponent;
import com.hololo.app.dnschanger.dnschanger.IDNSView;
import com.hololo.app.dnschanger.model.DNSModel;

public class TvActivity extends FragmentActivity implements IDNSView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
        DaggerDNSComponent.builder().applicationComponent(DNSChangerApp.getApplicationComponent())
                .dNSModule(new DNSModule(this)).build().inject(this);
    }

    @Override
    public void changeStatus(int serviceStatus) {

    }

    @Override
    public void setServiceInfo(DNSModel model) {

    }
}
