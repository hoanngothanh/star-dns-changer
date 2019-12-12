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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import com.hololo.app.dnschanger.R;
import com.hololo.app.dnschanger.model.DNSModel;

public class CardPresenter extends Presenter {

    private static int CARD_WIDTH = 200;
    private static int CARD_HEIGHT = 200;

    private static Context mContext;

    static class ViewHolder extends Presenter.ViewHolder {

        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;
//        private PicassoImageCardViewTarget mImageCardViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
//            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);
            mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.ic_launcher);
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

//        protected void updateCardViewImage(String url) {
//
//            Picasso.with(mContext)
//                    .load(url)
//                    .resize(CARD_WIDTH, CARD_HEIGHT)
//                    .centerCrop()
//                    .error(mDefaultCardImage)
//                    .into(mImageCardViewTarget);
//
//        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {

        Log.d("onCreateViewHolder", "creating viewholder");
        mContext = viewGroup.getContext();
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        ((TextView) cardView.findViewById(R.id.content_text)).setTextColor(Color.LTGRAY);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object o) {
        DNSModel dnsModel = (DNSModel) o;
        ((ViewHolder) viewHolder).mCardView.setTitleText(dnsModel.getName());
        ((ViewHolder) viewHolder).mCardView.setContentText(dnsModel.getFirstDns());
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH * 2, CARD_HEIGHT * 2);
//        ((ViewHolder) viewHolder).updateCardViewImage(dnsModel.getThumbUrl());
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

//    public static class PicassoImageCardViewTarget implements Target {
//        private ImageCardView mImageCardView;
//
//        public PicassoImageCardViewTarget(ImageCardView mImageCardView) {
//            this.mImageCardView = mImageCardView;
//        }
//
//        @Override
//        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
//            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
//            mImageCardView.setMainImage(bitmapDrawable);
//        }
//
//        @Override
//        public void onBitmapFailed(Drawable drawable) {
//            mImageCardView.setMainImage(drawable);
//        }
//
//        @Override
//        public void onPrepareLoad(Drawable drawable) {
//            // Do nothing, default_background manager has its own transitions
//        }
//    }

}
