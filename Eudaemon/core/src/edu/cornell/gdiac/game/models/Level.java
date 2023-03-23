package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.UIOverlay;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import edu.cornell.gdiac.game.obstacle.SwordWheelObstacle;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Level {

    //#region FINAL FIELDS
    private final Player player;
    private ArrayList<Enemy> enemies ;

    // NOTE: the natural way of viewing a 2d array is flipped for the map. tilemap[0] is the top of the map. need fancy
    // conversions between the two spaces
    private final int[][] tilemap; // value represents the index of the tile in the tiles array
    private final Tile[] tiles;
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    //private final TextureRegion backgroundTexture;

    /**
     * A HashMap that stores texture paths with integer keys.
     * Key: Integer representing the texture ID.
     * Value: String representing the path to the texture file.
     */
    private final HashMap<Integer, TextureRegion> texturePaths;
    /** Background texture for start-up */
    private final TextureRegion background;

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
    private final UIOverlay uiElements;
    private final World world;

    /** Queue for adding objects */
    private final PooledList<Obstacle> addQueue;

    private final PooledList<Obstacle> objects;

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
        Enemy[] result = new Enemy[enemies.size()];
        return enemies.toArray(result);
    }
    public float gettileSize(){
        return tileSize;
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
        String levelName = json.getString("level");
        JsonValue levelJson = assets.getEntry(levelName,  JsonValue.class);
        int widthInTiles = levelJson.getInt("width");
//        int heightInTiles = json.getInt("height");
        int heightInTiles = json.getInt("levelHeight");
        JsonValue layers = levelJson.get("layers").get("data");
        this.tilemap = new int[heightInTiles][widthInTiles];
        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                tilemap[y][x] = layers.getInt(y*widthInTiles + x) - 1;
            }
        }
        texturePaths = new HashMap<>();
        JsonValue tileset = assets.getEntry("tileset",  JsonValue.class);
        JsonValue tileList = tileset.get("tiles");
        int tileListLength = tileset.getInt("tileCount");
        for (int i= 0; i < tileListLength; i++) {
            JsonValue t = tileList.get(i);
            TextureRegion tileTexture = new TextureRegion(assets.getEntry("tiles:" + t.getString("image"), Texture.class));
            texturePaths.put(t.getInt("id"),tileTexture);
        }

        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < row.length; x++) {
                int tileId = row[x];
                if (tileId <= 0) continue;
                tiles[tileId].setTexture(texturePaths.get(tileId));
            }
        }

        this.tileSize = json.getFloat("tileSize");
        this.cameraWidth = json.getFloat("cameraWidth");
        this.cameraHeight = json.getFloat("cameraHeight");
        this.gravity = json.getFloat("gravity");
        this.background = new TextureRegion(assets.getEntry("background:city", Texture.class));

