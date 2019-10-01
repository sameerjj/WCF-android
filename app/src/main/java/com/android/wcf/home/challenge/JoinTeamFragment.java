package com.android.wcf.home.challenge;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.wcf.R;
import com.android.wcf.base.BaseFragment;
import com.android.wcf.helper.SharedPreferencesUtil;
import com.android.wcf.helper.view.ListPaddingDecoration;
import com.android.wcf.model.Event;
import com.android.wcf.model.Team;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JoinTeamFragment  extends BaseFragment implements JoinTeamMvp.View, JoinTeamAdapterMvp.Host {

    private static final String TAG = JoinTeamFragment.class.getSimpleName();
    JoinTeamMvp.Presenter presenter;
    JoinTeamMvp.Host host;

    List<Team> teams = new ArrayList<>();

    private Button joinTeamButton = null;
    private RecyclerView teamsListRecyclerView = null;
    private JoinTeamAdapter teamsAdapter = null;
    private String participantId;
    private Team selectedTeam = null;
    private TextInputLayout teamNameInputLayout = null;
    private TextInputEditText teamNameEditText = null;

    private TextWatcher searchTeamEditWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            searchTeamToJoin();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.join_team_button:
                    teamSelectedToJoin();
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof JoinTeamMvp.Host) {
            host = (JoinTeamMvp.Host) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement JoinTeamMvp.Host");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        host = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        participantId = SharedPreferencesUtil.getMyParticipantId();
        presenter = new JoinTeamPresenter(this);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_join_team, container, false);

        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        host.setToolbarTitle(getString(R.string.join_team_title), true);
        setupView(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (!handled) {
            switch (item.getItemId()) {
                case android.R.id.home:
                     closeView();
                    handled = true;
                    break;
                default:
                    break;
            }
        }
        return handled;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshTeamList();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    void refreshTeamList() {
        teams = getTeamList();
        joinTeamButton.setEnabled((teams == null || teams.size() == 0) ? false : true);
        teamsAdapter.clearTeamSelectionPosition(); //TODO: if we have a team previously selected, find its position and select that
        teamsListRecyclerView.scrollToPosition(0);
        teamsAdapter.updateTeamsData(teams);
    }

    @Override
    public void teamRowSelected(int pos) {
        //TODO: enable selecting a team if it has capacity for additional members or show message
        joinTeamButton.setEnabled(pos >= 0);
    }

    void setupView(View joinTeamView) {
        Event event = getEvent();

        if (teamsAdapter == null) {
            teamsAdapter = new JoinTeamAdapter(this, ((Event) event).getTeamLimit());
        }

        teamsListRecyclerView = joinTeamView.findViewById(R.id.teams_list);
        teamsListRecyclerView.setLayoutManager(new LinearLayoutManager(joinTeamView.getContext()));
        teamsListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

        teamsListRecyclerView.addItemDecoration(new ListPaddingDecoration(getContext()));

        teamsListRecyclerView.setAdapter(teamsAdapter);

        joinTeamButton = joinTeamView.findViewById(R.id.join_team_button);

        if (joinTeamButton != null) {
            joinTeamButton.setOnClickListener(onClickListener);
            joinTeamButton.setEnabled(false); // will be enabled when a team is selected
        }

        teamNameEditText = joinTeamView.findViewById(R.id.team_name);
        if (teamNameEditText != null) {
            teamNameEditText.addTextChangedListener(searchTeamEditWatcher);
        }
        teamNameInputLayout = joinTeamView.findViewById(R.id.search_team_name_input_layout);

    }

    void closeView() {
        getActivity().onBackPressed();
    }

    private void teamSelectedToJoin() {
         selectedTeam = teamsAdapter.getSelectedTeam();
        if (selectedTeam == null) {
            showMessage("Please select a team to join");
            return;
        }

        //TODO ensure capacity, if not show message

        presenter.assignParticipantToTeam(participantId, selectedTeam.getId());
    }

    @Override
    public void participantJoinedTeam(@NotNull String participantId, int teamId) {
        SharedPreferencesUtil.saveMyTeamId(teamId);
        closeView();
    }

    @Override
    public void participantJoinTeamError(String participantId, int teamId) {
        showMessage(getString(R.string.participant_team_join_error));
    }

    protected void searchTeamToJoin() {
        String errorMessage = getString(R.string.search_team_name_not_found_error);
        Log.d(TAG, errorMessage);

        if (teamNameEditText != null) {
            String teamName = teamNameEditText.getText().toString().trim();

            if (teamName.length() >= 1) {
                int teamsCount =  (teams != null ? teams.size() : 0);
                int dataRow = -1;
                for (int idx = 0; idx < teamsCount; idx++ ) {
                    if (teams.get(idx).getName().toLowerCase().startsWith(teamName.toLowerCase())) {
                        dataRow = idx;
                        break;
                    }
                }
                if (dataRow >= 0) {
                    teamsListRecyclerView.smoothScrollToPosition(dataRow);
                    teamsListRecyclerView.setSelected(true);
                    teamNameInputLayout.setError(null);
                }
                else {
                    if (teamNameInputLayout != null) {
                        teamNameInputLayout.setError(errorMessage);
                    }
                }
            }
        }
    }
}