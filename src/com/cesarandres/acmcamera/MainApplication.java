package com.cesarandres.acmcamera;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey="",
	mailTo = "cesar.ramsan@gmail.com",
	mode = ReportingInteractionMode.DIALOG,
	resToastText = R.string.crash_toast_text, 
	resDialogText = R.string.crash_dialog_text,
	resDialogIcon = android.R.drawable.ic_dialog_info, 
	resDialogTitle = R.string.crash_dialog_title, 	
	resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
	resDialogOkToast = R.string.crash_dialog_ok_toast)
public class MainApplication extends Application
{
	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}
}
