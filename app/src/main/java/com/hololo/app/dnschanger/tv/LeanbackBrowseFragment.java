/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hololo.app.dnschanger.tv;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.SinglePresenterSelector;

import com.google.gson.Gson;
import com.hololo.app.dnschanger.DNSChangerApp;
import com.hololo.app.dnschanger.R;
import com.hololo.app.dnschanger.di.component.DaggerApplicationComponent;
import com.hololo.app.dnschanger.di.module.ApplicationModule;
import com.hololo.app.dnschanger.dnschanger.IDNSView;
import com.hololo.app.dnschanger.model.DNSModel;
import com.hololo.app.dnschanger.model.DNSModelJSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeanbackBrowseFragment extends BrowseSupportFragment implements IDNSView {

    private ArrayObjectAdapter mRowsAdapter;

    @Inject
    Gson gson;

    private static final String[] HEADERS = new String[]{
            "Featured", "Popular", "Editor's choice"
    };
    private List<DNSModel> dnsList;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DaggerApplicationComponent.builder().applicationModule(
                new ApplicationModule((DNSChangerApp) getActivity().getApplication()))
                .build().inject(this);
        init();
    }

    public void init() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        setBrandColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        setBadgeDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_launcher));
        getDNSItems();
        for (int position = 0; position < HEADERS.length; position++) {
            ArrayObjectAdapter rowContents = new ArrayObjectAdapter((new SinglePresenterSelector(new CardPresenter())));

            rowContents.addAll(0, dnsList);
            HeaderItem headerItem = new HeaderItem(position, HEADERS[position]);
            mRowsAdapter.add(new ListRow(headerItem, rowContents));
        }
    }


    private CharSequence[] getDNSItems() {
        CharSequence[] result = new CharSequence[18];

        try {
            InputStream is = getActivity().getAssets().open("dns_servers.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            DNSModelJSON dnsModels = gson.fromJson(json, DNSModelJSON.class);
            dnsList = dnsModels.getModelList();
            int counter = 0;
            for (DNSModel dnsModel : dnsList) {
                result[counter] = (dnsModel.getName());
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void changeStatus(int serviceStatus) {

    }

    @Override
    public void setServiceInfo(DNSModel model) {

    }
}
