package com.android.wcf.home.challenge;

import com.android.wcf.model.Team;

import java.util.List;

public interface JoinTeamAdapterMvp {
    public interface View {
        void teamsDataUpdated();
        void teamRowSelected(int pos);

        void clearTeamSelectionPosition();

        void updateTeamsData(List<Team> teams);

        Team getSelectedTeam();
    }

    public interface Presenter {
        void updateTeamsData(List<Team> teams);
        int getTeamsCount();
        Team getTeam(int pos);
        void onTeamSelected(int pos);
        int getSelectedTeamPosition();
        void clearTeamSelectionPosition();
        Team getSelectedTeam();

        void teamRowSelected(int pos);
    }

    public interface Host {
        void teamRowSelected(int pos);
    }
}