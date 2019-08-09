package com.android.wcf.base;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.wcf.model.Participant;
import com.android.wcf.model.Team;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
abstract public class BaseFragment extends Fragment implements BaseMvp.BaseView {

    private BaseMvp.BaseView baseView;

    public BaseFragment() {
    }

    //TODO: implement the proper dialogFragment for showing error messages

    @Override
    public void showError(int messageId) {
        Toast.makeText(getContext(), getString(messageId) , Toast.LENGTH_SHORT).show();
    }


    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(Throwable error) {
        showMessage(error.getMessage());
    }

    @Override
    public void showError(String message) {
        showMessage(message);
    }

    @Override
    public void showError(String title, String message) {
        showMessage(message);
    }

    @Override
    public void showError(String title, int messageId) {
        Toast.makeText(getContext(), getString(messageId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(int titleId, String message) {
        showMessage(message);
    }

    @Override
    public void showError(int titleId, int messageId) {
        Toast.makeText(getContext(), getString(messageId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoadingDialogFragment() {

    }

    @Override
    public void hideLoadingDialogFragment() {

    }

    @Override
    public View showLoadingView() {
        return null;
    }

    @Override
    public void hideLoadingView() {

    }

    @Override
    public void setParticipant(Participant participant) {
        baseView.setParticipant(participant);
    }

    @Override
    public Participant getParticipant() {
        return baseView.getParticipant();
    }

    @Override
    public void setParticipantTeam(Team team) {
        baseView.setParticipantTeam(team);
    }

    @Override
    public Team getParticipantTeam() {
        return baseView.getParticipantTeam();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        baseView = (BaseMvp.BaseView) context;
    }

    @Override
    public void onStop() {
        super.onStop();

//        View view = view = getView();
//        if (view != null) {
//            view.clearFocus();
//        }
    }

    @Override
    public void closeKeyboard() {
        View view = view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
        }
    }
}
