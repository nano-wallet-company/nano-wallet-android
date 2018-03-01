/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package co.nano.nanowallet.ui.send;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.github.ajalt.reprint.rxjava2.RxReprint;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentFingerprintBinding;
import co.nano.nanowallet.ui.common.BaseDialogFragment;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FingerprintDialogFragment extends BaseDialogFragment
        implements TextView.OnEditorActionListener {
    private FragmentFingerprintBinding binding;
    public static String TAG = FingerprintDialogFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_fingerprint, container, false);
        View view = binding.getRoot();

        RxReprint.authenticate()
                .subscribe(result -> {
                    switch (result.status) {
                        case SUCCESS:

                            break;
                        case NONFATAL_FAILURE:

                            break;
                        case FATAL_FAILURE:

                            break;
                    }
                });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {

            return true;
        }
        return false;
    }
}
