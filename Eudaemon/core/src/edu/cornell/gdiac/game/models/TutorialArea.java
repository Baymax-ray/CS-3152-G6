package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.FontTextureLoader;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.SettingsObserver;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;

public class TutorialArea extends BoxObstacle implements SettingsObserver {

    private TextureRegion tutorialTexture;
    private boolean display;
    private PolygonShape sensorShape;
    private final String sensorName;
    private final JsonValue tutorialAreaData;
    private float tutorialImageWidth;
    private float tutorialImageHeight;

    private final AssetDirectory assets;

    private TutorialType tutorialType;

    private FontTextureLoader fontTextureLoader;
    private Settings settings;

    private enum TutorialType {
        MOVE,
        JUMP,
        DASH_JUMP,
        DASH_MULTIDIRECTIONAL,
        TRANSFORM,
        ATTACK_MULTIDIRECTIONAL,
    }
    /**
     * The texture scale along the x-axis.
     */
    private float scaleX;
    /**
     * The texture scale along the y-axis.
     */
    private float scaleY;
    /**
     * The texture origin offset value along the y-axis.
     */
    private float oyOffset;
    /**
     * The texture origin offset value along the y-axis.
     */
    private float oxOffset;
    /**
     * The offset value along the x-axis.
     */
    private float xOffset;
    /**
     * The offset value along the y-axis.
     */
    private float yOffset;

    public String getSensorName() {return this.sensorName;}

    public void setDisplay(boolean value) { this.display = value; }
    public boolean isDisplay() { return display; }

    public TutorialArea(JsonValue json, AssetDirectory assets, float x, float y, FontTextureLoader fontTextureLoader, Settings settings) {
        super(x, y,
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxWidth"),
                assets.getEntry("sharedConstants", JsonValue.class).get("Spike").getFloat("hitboxHeight"));
        this.assets = assets;
        this.tutorialAreaData = assets.getEntry("sharedConstants", JsonValue.class).get("TutorialArea");

        this.fontTextureLoader = fontTextureLoader;
        this.settings = settings;
        settings.addObserver(this);

        String type = json.get("properties").get(0).getString("value");

        if (type.equals("MOVE")) {
            this.tutorialType = TutorialType.MOVE;
        } else if (type.equals("JUMP")) {
            this.tutorialType = TutorialType.JUMP;
        } else if (type.equals("DASH_JUMP")) {
            this.tutorialType = TutorialType.DASH_JUMP;
        } else if (type.equals("DASH_MULTIDIRECTIONAL")) {
            this.tutorialType = TutorialType.DASH_MULTIDIRECTIONAL;
        } else if (type.equals("TRANSFORM")) {
            this.tutorialType = TutorialType.TRANSFORM;
        } else if (type.equals("ATTACK_MULTIDIRECTIONAL")) {
            this.tutorialType = TutorialType.ATTACK_MULTIDIRECTIONAL;
        }

        if (this.tutorialType == TutorialType.MOVE) {
            String entry = (settings.getUseArrowKeys()) ? "platform:combo_keyboardLeftRight_joystickLeftRight" : "platform:combo_keyboardA+D_joystickLeftRight";
            tutorialTexture = new TextureRegion(assets.getEntry(entry, Texture.class));
        } else if (this.tutorialType == TutorialType.JUMP) {
            tutorialTexture = createJumpTutorial();
        } else if (this.tutorialType == TutorialType.DASH_JUMP) {
            tutorialTexture = createDashJumpTutorial();
        } else if (this.tutorialType == TutorialType.TRANSFORM) {
            tutorialTexture = createTransformTutorial();
        } else if (this.tutorialType == TutorialType.DASH_MULTIDIRECTIONAL) {
            tutorialTexture = createDashMultidirectionalTutorial();
        } else if (this.tutorialType == TutorialType.ATTACK_MULTIDIRECTIONAL) {
            tutorialTexture = createAttackMultidirectionalTutorial();
        }
//        this.tutorialTexture = new TextureRegion(assets.getEntry(tutorialTextureAsset, Texture.class));
        this.setWidth(tutorialAreaData.getFloat("hitboxWidth"));
        this.setHeight(tutorialAreaData.getFloat("hitboxHeight"));
        this.tutorialImageWidth = tutorialAreaData.getFloat("ImageWidth");
        this.tutorialImageHeight = tutorialAreaData.getFloat("ImageHeight");
        this.sensorName = "BillboardSensor";
        this.texture = this.tutorialTexture;
        scaleX = tutorialAreaData.getFloat("drawScaleX");
        scaleY = tutorialAreaData.getFloat("drawScaleY");
        oxOffset = tutorialAreaData.getFloat("oxOffset");
        oyOffset = tutorialAreaData.getFloat("oyOffset");
        xOffset = tutorialAreaData.getFloat("xOffset");
        yOffset = tutorialAreaData.getFloat("yOffset");
        this.setX(x + xOffset);
        this.setY(y + yOffset);

        this.display = false;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) { }