//        String backgroundAsset = json.getString("backgroundAsset");
//        this.backgroundTexture = assets.get(backgroundAsset);

        this.world = new World(new Vector2(0, gravity), true);
        JsonValue playerData = assets.getEntry("sharedConstants", JsonValue.class).get("Player");
        this.player = new Player(json.get("player"), assets);
        uiElements = new UIOverlay(playerData, assets);


        this.enemies = new ArrayList<Enemy>();
        //System.out.println(json.get("enemy").get(0).toString());
        JsonValue.JsonIterator enemyIterator = json.get("enemy").iterator();
        while(enemyIterator.hasNext()){
            enemies.add(new Enemy(enemyIterator.next(), assets));
        }

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
        return tiles[tilemap[levelToTileCoordinatesY(y)][levelToTileCoordinatesX(x)]];
    }

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return whether the point is in an air tile.
     */
    public boolean isAirAt(float x, float y) {
        int nrow = tilemap.length;
        int ncol = tilemap[0].length;
        int nx=levelToTileCoordinatesX(x);
        //TODO: 3-21
        int ny = levelToTileCoordinatesY(y);
        //int ny=levelToTileCoordinatesY(y);

        if (nx<0 || nx>(ncol-1) ||ny<0 || ny>(nrow-1) ){
            return false;
        }
        return tilemap[ny][nx] <= 0;
    }
    public boolean isAirAt(int x, int y){
        int nrow = tilemap.length;
        int ncol = tilemap[0].length;
        if (x<0 || x>(ncol-1) ||y<0 || y>(nrow-1) ){
            return false;
        }
        return tilemap[y][x] <= 0;

    }


    public int levelToTileCoordinatesX(float x) {
        return (int) Math.floor(x / this.tileSize);
    }

    public int levelToTileCoordinatesY(float y) {
        //System.out.println("tilemap length is" + tilemap.length + "tilesize is "+ this.tileSize);
//        return (int) Math.floor(y / this.tileSize);
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

        handleSpirit();

    }

    public void draw(GameCanvas canvas) {

        canvas.begin();

        handleGameplayCamera(canvas);
        float cam_x = canvas.getCamera().position.x;
        float cam_y = canvas.getCamera().position.y;


        canvas.draw(background, -50F, 0);
//        canvas.draw(background, Color.CLEAR, background.getRegionWidth()/2, background.getRegionHeight()/2, 0, 0, 1 / background.getRegionWidth(), 1/ background.getRegionHeight());

        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < row.length; x++) {
                int tileId = row[x];
                if (tileId <= 0) continue;
                Tile tile = tiles[tileId];
                if (tile.getTexture() == null){ //running into glitch
                    continue;
                }
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
        uiElements.draw(canvas, player.getHearts(), player.getSpirit());
        canvas.end();

        canvas.setGameplayCamera(cam_x,cam_y, cameraWidth, cameraHeight);

    }

    public void activatePhysics() {
        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < row.length; x++) {
                int tileId = row[x];
                if (tileId <= 0) continue;
                Tile tile = tiles[tileId];
                bodyDef.position.x = tileToLevelCoordinatesX(x);
                bodyDef.position.y = tileToLevelCoordinatesY(y);
                bodyDef.active = true;
                Body body = world.createBody(bodyDef);
                body.setUserData(tile);

                ChainShape shape = new ChainShape();
                shape.createLoop(new float[]{
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
        for(int i = 0; i < enemies.size(); i++){
            addObject(enemies.get(i));
        }
    }



    public void handleGameplayCamera(GameCanvas canvas) {
        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            canvas.getCamera().zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            canvas.getCamera().zoom -= 0.02;
        }

        float camZone_x = 5;
        float camZone_y = 5;

//        System.out.println("camera: "+canvas.getCamera().position.x+" "+canvas.getCamera().position.y);
//        System.out.println("player: "+player.getX()+" "+player.getY());

        if (Math.abs(canvas.getCamera().position.x - player.getX()) > camZone_x) {
            if (canvas.getCamera().position.x > player.getX()) {

                canvas.setGameplayCamera(player.getX()+camZone_x, canvas.getCamera().position.y, cameraWidth, cameraHeight);
            }

            else {

                canvas.setGameplayCamera(player.getX()-camZone_x, canvas.getCamera().position.y, cameraWidth, cameraHeight);
            }
        }

        if (Math.abs(canvas.getCamera().position.y - player.getY()) > camZone_y) {
            if (canvas.getCamera().position.y > player.getY())
                canvas.setGameplayCamera(canvas.getCamera().position.x, player.getY()+camZone_y, cameraWidth, cameraHeight);

            else canvas.setGameplayCamera(canvas.getCamera().position.x, player.getY()-camZone_y, cameraWidth, cameraHeight);
        }

        //System.out.println("camera: "+canvas.getCamera().position.x+" "+canvas.getCamera().position.y);
        //System.out.println("player: "+player.getX()+" "+player.getY());
    }

    public void handleSpirit() {
        if (player.getForm()==1) {
            player.decreaseSpirit();
            if (player.getSpirit()==0) player.setForm();
        }

        //TODO: What is this number?
        double shortestDist = 99999;
        for (int i = 0; i < enemies.size(); i++) {
            double dist = Math.sqrt(Math.pow(player.getX() - enemies.get(i).getX(), 2) + Math.pow(player.getY() - enemies.get(i).getY(), 2));
            if (dist < shortestDist) shortestDist = dist;

            if (dist < player.getHitDist() && !player.isHit()) {
                player.setHit(true);
                player.hitByEnemy();
            }
            if (player.isAttacking() && dist < player.getAttackDist() &&
                    (player.isFacingRight() && player.getX() < enemies.get(i).getX() ||
                            !player.isFacingRight() && player.getX() > enemies.get(i).getX())) {
                enemies.get(i).hitBySword(player);
                System.out.println("removing enemy" + enemies.get(i).getType());
                enemies = removeEnemy(enemies, i);
            }

        }
        if (shortestDist < player.getSpiritIncreaseDist() && player.getForm()==0  ){
            player.increaseSpirit();
            Vector2 scale = new Vector2(5f,5f);
            SwordWheelObstacle spiritAnimate = new SwordWheelObstacle(player.getX(), player.getY(), player.getSpiritDrainSpriteSheet().getRegionWidth()/400F, player, player.getAttackLifespan(), 5f, scale, player.getSpiritDrainSpriteSheet());
            addQueuedObject(spiritAnimate);
        }

    }

    /**
     * Helper method that removes an enemy
     * Intended to fix the bug where players still gain soul after an enemy is killed
     *
     * @return the new array of enemies where a given enemy is removed
     */
    private ArrayList<Enemy> removeEnemy(ArrayList<Enemy> arr, int index) {
        if (arr == null || index < 0 || index > arr.size()) return arr;
        ArrayList<Enemy> result = new ArrayList<Enemy>();
        for (int i = 0, k = 0; i < arr.size(); i++) {
            if (i == index) {
                continue;
            }
            result.add(arr.get(i));
        }
        return result;
    }

    public void dispose() {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        world  = null;
    }


}
