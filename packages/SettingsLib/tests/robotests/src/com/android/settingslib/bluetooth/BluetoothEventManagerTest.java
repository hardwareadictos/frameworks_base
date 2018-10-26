/*
 * Copyright 2018 The Android Open Source Project
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
 * limitations under the License.
 */
package com.android.settingslib.bluetooth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.telephony.TelephonyManager;

import com.android.settingslib.SettingsLibRobolectricTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

@RunWith(SettingsLibRobolectricTestRunner.class)
public class BluetoothEventManagerTest {

    @Mock
    private LocalBluetoothAdapter mLocalAdapter;
    @Mock
    private CachedBluetoothDeviceManager mCachedDeviceManager;
    @Mock
    private BluetoothCallback mBluetoothCallback;
    @Mock
    private CachedBluetoothDevice mCachedBluetoothDevice;

    private Context mContext;
    private Intent mIntent;
    private BluetoothEventManager mBluetoothEventManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = RuntimeEnvironment.application;

        mBluetoothEventManager = new BluetoothEventManager(mLocalAdapter,
                mCachedDeviceManager, mContext, /* handler= */ null, /* userHandle= */ null);
    }

    @Test
    public void ifUserHandleIsNull_registerReceiverIsCalled() {
        Context mockContext = mock(Context.class);
        BluetoothEventManager eventManager =
                new BluetoothEventManager(mLocalAdapter, mCachedDeviceManager, mockContext,
                        /* handler= */ null, /* userHandle= */ null);

        verify(mockContext).registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class),
                eq(null), eq(null));
    }

    @Test
    public void ifUserHandleSpecified_registerReceiverAsUserIsCalled() {
        Context mockContext = mock(Context.class);
        BluetoothEventManager eventManager =
                new BluetoothEventManager(mLocalAdapter, mCachedDeviceManager, mockContext,
                        /* handler= */ null, UserHandle.ALL);

        verify(mockContext).registerReceiverAsUser(any(BroadcastReceiver.class), eq(UserHandle.ALL),
                any(IntentFilter.class), eq(null), eq(null));
    }

    /**
     * Intent ACTION_AUDIO_STATE_CHANGED should dispatch to callback.
     */
    @Test
    public void intentWithExtraState_audioStateChangedShouldDispatchToRegisterCallback() {
        mBluetoothEventManager.registerCallback(mBluetoothCallback);
        mIntent = new Intent(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);

        mContext.sendBroadcast(mIntent);

        verify(mBluetoothCallback).onAudioModeChanged();
    }

    /**
     * Intent ACTION_PHONE_STATE_CHANGED should dispatch to callback.
     */
    @Test
    public void intentWithExtraState_phoneStateChangedShouldDispatchToRegisterCallback() {
        mBluetoothEventManager.registerCallback(mBluetoothCallback);
        mIntent = new Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED);

        mContext.sendBroadcast(mIntent);

        verify(mBluetoothCallback).onAudioModeChanged();
    }

    /**
     * dispatchProfileConnectionStateChanged should dispatch to onProfileConnectionStateChanged
     * callback.
     */
    @Test
    public void dispatchProfileConnectionStateChanged_registerCallback_shouldDispatchCallback() {
        mBluetoothEventManager.registerCallback(mBluetoothCallback);

        mBluetoothEventManager.dispatchProfileConnectionStateChanged(mCachedBluetoothDevice,
                BluetoothProfile.STATE_CONNECTED, BluetoothProfile.A2DP);

        verify(mBluetoothCallback).onProfileConnectionStateChanged(mCachedBluetoothDevice,
                BluetoothProfile.STATE_CONNECTED, BluetoothProfile.A2DP);
    }
}
