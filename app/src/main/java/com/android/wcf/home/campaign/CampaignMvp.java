package com.android.wcf.home.campaign;

import com.android.wcf.base.BaseMvp;
import com.android.wcf.model.Event;
import com.android.wcf.model.Team;

import java.util.List;

public interface CampaignMvp {
    interface CampaignView extends BaseMvp.BaseView {

        void hideJourneyBeforeStartCard();
        void showJourneyBeforeStartCard(Event event);

        void hideJourneyDetails();
        void showJourneyDetails(Event event);

        void hideCreateOrJoinTeamCard();
        void showCreateOrJoinTeamCard();

        void onFacebookIdMissing();

        void showCreateNewTeamView();


        void showMyTeamCard(Team team);
        void hideTeamCard();

        void hideCreateNewTeamView();

        void teamCreated(Team team);

        void showTeamList(List<Team> teams);

        void enableShowCreateTeam(boolean enabledFlag);

        void enableJoinExistingTeam(boolean enabledFlag);

        void participantJoinedTeam(String fbid, int teamId);

    }

    interface Presenter extends BaseMvp.Presenter {
        void getEvent(int eventId);

        void createTeam(String teamName);

        void getTeams();

        void getTeam(int teamId);

        void getTeamStats(int teamId);

        void deleteTeam(int teamId);

        void createParticipant(String fbid);

        void getParticipant(String fbid);

        void assignParticipantToTeam(String fbid, int teamId);

        void getParticipantStats(String fbid);

        void deleteParticipant(String fbid);

        void showCreateTeamClick();

        void showTeamsToJoinClick();

        void createTeamClick(String teamName);

        void cancelCreateTeamClick();
    }
}