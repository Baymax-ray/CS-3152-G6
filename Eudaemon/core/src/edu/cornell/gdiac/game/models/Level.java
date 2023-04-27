package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.UIOverlay;
import edu.cornell.gdiac.game.obstacle.EffectObstacle;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

public class Level {
    private MyGridGraph gridGraph;
    //#region FINAL FIELDS
    private final Player player;
    private ArrayList<Enemy> enemies ;

    private ArrayList<Spike> spikes;

    private ArrayList<Billboard> billboards;

    private Exit exit;

    private EffectPool effectPool;

    // NOTE: the natural way of viewing a 2d array is flipped for the map. tilemap[0] is the top of the map. need fancy
    // conversions between the two spaces
    private final int[][] tilemap; // value represents the index of the tile in the tiles array

    /**
     * The list of background 1 tiles in this level.
     */
    private final int[][] tilemapBG1;

    /**
     * The list of background 12 tiles in this level.
     */
    private final int[][] tilemapBG2;

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

    /**
     * How far the player can move before the camera starts moving, x-coords
     */
    private float camZoneX;

    /**
     * How far the player can move before the camera starts moving, y-coords
     */
    private float camZoneY;

    private final float gravity;

    //#endregion


    //#region NONFINAL FIELDS
    private final UIOverlay uiElements;
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
    //private MyGridGraph gridGraph;

    //#endregion

    //#region GETTERS & SETTERS

    public Exit getExit() {
        return exit;
    }

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

