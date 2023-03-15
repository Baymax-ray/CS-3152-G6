package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
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

//    private final TextureRegion backgroundTexture;

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

    private UIOverlay uiElements;

    //#endregion


    //#region NONFINAL FIELDS
    private World world;

    /** Queue for adding objects */
    private PooledList<Obstacle> addQueue;

    private PooledList<Obstacle> objects;

    /**
     * Whether this level has been completed
     */
    private boolean isCompleted;

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

    //#endregion

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
//        canvas.getCamera().position.setZero(); // set to some other position to follow player;
//        canvas.getCamera().setToOrtho(false, cameraWidth, cameraHeight);
        canvas.setGameplayCamera(player.getX(), player.getY(), cameraWidth, cameraHeight);




        float camZone_x = 80;
        float camZone_y = 60;
        canvas.getCamera().zoom = 1.0f;
//        if (Math.abs(canvas.getCamera().position.x - player.getX() *32) > camZone_x){
//            if (canvas.getCamera().position.x > player.getX() *32)
//                canvas.getCamera().position.set(player.getX() * 32 + camZone_x, canvas.getCamera().position.y,0);
//
//            else canvas.getCamera().position.set(player.getX() * 32 - camZone_x, canvas.getCamera().position.y,0);
//        }
//
//
//        if (Math.abs(canvas.getCamera().position.y - player.getY() *32) > camZone_y){
//            if (canvas.getCamera().position.y > player.getY() *32)
//                canvas.getCamera().position.set(canvas.getCamera().position.x, player.getY() * 32 + camZone_y,0);
//
//            else canvas.getCamera().position.set(canvas.getCamera().position.x, player.getY() * 32 - camZone_y,0);
//        }

//        canvas.draw(backgroundTexture, 0, 0);

        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < tilemap.length; x++) {
                int tileId = row[x];
                if (tileId < 0) continue;
                Tile tile = tiles[tileId];
                canvas.draw(tile.getTexture(), Color.WHITE, 0, 0, tileToLevelCoordinatesX(x), tileToLevelCoordinatesY(y), 0, tileSize / tile.getTexture().getRegionWidth(), tileSize / tile.getTexture().getRegionHeight());
            }
        }
        player.draw(canvas);
        for (Obstacle obj : objects) {
            obj.draw(canvas);
        }

        //canvas.setOverlayCamera();
        //uiElements.draw(canvas);
    }

    public void activatePhysics() {
        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < tilemap.length; x++) {
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
        player.activatePhysics(world);
        //TODO: enemies activate too
    }


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
        this.uiElements = new UIOverlay(json.get("player"), assets, player.getHearts(), player.getSpirit());



        // TODO: need more than 1 ideally
        //this is a temporary code!!
        this.enemies = new Enemy[0];
        //this.enemies[0]=new Enemy(json.get("enemyOne"),assets);

        this.bodyDef = new BodyDef();
        this.bodyDef.type = BodyDef.BodyType.StaticBody;
        this.bodyDef.active = false;

        this.fixtureDef = new FixtureDef();

        this.addQueue = new PooledList<>();
        this.objects = new PooledList<>();
    }

}
