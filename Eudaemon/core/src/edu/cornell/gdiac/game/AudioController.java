package edu.cornell.gdiac.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class AudioController {

    public Sound chiyoSound;
    public Sound momoSound;

    public AudioController(){
        chiyoSound = Gdx.audio.newSound(Gdx.files.internal("music/ChiyoTheme.mp3"));
        momoSound = Gdx.audio.newSound(Gdx.files.internal("music/MomoTheme.mp3"));
    }

    public void playAllSound(){
        chiyoSound.loop();
        momoSound.loop();
    }

    public void muteChiyo(){
        chiyoSound.setVolume(0,0);
    }

    public void muteMomo(){
        momoSound.setVolume(0,0);
    }

    public void momoToChiyo(){
        muteMomo();
        momoSound.setVolume(1,1);
    }

    public void chiyoToMomo(){
        muteChiyo();
        momoSound.setVolume(1,1);
    }

    public void reset(){
        chiyoSound.dispose();
        momoSound.dispose();
    }

}
