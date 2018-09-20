package com.example.sombrero.bluem.Utils;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyMutableLiveData<T> extends MutableLiveData<T> {

    @NonNull
    private final AtomicBoolean delivered = new AtomicBoolean(false);

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        super.observe(owner, value -> {
            if (delivered.compareAndSet(true, false)) {
                observer.onChanged(value);
            }
        });

    }

    @Override
    public void setValue(@Nullable T value) {
        delivered.set(true);
        super.setValue(value);

    }
}