    public Billboard[] getBillboards() {
        Billboard[] result = new Billboard[billboards.size()];
        return billboards.toArray(result);
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

    public MyGridGraph getGridGraph(){
        return gridGraph;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public EffectPool getEffectPool() {
        return effectPool;
    }

    public void setEffectPool(EffectPool effectPool) {
        this.effectPool = effectPool;
    }

    //#endregion

    public class MyConnection<MyNode> implements Connection<MyNode> {
        protected MyNode fromNode;
        protected MyNode toNode;
        protected float cost;

        public MyConnection (MyNode fromNode, MyNode toNode,float cost) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.cost=cost;
        }

        @Override
        public float getCost () {
            return cost;
        }
        @Override
        public MyNode getFromNode () {
            return fromNode;
        }

        @Override
        public MyNode getToNode () {
            return toNode;
        }

    }
    /**
     * x and y stored in tile coordinates
     * same with tilemap
     */
    public class MyNode{
        private final int x;
        private final int y;
        private boolean passable;
        public MyNode(int x, int y){
            this.x=x;
            this.y=y;
        }

        /**
         * true is this node can be passed
         */
        public void setPassable(boolean passable) {
            this.passable = passable;
        }

        /**
         * @return true is this node can be passed
         */
        public boolean isPassable() {
            return passable;
        }

        /**
         * @return x in tile coordinates
         */
        public int getX() {
            return x;
        }

        /**
         * @return y in tile coordinates
         */
        public int getY() {
            return y;
        }
    }
    public class MyGridGraph implements IndexedGraph<MyNode> {
        private final int width, height;
        private MyNode[][] nodes;
        public MyGridGraph(int width, int height,int[][]tilemap) {
            this.width = width;
            this.height = height;
            nodes = new MyNode[height][width];
            for (int y=0;y<height;y++){
                for (int x = 0; x < width; x++) {
                    MyNode node=new MyNode(x,y);
                    node.setPassable(tilemap[y][x]<=0);
                    nodes[y][x]=node;
                }
            }

        }
        public int getHeight(){
            return height;
        }
        @Override
        public int getIndex(MyNode node) {
            int x= node.getX();
            int y= node.getY();
            return y*width+x;
        }

        public int getNodeCount(){
            return width * height;
        }
        public MyNode getNode(int index) {
            int x = index % width;
            int y = index / width;
            return nodes[y][x];
        }
        public MyNode getNode(int x, int y) {
            return nodes[y][x];
        }

        @Override
        public Array<Connection<MyNode>> getConnections(MyNode fromNode) {
            int x= fromNode.getX();
            int y= fromNode.getY();
            Array<Connection<MyNode>> connections=new Array<>();
            if (x>0) {
                MyNode toNode=getNode(x-1,y);
                if (toNode.isPassable()) {
                    connections.add(new MyConnection<>(fromNode,toNode,1f));
                    // Add diagonal neighboring nodes
                    if (y > 0){
                        toNode=getNode(x-1,y-1);
                        if (toNode.isPassable()) {
                            connections.add(new MyConnection<>(fromNode,toNode,1.4f));
                        }
                    }
                    if (y < height - 1){
                        toNode=getNode(x-1,y+1);
                        if (toNode.isPassable()) {
                            connections.add(new MyConnection<>(fromNode,toNode,1.4f));
                        }
                    }
                }
            }
            if (x < width - 1){
                MyNode toNode=getNode(x+1,y);
                if (toNode.isPassable()) {
                    connections.add(new MyConnection<>(fromNode,toNode,1f));
                    // Add diagonal neighboring nodes
                    if (x < width - 1 && y > 0){
                        toNode=getNode(x+1,y-1);
                        if (toNode.isPassable()) {
                            connections.add(new MyConnection<>(fromNode,toNode,1.4f));
                        }
                    }
                    if (x < width - 1 && y < height - 1){
                        toNode=getNode(x+1,y+1);
                        if (toNode.isPassable()) {
                            connections.add(new MyConnection<>(fromNode,toNode,1.4f));
                        }
                    }
                }
            }
            if (y > 0){
                MyNode toNode=getNode(x,y-1);
                if (toNode.isPassable()) {
                    connections.add(new MyConnection<>(fromNode,toNode,1f));
                }
            }
            if (y < height - 1){
                MyNode toNode=getNode(x,y+1);
                if (toNode.isPassable()) {
                    connections.add(new MyConnection<>(fromNode,toNode,1f));
                }
            }

            return connections;
        }
    }
    public Level(JsonValue json, Tile[] tiles, AssetDirectory assets) {
        this.tiles = tiles;
        //this.billboard = new Billboard();
        String levelName = json.getString("level");
        camZoneX = json.getFloat("camZoneX");
        camZoneY = json.getFloat("camZoneY");

        JsonValue levelJson = assets.getEntry(levelName,  JsonValue.class);

        // Access the JsonValues for each layer in the tilemap
        JsonValue layerArray = levelJson.get("layers");

        // Create a dictionary to contain the names of the layers and their JsonValues
        Hashtable<String,JsonValue> layerData = new Hashtable<>();

        // Iterate through the JsonValue layer array and perform operations as needed
        for (JsonValue item : layerArray) {
            // Add the name of the layer to the hashset as the key with a value of item
            layerData.put(item.getString("name"), item);
        }


        // Create the tilemap (foreground tiles)
        JsonValue tilesFG = layerData.get("TileLayerFG");
        JsonValue tilesFGData = tilesFG.get("data");

        // Get the width and height of the tilemap in tiles
        int widthInTiles = tilesFG.getInt("width");
        int heightInTiles = tilesFG.getInt("height");

        this.tilemap = new int[heightInTiles][widthInTiles];
        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                tilemap[y][x] = tilesFGData.getInt(y*widthInTiles + x) - 1;
            }
        }
        gridGraph= new MyGridGraph(widthInTiles,heightInTiles,this.tilemap);

        // Create the tileset
        texturePaths = new HashMap<>();
        JsonValue tileset = assets.getEntry("tileset",  JsonValue.class);
        JsonValue tileList = tileset.get("tiles");
        int tileListLength = tileset.getInt("tileCount");
        for (int i= 0; i < tileListLength; i++) {
            JsonValue t = tileList.get(i);
            String name = tileList.get(i).getString("image").substring(6);
            //Avoiding adding enemies and objects
            if (!(name.substring(0,7).equals("Enemies"))){
                TextureRegion tileTexture = new TextureRegion(assets.getEntry("tiles:" + name, Texture.class));
                texturePaths.put(t.getInt("id"),tileTexture);
            }
        }

