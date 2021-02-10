package com.example.chronopassbluetoothterminal.view.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.controller.SettingsController;

public class SettingsFragment extends Fragment {

    private SettingsController objCC;

    public RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        init(root);

        return root;
    }

    private void init(View root) {
        this.recyclerView = root.findViewById(R.id.rv_command_config);

        this.objCC = new SettingsController(this, root.getContext());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}