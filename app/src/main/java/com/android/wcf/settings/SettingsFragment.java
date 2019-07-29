package com.android.wcf.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.wcf.R;
import com.android.wcf.base.BaseFragment;
import com.android.wcf.helper.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


public class SettingsFragment extends BaseFragment implements SettingsMvp.View {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    SettingsMvp.Host host;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.participant_miles_setting:
                    showMilesEditDialog();
                    break;
                case R.id.navigate_to_connect_app_or_device:
                    if (host != null) {
                        host.showDeviceConnection();
                    }
                case R.id.team_view_team_icon:
                    break;
                case R.id.btn_signout:
                    if (host != null) {
                        host.signout();
                    }
            }
        }
    };

    TextView particpantMiles;
    ImageView participantImage;
    TextView participantName;
    TextView teamName;
    TextView teamLeadLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings, container, false);

        particpantMiles = fragmentView.findViewById(R.id.participant_miles);
        fragmentView.findViewById(R.id.participant_miles_setting).setOnClickListener(onClickListener);
        participantImage = fragmentView.findViewById(R.id.participant_image);
        participantName = fragmentView.findViewById(R.id.participant_name);
        teamName = fragmentView.findViewById(R.id.team_name);
        teamLeadLabel = fragmentView.findViewById(R.id.teamlead_label);

        setupConnectDeviceClickListeners(fragmentView);
        setupTeamSettingsClickListeners(fragmentView);
        fragmentView.findViewById(R.id.btn_signout).setOnClickListener(onClickListener);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (host != null) {
            host.setToolbarTitle(getString(R.string.settings));
        }
        showParticipantInfo();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SettingsMvp.Host) {
            this.host = (SettingsMvp.Host) context;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showMilesEditDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_miles_entry, null);

        final EditText editText = dialogView.findViewById(R.id.participant_miles);
        Button saveBtn = dialogView.findViewById(R.id.save);
        Button cancelBtn = dialogView.findViewById(R.id.cancel);

        editText.setText(particpantMiles.getText());
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                particpantMiles.setText(editText.getText());
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    void showParticipantInfo() {

        String profileImageUrl = SharedPreferencesUtil.getUserFbProfileUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Log.d(TAG, "profileImageUrl=" + profileImageUrl);

            Glide.with(getContext())
                    .load(profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(participantImage);
        }

        participantName.setText(SharedPreferencesUtil.getUserFullName());
        teamLeadLabel.setVisibility(View.GONE);
    }

    void setupTeamSettingsClickListeners(View parentView) {

        View container = parentView.findViewById(R.id.view_team_container);
        View image = container.findViewById(R.id.team_view_team_icon);
        image.setOnClickListener(onClickListener);
        expandViewHitArea(image, container);
    }

    void setupConnectDeviceClickListeners(View parentView) {

        View container = parentView.findViewById(R.id.connect_device_container);
        View image = container.findViewById(R.id.navigate_to_connect_app_or_device);
        image.setOnClickListener(onClickListener);
        expandViewHitArea(image, container);

    }

    void expandViewHitArea(final View childView, final View parentView) {
        parentView.post(new Runnable() {
            @Override
            public void run() {
                Rect parentRect = new Rect();
                Rect childRect = new Rect();
                parentView.getHitRect(parentRect);
                childView.getHitRect(childRect);
                childRect.left = parentRect.left;
                childRect.right = parentRect.width();

                parentView.setTouchDelegate(new TouchDelegate(childRect, childView));
            }
        });
    }
}