        for (int[] row : tilemap) {
            for (int tileId : row) {
                if (tileId <= 0) continue;
                tiles[tileId].setTexture(texturePaths.get(tileId));
            }
        }

        this.tileSize = json.getFloat("tileSize");
        this.cameraWidth = json.getFloat("cameraWidth");
        this.cameraHeight = json.getFloat("cameraHeight");
        this.gravity = json.getFloat("gravity");
        this.background = new TextureRegion(assets.getEntry("background:city", Texture.class));


        // Define the world
        this.world = new World(new Vector2(0, gravity), true);

//        String backgroundAsset = json.getString("backgroundAsset");
//        this.backgroundTexture = assets.get(backgroundAsset);

        //#region Enemies and Objects
        this.enemies = new ArrayList<Enemy>();
        this.spikes = new ArrayList<Spike>();
        this.billboards = new ArrayList<Billboard>();

        int startX = 0;
        int startY = 0;
        JsonValue objectLayer = layerData.get("ObjectLayer");
        JsonValue objects = objectLayer.get("objects");

        for (JsonValue object : objects) {
            if (object.getString("type").equals("Enemy")) {
                float x = (int) (object.getInt("x") / 32);
                float y = heightInTiles -1-(int) (object.getInt("y") / 32);
                enemies.add(new Enemy(object, assets,x,y));
            }
            else if (object.getString("name").equals("Spike")) {
                float x = (int) (object.getInt("x") / 32);
                float y = heightInTiles - (int) (object.getInt("y") / 32);
                spikes.add(new Spike(object, assets,x,y));
            }
            else if (object.getString("name").equals("Billboard")) {
                float x = (int) (object.getInt("x") / 32);
                float y = heightInTiles - (int) (object.getInt("y") / 32);
                billboards.add(new Billboard(object, assets,x,y));
            }
            else if (object.getString("name").equals("StartingPoint")) {
                startX = (int) (object.getInt("x") / 32) + 1;
                startY = heightInTiles - (int) (object.getInt("y") / 32);
            } else if (object.getString("name").equals("Exit")) {
                float x = (float) object.getInt("x") / 32;
                float y = heightInTiles - (float) object.getInt("y") / 32;
                float width = object.getFloat("width") / 64;
                float height = object.getFloat("height") / 50;
                exit = new Exit(object, assets, x, y, width, height);
            }
        }

        effectPool = new EffectPool();

        //#endregion

        JsonValue playerData = assets.getEntry("sharedConstants", JsonValue.class).get("Player");
        this.player = new Player(json.get("player"), assets, startX, startY);
        uiElements = new UIOverlay(playerData, assets);

