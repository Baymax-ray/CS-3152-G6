package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.Action;

public class AudioController {

    private static Sound chiyoSound;
    private static Sound momoSound;
    private static boolean isMomo;
    private static long chiyoSoundId;
    private static long momoSoundId;

    public AudioController(AssetDirectory assets){
        chiyoSound = Gdx.audio.newSound(Gdx.files.internal("music/ChiyoTheme-Ver2.mp3"));
        momoSound = Gdx.audio.newSound(Gdx.files.internal("music/MomoTheme-Ver2.mp3"));
        isMomo = true;
    }

    public static void playAllSound(){
        chiyoSoundId=chiyoSound.loop(0.5f);
        momoSoundId=momoSound.loop(0.5f);
    }

    public void updateAudio(float form){

        //0: momo, 1: chiyo
        if(form==1 && isMomo){
            isMomo=false;
            momoToChiyo();
        }else if (form == 0 && !isMomo){
            isMomo=true;
            chiyoToMomo();
        }
    }

    public static void muteChiyo(){
        chiyoSound.setVolume(chiyoSoundId,0);
    }

    public static void muteMomo(){
        momoSound.setVolume(momoSoundId,0);
    }

    public static void momoToChiyo(){
        muteMomo();
        chiyoSound.setVolume(chiyoSoundId,0.5f);
    }

    public static void chiyoToMomo(){
        muteChiyo();
        momoSound.setVolume(momoSoundId,0.5f);
    }

    public static void dispose(){
        chiyoSound.dispose();
        momoSound.dispose();
        isMomo=true;
    }

}
