package com.example.chronopassbluetoothterminal.view.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chronopassbluetoothterminal.R;
import com.example.chronopassbluetoothterminal.controller.CommandsController;

import org.jetbrains.annotations.NotNull;

public class CommandsFragment extends Fragment {

    private CommandsController objCC;

    public RecyclerView recyclerView;
    public LinearLayout linearLayoutNoCommandConfigured;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_commands, container, false);
        init(root);

        return root;
    }

    private void init(View root) {
        this.recyclerView = root.findViewById(R.id.rv_command_config);
        this.linearLayoutNoCommandConfigured = root.findViewById(R.id.ll_no_command_configured);

        this.objCC = new CommandsController(this, root.getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.ic_menu_scan);
        item.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ic_menu_add) {
            this.objCC.showConfigCommandDialog(false, null, -1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}