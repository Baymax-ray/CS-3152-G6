package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.UIOverlay;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

import java.util.Iterator;

public class Level {

    //#region FINAL FIELDS
    private final Player player;
    private final Enemy[] enemies;

    // NOTE: the natural way of viewing a 2d array is flipped for the map. tilemap[0] is the top of the map. need fancy
    // conversions between the two spaces
    private final int[][] tilemap; // value represents the index of the tile in the tiles array
    private final Tile[] tiles;
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    //private final TextureRegion backgroundTexture;

    /**
     * In level coordinates (not pixels), the width and height of a single tile.
     */
    private final float tileSize;

    /**
     * In level coordinates, the width of the region the camera displays
     */
    private final float cameraWidth;

    /**
     * In level coordinates, the height of the region the camera displays
     */
    private final float cameraHeight;

    private final float gravity;

    //#endregion


    //#region NONFINAL FIELDS
    private UIOverlay uiElements;
    private World world;

    /** Queue for adding objects */
    private PooledList<Obstacle> addQueue;

    private PooledList<Obstacle> objects;

    /**
     * Whether this level has been completed
     */
    private boolean isCompleted;

    /**
     * Whether we are in debug mode or not.
     */
    private boolean debug;

    /**
     * The x position of the camera in level coordinates
     */
    private float cameraX;

    /**
     * The y position of the camera in level coordinates
     */
    private float cameraY;

    //#endregion

    //#region GETTERS & SETTERS
    public World getWorld() {
        return world;
    }

    public float getGravity() {
        return gravity;
    }

    public Enemy[] getEnemies() {
        return enemies;
    }

    /**
     * Returns the player associated with this level instance.
     *
     * @return the player associated with this level instance
     */
    public Player getPlayer() {
        return player;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    //#endregion

    public Level(JsonValue json, Tile[] tiles, AssetDirectory assets) {
        this.tiles = tiles;

        int widthInTiles = json.getInt("widthInTiles");
        int heightInTiles = json.getInt("heightInTiles");

        this.tilemap = new int[heightInTiles][widthInTiles];
        for (int y = 0; y < heightInTiles; y++) {
            JsonValue row = json.get("tilemap").get(y);
            for (int x = 0; x < widthInTiles; x++) {
                tilemap[y][x] = row.getInt(x);
            }
        }

        this.tileSize = json.getFloat("tileSize");
        this.cameraWidth = json.getFloat("cameraWidth");
        this.cameraHeight = json.getFloat("cameraHeight");
        this.gravity = json.getFloat("gravity");


//        String backgroundAsset = json.getString("backgroundAsset");
//        this.backgroundTexture = assets.get(backgroundAsset);

        this.world = new World(new Vector2(0, gravity), true);
        this.player = new Player(json.get("player"), assets);
        uiElements = new UIOverlay(json.get("player"), assets, player.getHearts(), player.getSpirit());



        // TODO: need more than 1 ideally
        //this is a temporary code!!
        this.enemies = new Enemy[1];
        //System.out.println(json.get("enemy").get(0).toString());
        this.enemies[0]=new Enemy(json.get("enemy").get(0),assets);

        this.bodyDef = new BodyDef();
        this.bodyDef.type = BodyDef.BodyType.StaticBody;
        this.bodyDef.active = false;

        this.fixtureDef = new FixtureDef();

        this.addQueue = new PooledList<>();
        this.objects = new PooledList<>();

        this.debug = false;
    }

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return
     */
    public Tile tileAt(float x, float y) {
        return tiles[tilemap[levelToTileCoordinatesX(x)][levelToTileCoordinatesY(y)]];
    }

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return whether the point is in an air tile.
     */
    public boolean isAirAt(float x, float y) {
        return tilemap[levelToTileCoordinatesX(x)][levelToTileCoordinatesY(y)] < 0;
    }


    public int levelToTileCoordinatesX(float x) {
        return (int) Math.floor(x / this.tileSize);
    }

    public int levelToTileCoordinatesY(float y) {
        return tilemap.length - 1 - (int) Math.floor(y / this.tileSize);
    }

    public float tileToLevelCoordinatesX(int x) {
        return x * this.tileSize;
    }

    public float tileToLevelCoordinatesY(int y) {
        return (tilemap.length - 1 - y) * this.tileSize;
    }

    //#region Other Methods
    /**
     *
     * Adds a physics object in to the insertion queue.
     *
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     *
     * param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }


    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    public void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }
    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        float boundsX = tilemap[0].length * tileSize;
        float boundsY = tilemap.length * tileSize;
        boolean horiz = (obj.getX() >= 0 && obj.getX() <= boundsX);
        boolean vert  = (obj.getY() >= 0 && obj.getY() <= boundsY);
        return horiz && vert;
    }
    //#endregion

    public void update(float delta){
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }


        world.step(delta, 6, 2);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(delta);
            }
        }



    }

    public void draw(GameCanvas canvas) {
        canvas.begin();

        handleGameplayCamera(canvas);

//        canvas.draw(backgroundTexture, 0, 0);

        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < row.length; x++) {
                int tileId = row[x];
                if (tileId < 0) continue;
                Tile tile = tiles[tileId];
                float sx = tileSize / tile.getTexture().getRegionWidth();
                float sy = tileSize / tile.getTexture().getRegionHeight();
                canvas.draw(tile.getTexture(), Color.WHITE, 0, 0, tileToLevelCoordinatesX(x), tileToLevelCoordinatesY(y), 0, sx, sy);
            }
        }

        for (Obstacle obj : objects) {
            obj.draw(canvas);
        }
        canvas.end();

        if (debug) {
            canvas.beginDebug(1, 1);
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        canvas.setOverlayCamera();
        canvas.begin();
        uiElements.draw(canvas);
        canvas.end();
    }

    public void activatePhysics() {
        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < row.length; x++) {
                int tileId = row[x];
                if (tileId < 0) continue;
                Tile tile = tiles[tileId];
                bodyDef.position.x = tileToLevelCoordinatesX(x);
                bodyDef.position.y = tileToLevelCoordinatesY(y);
                bodyDef.active = true;
                Body body = world.createBody(bodyDef);
                body.setUserData(tile);

                PolygonShape shape = new PolygonShape();
                shape.set(new float[]{
                        0,        0,
                        tileSize, 0,
                        tileSize, tileSize,
                        0,        tileSize,
                });

                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);
            }
        }

        addObject(player);
        //TODO: enemies activate too
        for(int i = 0; i < enemies.length; i++){
            addObject(enemies[i]);
        }
    }



    public void handleGameplayCamera(GameCanvas canvas) {
        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            canvas.getCamera().zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            canvas.getCamera().zoom -= 0.02;
        }

        float camZone_x = 1;
        float camZone_y = 1;

        if (Math.abs(canvas.getCamera().position.x - player.getX()) > camZone_x) {
            if (canvas.getCamera().position.x > player.getX())
                canvas.setGameplayCamera(player.getX()+camZone_x, canvas.getCamera().position.y, cameraWidth, cameraHeight);

            else canvas.setGameplayCamera(player.getX()-camZone_x, canvas.getCamera().position.y, cameraWidth, cameraHeight);
        }

        if (Math.abs(canvas.getCamera().position.y - player.getY()) > camZone_y) {
            if (canvas.getCamera().position.y > player.getY())
                canvas.setGameplayCamera(canvas.getCamera().position.x, player.getY()+camZone_y, cameraWidth, cameraHeight);

            else canvas.setGameplayCamera(canvas.getCamera().position.x, player.getY()-camZone_y, cameraWidth, cameraHeight);
        }

    }


}
