package com.decodedhealth.flutter_zoom_plugin;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import us.zoom.sdk.InviteOptions;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.StartMeetingParamsWithoutLogin;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKAuthenticationListener;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

public class ZoomView implements PlatformView,
        MethodChannel.MethodCallHandler,
        ZoomSDKAuthenticationListener {
    private final TextView textView;
    private final MethodChannel methodChannel;
    private final Context context;
    private final EventChannel meetingStatusChannel;

    ZoomView(Context context, BinaryMessenger messenger, int id) {
        textView = new TextView(context);
        this.context = context;

        methodChannel = new MethodChannel(messenger, "com.decodedhealth/flutter_zoom_plugin");
        methodChannel.setMethodCallHandler(this);

        meetingStatusChannel = new EventChannel(messenger, "com.decodedhealth/zoom_event_stream");
    }

    @Override
    public View getView() {
        return textView;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "init":
                init(methodCall, result);
                break;
            case "join":
                joinMeeting(methodCall, result);
                break;
            case "start":
                startMeeting(methodCall, result);
                break;
            case "meeting_status":
                meetingStatus(result);
                break;
            default:
                result.notImplemented();
        }

    }

    private void init(final MethodCall methodCall, final MethodChannel.Result result) {

        Map<String, String> options = methodCall.arguments();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if (zoomSDK.isInitialized()) {
            List<Integer> response = Arrays.asList(0, 0);
            result.success(response);
            return;
        }

        ZoomSDKInitParams initParams = new ZoomSDKInitParams();
        initParams.jwtToken = options.get("sdkToken");
        initParams.appKey = options.get("appKey");
        initParams.appSecret = options.get("appSecret");
        initParams.domain = options.get("domain");
        zoomSDK.initialize(
                context,
                new ZoomSDKInitializeListener() {

                    @Override
                    public void onZoomAuthIdentityExpired() {

                    }

                    @Override
                    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
                        List<Integer> response = Arrays.asList(errorCode, internalErrorCode);

                        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
                            System.out.println("Failed to initialize Zoom SDK");
                            result.success(response);
                            return;
                        }

                        ZoomSDK zoomSDK = ZoomSDK.getInstance();
                        MeetingService meetingService = zoomSDK.getMeetingService();
                        meetingStatusChannel.setStreamHandler(new StatusStreamHandler(meetingService));
                        result.success(response);
                    }
                },
                initParams);
    }

    private void joinMeeting(MethodCall methodCall, MethodChannel.Result result) {

        Map<String, String> options = methodCall.arguments();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if (!zoomSDK.isInitialized()) {
            System.out.println("Not initialized!!!!!!");
            result.success(false);
            return;
        }

        final MeetingService meetingService = zoomSDK.getMeetingService();

        JoinMeetingOptions opts = new JoinMeetingOptions();
//        opts.no_invite = parseBoolean(options, "disableInvite", false);
//        opts.no_share = parseBoolean(options, "disableShare", false);
//        opts.no_driving_mode = parseBoolean(options, "disableDrive", false);
//        opts.no_dial_in_via_phone = parseBoolean(options, "disableDialIn", false);
//        opts.no_disconnect_audio = parseBoolean(options, "noDisconnectAudio", false);
//        opts.no_audio = parseBoolean(options, "noAudio", false);

        opts.invite_options = InviteOptions.INVITE_DISABLE_ALL;
        opts.no_invite = true;
        opts.no_meeting_end_message = true;
        opts.meeting_views_options = 104;
        opts.no_titlebar = false;
        opts.no_dial_in_via_phone = true;
        opts.no_dial_out_to_phone = true;

//        invite_options: >>0
//        no_invite: >>true
//        no_meeting_end_message: >>true
//        meeting_views_options: >>104
//        no_titlebar: >>false
//        no_dial_in_via_phone: >>true
//        no_dial_out_to_phone: >>true

        JoinMeetingParams params = new JoinMeetingParams();

        Log.e("userId",""+options.get("userId"));
        Log.e("meetingId",""+options.get("meetingId"));
        Log.e("meetingPassword",""+options.get("meetingPassword"));
        params.displayName = options.get("userId");
        params.meetingNo = options.get("meetingId");
        params.password = options.get("meetingPassword");

        meetingService.joinMeetingWithParams(context, params, opts);

        result.success(true);
    }

    private void startMeeting(MethodCall methodCall, MethodChannel.Result result) {

        Map<String, String> options = methodCall.arguments();

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if (!zoomSDK.isInitialized()) {
            System.out.println("Not initialized!!!!!!");
            result.success(false);
            return;
        }

        final MeetingService meetingService = zoomSDK.getMeetingService();

        StartMeetingOptions opts = new StartMeetingOptions();
//        opts.no_invite = parseBoolean(options, "disableInvite", false);
//        opts.no_share = parseBoolean(options, "disableShare", false);
//        opts.no_driving_mode = parseBoolean(options, "disableDrive", false);
//        opts.no_dial_in_via_phone = parseBoolean(options, "disableDialIn", false);
//        opts.no_disconnect_audio = parseBoolean(options, "noDisconnectAudio", false);
//        opts.no_audio = parseBoolean(options, "noAudio", false);


        opts.no_driving_mode = false;
        opts.no_invite = false;
        opts.no_meeting_end_message = false;
        opts.no_titlebar = false;
        opts.no_bottom_toolbar = false;
        opts.no_dial_in_via_phone = false;
        opts.no_dial_out_to_phone = false;
        opts.no_disconnect_audio = false;
        opts.no_share = false;
        opts.invite_options = 255;
        opts.no_video = false;
        opts.meeting_views_options = 0;
        opts.no_meeting_error_message = false;
        opts.participant_id = null;

//        no_driving_mode: >>false
//        no_invite: >>false
//        no_meeting_end_message: >>false
//        no_titlebar: >>false
//        no_bottom_toolbar: >>false
//        no_dial_in_via_phone: >>false
//        no_dial_out_to_phone: >>false
//        no_disconnect_audio: >>false
//        no_share: >>false
//        invite_options: >>255
//        no_video: >>false
//        meeting_views_options: >>0
//        no_meeting_error_mess: >>false
//        participant_id: >>null


        StartMeetingParamsWithoutLogin params = new StartMeetingParamsWithoutLogin();

        params.userId = options.get("userId");
        params.displayName = options.get("displayName");
        params.meetingNo = options.get("meetingId");
        params.userType = MeetingService.USER_TYPE_API_USER;
        params.zoomToken = options.get("zoomToken");
        params.zoomAccessToken = options.get("zoomAccessToken");

        meetingService.startMeetingWithParams(context, params, opts);

        result.success(true);
    }

    private boolean parseBoolean(Map<String, String> options, String property, boolean defaultValue) {
        return options.get(property) == null ? defaultValue : Boolean.parseBoolean(options.get(property));
    }


    private void meetingStatus(MethodChannel.Result result) {

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if (!zoomSDK.isInitialized()) {
            System.out.println("Not initialized!!!!!!");
            result.success(Arrays.asList("MEETING_STATUS_UNKNOWN", "SDK not initialized"));
            return;
        }

        MeetingService meetingService = zoomSDK.getMeetingService();

        if (meetingService == null) {
            result.success(Arrays.asList("MEETING_STATUS_UNKNOWN", "No status available"));
            return;
        }

        MeetingStatus status = meetingService.getMeetingStatus();
        result.success(status != null ? Arrays.asList(status.name(), "") : Arrays.asList("MEETING_STATUS_UNKNOWN", "No status available"));
    }

    @Override
    public void dispose() {
    }

    @Override
    public void onZoomAuthIdentityExpired() {

    }

    @Override
    public void onZoomSDKLoginResult(long result) {

    }

    @Override
    public void onZoomSDKLogoutResult(long result) {

    }

    @Override
    public void onZoomIdentityExpired() {

    }
}
