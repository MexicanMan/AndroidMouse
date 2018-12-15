package com.example.sombrero.bluem.Activities;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.widget.Toast;

import com.example.sombrero.bluem.MouseViewModel;
import com.example.sombrero.bluem.R;
import com.example.sombrero.bluem.databinding.ActivityMouseBinding;

public class MouseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMouseBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_mouse);
        MouseViewModel mouseViewModel = ViewModelProviders.of(this).get(MouseViewModel.class);
        binding.setDataContext(mouseViewModel);
        binding.setLifecycleOwner(this);
        getLifecycle().addObserver(mouseViewModel);

        mouseViewModel.getToastMessage().observe(this, toastMessage -> {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        });
    }

}
