package edu.cornell.gdiac.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.models.Settings;

public class AudioController implements SettingsObserver {


    private Sound chiyoSound;
    private Sound momoSound;
    private boolean isMomo;
    private long chiyoSoundId;
    private long momoSoundId;

    private ObjectMap<String, Sound> effects;
    private ObjectMap<Sound, Long> effectIds;
    private ObjectMap<Sound, Long> loopEffectIds;
    /** The jump sound.  We only want to play once. */
    private Sound jumpSound;

    /** The impact sound.  We only want to play once. */
    private Sound impactSound;

    /** The sword swipe sound.  We only want to play once. */
    private Sound swordSwipeSound;

    /** The player dash sound.  We only want to play once. */
    private Sound dashSound;

    /** The player transform to Chiyo sound.  We only want to play once. */
    private Sound playerChiyoTransformSound;

    /** The player transform to momo sound.  We only want to play once. */
    private Sound playerMomoTransformSound;

    /** The chiyo running sound.  We only want to play once. */
    private Sound chiyoRunSound;

    /** The momo running sound.  We only want to play once. */
    private Sound momoRunSound;

    private Sound smallImpactSound;

    private Sound swordKillingSound;
    private Sound swordHittingSound;

    private Sound wallSlideSound;

    private Settings settings;

    public AudioController(AssetDirectory assets, Settings settings){
        this.settings = settings;
        settings.addObserver(this);

        chiyoSound = assets.getEntry("music:chiyo", Sound.class);
        momoSound = assets.getEntry("music:momo", Sound.class);

        jumpSound = assets.getEntry("effect:temp-jump", Sound.class);
        impactSound = assets.getEntry("effect:temp-impact", Sound.class);
        swordSwipeSound = assets.getEntry("effect:temp-sword-swipe", Sound.class);
        dashSound = assets.getEntry("effect:temp-dash", Sound.class);
        playerChiyoTransformSound = assets.getEntry("effect:se1-trans", Sound.class);
        playerMomoTransformSound = assets.getEntry("effect:temp-transform-to-momo", Sound.class);
        chiyoRunSound = assets.getEntry("effect:temp-chiyo-run", Sound.class);
        momoRunSound = assets.getEntry("effect:temp-momo-run", Sound.class);
        smallImpactSound = assets.getEntry("effect:temp-small-impact", Sound.class);
        swordKillingSound = assets.getEntry("effect:temp-sword-killing", Sound.class);
        swordHittingSound = assets.getEntry("effect:temp-non-killing", Sound.class);
        wallSlideSound = assets.getEntry("effect:temp-wall-slide", Sound.class);

        effectIds = new ObjectMap<>();
        loopEffectIds = new ObjectMap<>();
        effects = new ObjectMap<>();
        effects.put("jump", jumpSound);
        effects.put("impact", impactSound);
        effects.put("sword-swipe", swordSwipeSound);
        effects.put("dash", dashSound);
        effects.put("chiyo-transform", playerChiyoTransformSound);
        effects.put("momo-transform", playerMomoTransformSound);
        effects.put("chiyo-run", chiyoRunSound);
        effects.put("momo-run", momoRunSound);
        effects.put("small-impact", smallImpactSound);
        effects.put("sword-kill", swordKillingSound);
        effects.put("sword-hit", swordHittingSound);
        effects.put("wall-slide", wallSlideSound);

        isMomo = true;
    }

    public void playAllSound(){
        chiyoSoundId=chiyoSound.loop(0.2f);
        momoSoundId=momoSound.loop(0.2f);
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

    public void muteChiyo(){
        chiyoSound.setVolume(chiyoSoundId,0);
    }

    public void muteMomo(){
        momoSound.setVolume(momoSoundId,0);
    }

    public void momoToChiyo(){
        muteMomo();
        chiyoSound.setVolume(chiyoSoundId,0.2f);
    }

    public void chiyoToMomo(){
        muteChiyo();
        momoSound.setVolume(momoSoundId,0.2f);
    }

    public void dispose(){
        isMomo=true;
        settings.removeObserver(this);
    }

    private void playEffect(Sound effect, float volume) {
        if (effectIds.containsKey(effect)) {
            effect.stop(effectIds.get(effect));
            effectIds.remove(effect);
        }
        if (loopEffectIds.containsKey(effect)) {
            effect.stop(loopEffectIds.get(effect));
            loopEffectIds.remove(effect);
        }
        long id = effect.play(volume);
        effectIds.put(effect, id);
    }

    public void playEffect(String effectName, float volume) {
        if (!effects.containsKey(effectName)) {
            System.out.println("WARNING: sound name " + effectName + " does not exist in AudioController.");
            return;
        }

        playEffect(effects.get(effectName), volume);
    }

    public void loopEffect(Sound effect, float volume) {
        if (effectIds.containsKey(effect)) {
            effect.stop(effectIds.get(effect));
        }
        if (loopEffectIds.containsKey(effect)) {
            return; // already looping no need to do anything
        }
        long id = effect.loop(volume);
        loopEffectIds.put(effect, id);
    }

    public void loopEffect(String effectName, float volume) {
        if (!effects.containsKey(effectName)) {
            System.out.println("WARNING: sound name " + effectName + " does not exist in AudioController.");
            return;
        }

        loopEffect(effects.get(effectName), volume);
    }

    public void stopEffect(String effectName) {
        if (!effects.containsKey(effectName)) {
            System.out.println("WARNING: sound name " + effectName + " does not exist in AudioController.");
            return;
        }

        effects.get(effectName).stop();
        effectIds.remove(effects.get(effectName));
        loopEffectIds.remove(effects.get(effectName));
    }

    @Override
    public void onMasterVolumeChange(float newVolume) {
        //TODO
    }

    @Override
    public void onMusicVolumeChange(float newVolume) {
        //TODO
    }

    @Override
    public void onSfxVolumeChange(float newVolume) {
        //TODO
    }
}
