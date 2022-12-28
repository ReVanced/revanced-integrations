package app.revanced.integrations.videoplayer;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import app.revanced.integrations.utils.ReVancedUtils;

public class DownloadButtonAnimation {

    static Animation getFadeInAnimation(){
        return getAnimation("fade_id","fade_duration_fast");
    }
    static Animation getFadeOutAnimation(){
        return getAnimation("fade_out","fade_duration_scheduled");
    }
    private static Animation getAnimation(String animationId,String durationId){
        Animation animation = getAnimation(animationId);
        int duration = getInteger(durationId);
        animation.setDuration(duration);
        return animation;
    }
    private static int getIdentifier(String str, String str2) {
        Context appContext = ReVancedUtils.getContext();
        return appContext.getResources().getIdentifier(str, str2, appContext.getPackageName());
    }

    private static int getInteger(String str) {
        return ReVancedUtils.getContext().getResources().getInteger(getIdentifier(str, "integer"));
    }
    private static Animation getAnimation(String str) {
        return AnimationUtils.loadAnimation(ReVancedUtils.getContext(), getIdentifier(str, "anim"));
    }
}