        // Create the tilemap (background tiles 1)
        JsonValue tilesBG1 = layerData.get("TileLayerBG");
        JsonValue tilesBG1Data = tilesBG1.get("data");
        this.tilemapBG1 = new int[heightInTiles][widthInTiles];

        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                tilemapBG1[y][x] = tilesBG1Data.getInt(y*widthInTiles + x) - 1;
            }
        }

        for (int[] row : tilemapBG1) {
            for (int tileId : row) {
                if (tileId <= 0) continue;
                tiles[tileId].setTexture(texturePaths.get(tileId));
            }
        }

        // Create the tilemap (background tiles 1)
        JsonValue tilesBG2 = layerData.get("TileLayerBG2");
        JsonValue tilesBG2Data = tilesBG2.get("data");
        this.tilemapBG2 = new int[heightInTiles][widthInTiles];

        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                tilemapBG2[y][x] = tilesBG2Data.getInt(y*widthInTiles + x) - 1;
            }
        }

        for (int[] row : tilemapBG2) {
            for (int tileId : row) {
                if (tileId <= 0) continue;
                tiles[tileId].setTexture(texturePaths.get(tileId));
            }
        }


        this.bodyDef = new BodyDef();
        this.bodyDef.type = BodyDef.BodyType.StaticBody;
        this.bodyDef.active = false;

        this.fixtureDef = new FixtureDef();

        this.addQueue = new PooledList<>();
        this.objects = new PooledList<>();

        this.debug = false;
        this.isCompleted = false;
    }

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return the tile
     */
    public int tileAt(float x, float y) {
        //return tiles[tilemap[levelToTileCoordinatesY(y)][levelToTileCoordinatesX(x)]];
        return tilemap[levelToTileCoordinatesY(y)][levelToTileCoordinatesX(x)];
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
        int ny = levelToTileCoordinatesY(y);

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
        return tilemap.length - 1 - (int) Math.floor(y/ this.tileSize);
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
     * @param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }

    public boolean inAddQueue(Obstacle obj){
        return addQueue.contains(obj);
    }

    public void removeQueuedObject(Obstacle obj){
        addQueue.remove(obj);
    }


    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    public void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        if (obj != null) {
            objects.add(obj);
            obj.activatePhysics(world);
        }
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
                if (obj instanceof EffectObstacle) {
                    effectPool.free((EffectObstacle) obj);
                }
            } else {
                // Note that update is called last!
                obj.update(delta);
            }
        }

        for (int i = 0; i < billboards.size(); i++) {
            billboards.get(i).aggregateStringCompleteness(delta);
        }
        //billboard.aggregateStringCompleteness(delta);


    }

    public void draw(GameCanvas canvas) {

        canvas.begin();

        //handleGameplayCamera(canvas);
        canvas.getCameraController().handleGameplayCamera(canvas, this);
        float cam_x = canvas.getCamera().position.x;
        float cam_y = canvas.getCamera().position.y;


//        canvas.draw(background, 0, 0);
        canvas.draw(background, Color.WHITE, 0, 0, -3, 0, 0, 0.03F, 0.03F);
//        canvas.draw(background, Color.CLEAR, background.getRegionWidth()/2, background.getRegionHeight()/2, 0, 0, 1 / background.getRegionWidth(), 1/ background.getRegionHeight());

        //Drawing background 2 tiles
        for (int y = 0; y < tilemapBG2.length; y++) {
            int[] row = tilemapBG2[y];
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

        //Drawing background 1 tiles
        for (int y = 0; y < tilemapBG1.length; y++) {
            int[] row = tilemapBG1[y];
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

        //Drawing foreground tiles
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
            if (obj.getClass().equals(Billboard.class) || obj.getClass().equals(Exit.class)) {
                obj.draw(canvas);
            }
        }
        for (Obstacle obj : objects) {
            if (!obj.getClass().equals(Billboard.class) && !obj.getClass().equals(Exit.class)) {
                obj.draw(canvas);
            }
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
        boolean alreadyDisplay = false;
        for (int i = 0; i < billboards.size(); i++) {
            if (billboards.get(i).isDisplay() && !alreadyDisplay) {
                billboards.get(i).displayDialog(canvas);
                alreadyDisplay = true;
            }

        }
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

        //TODO: enemies activate too
        for(int i = 0; i < spikes.size(); i++){
            addObject(spikes.get(i));
        }

        for(int i = 0; i < billboards.size(); i++){
            addObject(billboards.get(i));
        }

        for(int i = 0; i < enemies.size(); i++){
            addObject(enemies.get(i));
        }

        addObject(player);
        addObject(exit);
    }



    public void handleGameplayCamera(GameCanvas canvas) {
        if (player.isRemoved()) return;

        if (Gdx.input.isKeyPressed(Input.Keys.O)) {
            canvas.getCamera().zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            canvas.getCamera().zoom -= 0.02;
        }

        float camZone_x = camZoneX;
        float camZone_y = camZoneY;

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

        if (canvas.getCamera().position.x < cameraWidth/2)
            canvas.setGameplayCamera(cameraWidth/2, canvas.getCamera().position.y, cameraWidth, cameraHeight);
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
        world.setContactListener(null);
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        gridGraph = null;
        player.dispose();
        objects.clear();
        spikes.clear();
        billboards.clear();
        effectPool.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        world  = null;
    }

    public float getCameraWidth() { return cameraWidth; }
    public float getCameraHeight() { return cameraHeight; }
    public float getCamZoneX() { return camZoneX; }
    public float getCamZoneY() { return camZoneY; }



}