    public void displayTutorial(GameCanvas canvas, Level level) {
        float x = level.getPlayer().getX();
        float y = level.getPlayer().getY() + 2;

        float ox = oxOffset + tutorialTexture.getRegionWidth()/2;
        float oy = oyOffset + tutorialTexture.getRegionHeight()/2;

        float sx = scaleX * tutorialImageWidth / tutorialTexture.getRegionHeight();
        float sy = scaleY * tutorialImageHeight / tutorialTexture.getRegionHeight();

        canvas.draw(tutorialTexture, Color.WHITE, ox, oy, x, y, 0, sx, sy);
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = tutorialAreaData.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = tutorialAreaData.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        super.body.setType(BodyType.StaticBody);
//        super.setBodyTypeToStatic();

        return true;
    }

    public TextureRegion createJumpTutorial() {
        return createSimpleTutorial(settings.getActionBindings().getKeyMap().get(Action.BEGIN_JUMP), "platform:controllerButtonDown");
    }

    public TextureRegion createTransformTutorial() {
        return createSimpleTutorial(settings.getActionBindings().getKeyMap().get(Action.TRANSFORM), "platform:controllerButtonUp");
    }

    public TextureRegion createSimpleTutorial(int keycode, String controllerEntry) {
        Texture controllerTexture = assets.getEntry(controllerEntry, Texture.class);
        Texture keyboardTexture;
        if (keycode == Input.Keys.SPACE) {
            keyboardTexture = assets.getEntry("platform:keyboardSpace", Texture.class);
        } else if (keycode == Input.Keys.LEFT) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowLeft", Texture.class);
        } else if (keycode == Input.Keys.RIGHT) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowRight", Texture.class);
        } else if (keycode == Input.Keys.UP) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowUp", Texture.class);
        } else if (keycode == Input.Keys.DOWN) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowDown", Texture.class);
        } else {
            keyboardTexture = assets.getEntry("platform:keyboardBlank", Texture.class);
        }

        controllerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        keyboardTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        BitmapFont font = assets.getEntry("font:keycap", BitmapFont.class);
        float controllerScale = 3.0f;
        float width = Math.max(controllerScale * controllerTexture.getWidth(), keyboardTexture.getWidth());
        float height = controllerScale * controllerTexture.getHeight() + keyboardTexture.getHeight() + 20;

        return new TextureRegion(fontTextureLoader.createTexture(spriteBatch -> {
            spriteBatch.draw(controllerTexture, width / 2.0f - controllerScale * controllerTexture.getWidth()/2.0f, 0, controllerScale * controllerTexture.getWidth(), controllerScale * controllerTexture.getHeight());
            spriteBatch.draw(keyboardTexture, width / 2.0f - keyboardTexture.getWidth()/2.0f, height - keyboardTexture.getHeight(), keyboardTexture.getWidth(), keyboardTexture.getHeight());
            String text = Input.Keys.toString(keycode);
            if (text.length() == 1) {
                GlyphLayout layout = new GlyphLayout(font, text);
                font.setColor(Color.CYAN);
                font.draw(spriteBatch, layout, width / 2.0f - layout.width / 2.0f, height - keyboardTexture.getHeight() / 2.0f + layout.height / 2.0f);
            }
        }, width, height));
    }

    public TextureRegion createDashMultidirectionalTutorial() {
        return createMultidirectionalTutorial(settings.getActionBindings().getKeyMap().get(Action.DASH));
    }

    public TextureRegion createAttackMultidirectionalTutorial() {
        return createMultidirectionalTutorial(settings.getActionBindings().getKeyMap().get(Action.ATTACK));
    }

    public TextureRegion createMultidirectionalTutorial(int keycode) {
        Texture controllerTexture = assets.getEntry("platform:controllerMultidirectional", Texture.class);

        Texture keyboardTexture;
        if (keycode == Input.Keys.SPACE) {
            keyboardTexture = assets.getEntry("platform:keyboardSpace", Texture.class);
        } else if (keycode == Input.Keys.LEFT) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowLeft", Texture.class);
        } else if (keycode == Input.Keys.RIGHT) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowRight", Texture.class);
        } else if (keycode == Input.Keys.UP) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowUp", Texture.class);
        } else if (keycode == Input.Keys.DOWN) {
            keyboardTexture = assets.getEntry("platform:keyboardArrowDown", Texture.class);
        } else {
            keyboardTexture = assets.getEntry("platform:keyboardBlank", Texture.class);
        }

        String directionalEntry = (settings.getUseArrowKeys()) ? "platform:keyboardArrows" : "platform:keyboardWASD";
        Texture directionalTexture = assets.getEntry(directionalEntry, Texture.class);
        Texture plusSign = assets.getEntry("platform:plusSign", Texture.class);

        controllerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        keyboardTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        directionalTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        plusSign.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        BitmapFont font = assets.getEntry("font:keycap", BitmapFont.class);
        float plusScale = 3.0f;
        float controllerScale = 3.0f;
        float width = Math.max(controllerScale * controllerTexture.getWidth(), keyboardTexture.getWidth() + plusScale * plusSign.getWidth() + directionalTexture.getWidth() / (float) directionalTexture.getHeight() * keyboardTexture.getHeight());
        float height = controllerScale * controllerTexture.getHeight() + keyboardTexture.getHeight() + 20;

        return new TextureRegion(fontTextureLoader.createTexture(spriteBatch -> {
            spriteBatch.draw(controllerTexture, width / 2.0f - controllerScale * controllerTexture.getWidth()/2.0f, 0, controllerScale * controllerTexture.getWidth(), controllerScale * controllerTexture.getHeight());
            spriteBatch.draw(keyboardTexture, width - keyboardTexture.getWidth(), height - keyboardTexture.getHeight(), keyboardTexture.getWidth(), keyboardTexture.getHeight());
            spriteBatch.draw(directionalTexture, 0, height - keyboardTexture.getHeight(), directionalTexture.getWidth() / (float) directionalTexture.getHeight() * keyboardTexture.getHeight(), keyboardTexture.getHeight());
            spriteBatch.draw(plusSign, width - keyboardTexture.getWidth() - plusScale * plusSign.getWidth(), height - keyboardTexture.getHeight()/2.0f - plusScale * plusSign.getHeight()/2.0f, plusScale * plusSign.getWidth(), plusScale * plusSign.getHeight());
            String text = Input.Keys.toString(keycode);
            if (text.length() == 1) {
                GlyphLayout layout = new GlyphLayout(font, text);
                font.setColor(Color.CYAN);
                font.draw(spriteBatch, layout, width - keyboardTexture.getWidth()/2.0f - layout.width / 2.0f, height - keyboardTexture.getHeight() / 2.0f + layout.height / 2.0f);
            }
        }, width, height));
    }

    public TextureRegion createDashJumpTutorial() {
        Texture controllerTexture = assets.getEntry("platform:controllerMultidirectional", Texture.class);
        Texture controllerJumpTexture = assets.getEntry("platform:controllerButtonDown", Texture.class);

        Texture jumpTexture;
        int jumpKeycode = settings.getActionBindings().getKeyMap().get(Action.BEGIN_JUMP);
        if (jumpKeycode == Input.Keys.SPACE) {
            jumpTexture = assets.getEntry("platform:keyboardSpace", Texture.class);
        } else if (jumpKeycode == Input.Keys.LEFT) {
            jumpTexture = assets.getEntry("platform:keyboardArrowLeft", Texture.class);
        } else if (jumpKeycode == Input.Keys.RIGHT) {
            jumpTexture = assets.getEntry("platform:keyboardArrowRight", Texture.class);
        } else if (jumpKeycode == Input.Keys.UP) {
            jumpTexture = assets.getEntry("platform:keyboardArrowUp", Texture.class);
        } else if (jumpKeycode == Input.Keys.DOWN) {
            jumpTexture = assets.getEntry("platform:keyboardArrowDown", Texture.class);
        } else {
            jumpTexture = assets.getEntry("platform:keyboardBlank", Texture.class);
        }

        Texture dashTexture;
        int dashKeycode = settings.getActionBindings().getKeyMap().get(Action.DASH);
        if (dashKeycode == Input.Keys.SPACE) {
            dashTexture = assets.getEntry("platform:keyboardSpace", Texture.class);
        } else if (dashKeycode == Input.Keys.LEFT) {
            dashTexture = assets.getEntry("platform:keyboardArrowLeft", Texture.class);
        } else if (dashKeycode == Input.Keys.RIGHT) {
            dashTexture = assets.getEntry("platform:keyboardArrowRight", Texture.class);
        } else if (dashKeycode == Input.Keys.UP) {
            dashTexture = assets.getEntry("platform:keyboardArrowUp", Texture.class);
        } else if (dashKeycode == Input.Keys.DOWN) {
            dashTexture = assets.getEntry("platform:keyboardArrowDown", Texture.class);
        } else {
            dashTexture = assets.getEntry("platform:keyboardBlank", Texture.class);
        }

        String directionalEntry = (settings.getUseArrowKeys()) ? "platform:keyboardArrows" : "platform:keyboardWASD";
        Texture directionalTexture = assets.getEntry(directionalEntry, Texture.class);
        Texture plusSign = assets.getEntry("platform:plusSign", Texture.class);

        controllerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        jumpTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        directionalTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        plusSign.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        BitmapFont font = assets.getEntry("font:keycap", BitmapFont.class);
        float plusScale = 3.0f;
        float controllerScale = 3.0f;
        float width = Math.max(controllerScale * controllerTexture.getWidth() + plusScale * plusSign.getWidth() + controllerScale * controllerJumpTexture.getWidth(), jumpTexture.getWidth() + dashTexture.getWidth() + 2 * plusScale * plusSign.getWidth() + directionalTexture.getWidth() / (float) directionalTexture.getHeight() * jumpTexture.getHeight());
        float height = controllerScale * controllerTexture.getHeight() + jumpTexture.getHeight() + 20;

        float lowerWidth = controllerScale * controllerTexture.getWidth() + plusScale * plusSign.getWidth() + controllerScale * controllerJumpTexture.getWidth();

        return new TextureRegion(fontTextureLoader.createTexture(spriteBatch -> {
            spriteBatch.draw(controllerTexture, width / 2.0f + lowerWidth/2.0f - controllerScale * controllerTexture.getWidth(), 0, controllerScale * controllerTexture.getWidth(), controllerScale * controllerTexture.getHeight());
            spriteBatch.draw(plusSign, width / 2.0f - lowerWidth/2.0f + controllerScale * controllerJumpTexture.getWidth(), 0, plusScale * plusSign.getWidth(), plusScale * plusSign.getHeight());
            spriteBatch.draw(controllerJumpTexture, width / 2.0f - lowerWidth / 2.0f, 0, controllerScale * controllerJumpTexture.getWidth(), controllerScale * controllerJumpTexture.getHeight());
            spriteBatch.draw(dashTexture, width - dashTexture.getWidth(), height - dashTexture.getHeight(), dashTexture.getWidth(), dashTexture.getHeight());
            spriteBatch.draw(jumpTexture, 0, height - jumpTexture.getHeight(), jumpTexture.getWidth(), jumpTexture.getHeight());
            spriteBatch.draw(plusSign, width - dashTexture.getWidth() - plusScale * plusSign.getWidth(), height - dashTexture.getHeight()/2.0f - plusScale * plusSign.getHeight()/2.0f, plusScale * plusSign.getWidth(), plusScale * plusSign.getHeight());
            spriteBatch.draw(plusSign, jumpTexture.getWidth(), height - jumpTexture.getHeight()/2.0f - plusScale * plusSign.getHeight()/2.0f, plusScale * plusSign.getWidth(), plusScale * plusSign.getHeight());
            spriteBatch.draw(directionalTexture, jumpTexture.getWidth() + plusScale * plusSign.getWidth(), height - jumpTexture.getHeight(), directionalTexture.getWidth() / (float) directionalTexture.getHeight() * jumpTexture.getHeight(), jumpTexture.getHeight());
            String text = Input.Keys.toString(dashKeycode);
            if (text.length() == 1) {
                GlyphLayout layout = new GlyphLayout(font, text);
                font.setColor(Color.CYAN);
                font.draw(spriteBatch, layout, width - dashTexture.getWidth()/2.0f - layout.width / 2.0f, height - dashTexture.getHeight() / 2.0f + layout.height / 2.0f);
            }
            text = Input.Keys.toString(jumpKeycode);
            if (text.length() == 1) {
                GlyphLayout layout = new GlyphLayout(font, text);
                font.setColor(Color.CYAN);
                font.draw(spriteBatch, layout, jumpTexture.getWidth()/2.0f - layout.width / 2.0f, height - jumpTexture.getHeight() / 2.0f + layout.height / 2.0f);
            }
        }, width, height));
    }

    @Override
    public void onUseArrowKeys(boolean useArrowKeys) {
        fontTextureLoader.disposeTexture(tutorialTexture.getTexture());
        if (this.tutorialType == TutorialType.MOVE) {
            String entry = (!settings.getUseArrowKeys()) ? "platform:combo_keyboardA+D_joystickLeftRight" : "platform:combo_keyboardLeftRight_joystickLeftRight";
            tutorialTexture = new TextureRegion(assets.getEntry(entry, Texture.class));
        } else if (this.tutorialType == TutorialType.JUMP) {
            tutorialTexture = createJumpTutorial();
        } else if (this.tutorialType == TutorialType.DASH_JUMP) {
            tutorialTexture = createDashJumpTutorial();
        } else if (this.tutorialType == TutorialType.TRANSFORM) {
            tutorialTexture = createTransformTutorial();
        } else if (this.tutorialType == TutorialType.DASH_MULTIDIRECTIONAL) {
            tutorialTexture = createDashMultidirectionalTutorial();
        } else if (this.tutorialType == TutorialType.ATTACK_MULTIDIRECTIONAL) {
            tutorialTexture = createAttackMultidirectionalTutorial();
        }
    }

    @Override
    public void onCustomBinding(Action action, String newBinding) {
        fontTextureLoader.disposeTexture(tutorialTexture.getTexture());
        if (this.tutorialType == TutorialType.MOVE) {
            String entry = (!settings.getUseArrowKeys()) ? "platform:combo_keyboardA+D_joystickLeftRight" : "platform:combo_keyboardLeftRight_joystickLeftRight";
            tutorialTexture = new TextureRegion(assets.getEntry(entry, Texture.class));
        } else if (this.tutorialType == TutorialType.JUMP) {
            tutorialTexture = createJumpTutorial();
        } else if (this.tutorialType == TutorialType.DASH_JUMP) {
            tutorialTexture = createDashJumpTutorial();
        } else if (this.tutorialType == TutorialType.TRANSFORM) {
            tutorialTexture = createTransformTutorial();
        } else if (this.tutorialType == TutorialType.DASH_MULTIDIRECTIONAL) {
            tutorialTexture = createDashMultidirectionalTutorial();
        } else if (this.tutorialType == TutorialType.ATTACK_MULTIDIRECTIONAL) {
            tutorialTexture = createAttackMultidirectionalTutorial();
        }
    }
}
