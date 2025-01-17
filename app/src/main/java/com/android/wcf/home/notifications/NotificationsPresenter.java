package com.android.wcf.home.notifications;

import com.android.wcf.model.Participant;

public class NotificationsPresenter implements NotificationsMvp.Presenter {
    private static final String TAG = NotificationsPresenter.class.getSimpleName();

    private NotificationsMvp.NotificationView notificationView;
    Participant mParticipant = null;

    public NotificationsPresenter(NotificationsMvp.NotificationView view) {
        this.notificationView = view;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void onStop() {

    }
}
