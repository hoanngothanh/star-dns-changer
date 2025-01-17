package com.hololo.app.dnschanger.dnschanger;

import com.hololo.app.dnschanger.di.component.ApplicationComponent;
import com.hololo.app.dnschanger.di.scope.ActivityScope;
import com.hololo.app.dnschanger.tv.TvActivity;

import dagger.Component;

@Component(modules = {DNSModule.class}, dependencies = {ApplicationComponent.class})
@ActivityScope
public interface DNSComponent {

    IDNSView view();

    void inject(MainActivity activity);

    void inject(TvActivity activity);
}
