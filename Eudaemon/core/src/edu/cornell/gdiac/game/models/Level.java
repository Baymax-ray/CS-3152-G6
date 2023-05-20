package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.UIOverlay;
import edu.cornell.gdiac.game.obstacle.EffectObstacle;
import edu.cornell.gdiac.game.obstacle.Obstacle;
import edu.cornell.gdiac.util.Controllers;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

public class Level {
    private final float scaleforBackground;
    private int startX;
    private int startY;
    private MyGridGraph gridGraph;
    //#region FINAL FIELDS
    private final Player player;
    private ArrayList<Enemy> enemies ;

    private ArrayList<Spike> spikes;

    private ArrayList<Billboard> billboards;

    private ArrayList<TutorialArea> tutorialAreas;

    private Array<Controller> controllers;

    private Controller controller;

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
    private final TextureRegion background_L1;
    private final TextureRegion background_L2;
    private final TextureRegion background_L3;
    private final TextureRegion background_L4;
    private final TextureRegion background_L5;

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
    private final float camZoneX;

    /**
     * How far the player can move before the camera starts moving, y-coords
     */
    private final float camZoneY;

    private final float gravity;

    private final int widthInTiles;
    private final int heightInTiles;



    //#endregion


    //#region NONFINAL FIELDS
//    private final UIOverlay uiElements;
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

    /**The drawing x-origin of the background */
    private float backgroundOx;
    /** The drawing y-origin of the background */
    private float backgroundOy;
    //private MyGridGraph gridGraph;

    private boolean shouldShakeCamera;

    private int cameraShakeType;

    private boolean cameraShakeOn;

    private boolean cameraShaked;

    private AssetDirectory assets;

    public float levelDifficulty;

    private UIOverlay uiElements;


    public boolean normalDifficulty;
    public boolean hardDifficulty;
    public boolean veteranDifficulty;
    public boolean settingsChanged;

    private Vector3 origCameraPosition;


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

    public TutorialArea[] getTutorialAreas() {
        TutorialArea[] result = new TutorialArea[tutorialAreas.size()];
        return tutorialAreas.toArray(result);
    }
    public float gettileSize(){
        return tileSize;
    }

    public int getWidthInTiles() { return widthInTiles; }
    public int getHeightInTiles() { return heightInTiles; }
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

    public boolean isNormalDifficulty(){
        return normalDifficulty;
    }
    public boolean isHardDifficulty(){
        return hardDifficulty;
    }
    public boolean isVeteranDifficulty(){
        return veteranDifficulty;
    }
    public void setDifficulty(int difficulty){
        if(difficulty == 0){setNormalDifficulty(true);}
        else if (difficulty ==1){setHardDifficulty(true);}
        else if (difficulty == 2){setVeteranDifficulty(true);}
    }
    public void setNormalDifficulty(boolean bool){
        normalDifficulty = bool;
        hardDifficulty = !bool;
        veteranDifficulty = !bool;
        levelDifficulty = 5;
        player.setHearts(levelDifficulty);
        settingsChanged = true;
    }
    public void setHardDifficulty(boolean bool){
        hardDifficulty = bool;
        veteranDifficulty = !bool;
        normalDifficulty = !bool;
        levelDifficulty = 4;
        player.setHearts(levelDifficulty);
        settingsChanged = true;
    }
    public void setVeteranDifficulty(boolean bool){
        veteranDifficulty = bool;
        hardDifficulty = !bool;
        normalDifficulty = !bool;
        levelDifficulty = 3;
        player.setHearts(levelDifficulty);
        settingsChanged = true;
    }

    public float getLevelDifficulty() {
        return levelDifficulty;
    }
    public boolean isSettingsChanged(){
        return settingsChanged;
    }

    public void setCameraShakeOn(boolean value) { this.cameraShakeOn = value; }
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
        public int getWidth() {return width;}
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
                        MyNode sideNode=getNode(x,y-1);
                        if (toNode.isPassable()&& sideNode.isPassable()) {
                            connections.add(new MyConnection<>(fromNode,toNode,1.4f));
                        }
                    }
                    if (y < height - 1){
                        toNode=getNode(x-1,y+1);
                        MyNode sideNode=getNode(x,y+1);
                        if (toNode.isPassable()&& sideNode.isPassable()) {
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
                    if (y > 0){
                        toNode=getNode(x+1,y-1);
                        MyNode sideNode=getNode(x,y-1);
                        if (toNode.isPassable()&& sideNode.isPassable()) {
                            connections.add(new MyConnection<>(fromNode,toNode,1.4f));
                        }
                    }
                    if (y < height - 1){
                        toNode=getNode(x+1,y+1);
                        MyNode sideNode=getNode(x,y+1);
                        if (toNode.isPassable()&& sideNode.isPassable()) {
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
    public Level(String levelName, Tile[] tiles, AssetDirectory assets) {
        this.tiles = tiles;
        //this.billboard = new Billboard();
        this.normalDifficulty = true;
        this.hardDifficulty = false;
        this.veteranDifficulty = false;
        this.settingsChanged = false;

        if(veteranDifficulty){
            levelDifficulty = 3;
            uiElements = new UIOverlay(assets.getEntry("sharedConstants", JsonValue.class).get("Player"), assets, this);
        }
        else if(hardDifficulty){
            levelDifficulty = 4;
            uiElements = new UIOverlay(assets.getEntry("sharedConstants", JsonValue.class).get("Player"), assets, this);
        }
        else{
            levelDifficulty = 5;
            uiElements = new UIOverlay(assets.getEntry("sharedConstants", JsonValue.class).get("Player"), assets, this);
        }

        controllers = Controllers.get().getControllers();
        if (controllers.size > 0) controller = controllers.first();

        JsonValue levelJson = assets.getEntry(levelName,  JsonValue.class);

        // go through properties
        Hashtable<String, JsonValue> properties = new Hashtable<>();
        for (JsonValue item : levelJson.get("properties")) {
            properties.put(item.getString("name"), item);
        }

        this.gravity = properties.get("gravity").getFloat("value");
        this.camZoneX = properties.get("camZoneX").getFloat("value");
        this.camZoneY = properties.get("camZoneY").getFloat("value");
        this.cameraWidth = properties.get("cameraWidth").getFloat("value");
        this.cameraHeight = properties.get("cameraHeight").getFloat("value");
        this.tileSize = properties.get("tileSize").getFloat("value");
        this.scaleforBackground = properties.get("backgroundScale").getFloat("value");
        this.backgroundOx = properties.get("backgroundOx").getFloat("value");
        this.backgroundOy = properties.get("backgroundOy").getFloat("value");


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
        widthInTiles = tilesFG.getInt("width");
        heightInTiles = tilesFG.getInt("height");

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
//                System.out.println(name);
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

        this.background_L1 = new TextureRegion(assets.getEntry("background:L1", Texture.class));
        //get the size of the background
        //int background_L1Width = background_L1.getRegionWidth();
        //System.out.println("background_L1Width: " + background_L1Width);
        //int background_L1Height = background_L1.getRegionHeight();
        //System.out.println("background_L1Height: " + background_L1Height);
        this.background_L2 = new TextureRegion(assets.getEntry("background:L2", Texture.class));
        this.background_L3 = new TextureRegion(assets.getEntry("background:L3", Texture.class));
        this.background_L4 = new TextureRegion(assets.getEntry("background:L4", Texture.class));
        this.background_L5 = new TextureRegion(assets.getEntry("background:L5", Texture.class));

        // Define the world
        this.world = new World(new Vector2(0, gravity), true);

        //#region Enemies and Objects
        this.enemies = new ArrayList<>();
        this.spikes = new ArrayList<>();
        this.billboards = new ArrayList<>();
        this.tutorialAreas = new ArrayList<>();


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
            else if (object.getString("name").equals("TutorialArea")) {
                float x = (int) (object.getInt("x") / 32);
                float y = heightInTiles - (int) (object.getInt("y") / 32);
                tutorialAreas.add(new TutorialArea(object, assets,x,y));
            }
            else if (object.getString("name").equals("StartingPoint")) {
                startX = (int) (object.getInt("x") / 32) + 1;
                startY = heightInTiles - (int) (object.getInt("y") / 32);
            }
        }

        effectPool = new EffectPool();

        //#endregion

        // Create the player
        this.player = new Player(assets, startX, startY, levelDifficulty);
//        JsonValue playerData = assets.getEntry("sharedConstants", JsonValue.class).get("Player");
        this.assets = assets;
//        uiElements = new UIOverlay(playerData, assets);

        for (JsonValue object : objects) {
            if (object.getString("name").equals("Exit")) {
                float x = (float) object.getInt("x") / 32;
                float y = heightInTiles - (float) object.getInt("y") / 32;
                float width = 1.15f;
                float height = 1.7f;
                exit = new Exit(object, assets, x, y, width, height, player);
            }
        }

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
        this.shouldShakeCamera = false;
        this.cameraShakeOn = true;
        this.cameraShaked = false;

        this.origCameraPosition = new Vector3();
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
     * Adds a physics object in to the insertion queue.
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     * @param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }


    /**
     * Immediately adds the object to the physics world
     * @param obj The object to add
     */
    public void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
            objects.add(obj);
            obj.activatePhysics(world);
    }
    /**
     * Returns true if the object is in bounds.
     * <p>
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

        for (Billboard billboard : billboards) {
            billboard.aggregateStringCompleteness(delta);
        }
        //billboard.aggregateStringCompleteness(delta);


    }

    public void draw(GameCanvas canvas) {

        canvas.begin();

        //handleGameplayCamera(canvas);
        canvas.getCameraController().handleGameplayCamera(canvas, this);
        float cam_x = canvas.getCamera().position.x;
        float cam_y = canvas.getCamera().position.y;

        float px;
        float py;
//        if (!cameraShaked) {
//            px=canvas.getCamera().position.x;
//            py=canvas.getCamera().position.y;
//        } else if (canvas.getCameraController().getCameraShaker().isCameraShaking()){
//            px = canvas.getCameraController().getCameraShaker().getStartPosition().x;
//            py = canvas.getCameraController().getCameraShaker().getStartPosition().y;
//        } else if (cameraShaked && canvas.getCamera().position.equals(origCameraPosition)){
//            px = origCameraPosition.x;
//            py = origCameraPosition.y;
//        } else {
//            px=canvas.getCamera().position.x;
//            py=canvas.getCamera().position.y;
//        }
//        if (canvas.getCameraController().getCameraShaker().isCameraShaking()) {
//            float deltaX = canvas.getCameraController().getCamera().position.x - origCameraPosition.x;
//            float deltaY = canvas.getCameraController().getCamera().position.y - origCameraPosition.y;
//            px = canvas.getCameraController().getCameraShaker().getStartPosition().x + deltaX;
//            py = canvas.getCameraController().getCameraShaker().getStartPosition().y - deltaY;
//        } else {
//            px = canvas.getCameraController().getCamera().position.x;
//            py = canvas.getCameraController().getCamera().position.y;
//        }
        Vector3 offset = canvas.getCameraController().getCameraShaker().getOffset();
        px = canvas.getCameraController().getBackgroundCoordinates().x;
        py = canvas.getCameraController().getBackgroundCoordinates().y;
        float stx=levelToTileCoordinatesX(this.startX);
        float sty=levelToTileCoordinatesY(this.startY);
        float diffX=px-stx - offset.x;
        float diffY=py-sty - offset.y;
        //the background moves with the player,the farther back the faster the speed
        canvas.draw(background_L1, Color.WHITE, backgroundOx + 30, backgroundOy, diffX, diffY, 0, this.scaleforBackground, this.scaleforBackground);
        canvas.draw(background_L2, Color.WHITE, backgroundOx, backgroundOy, diffX*0.8f, diffY*0.8f, 0,  this.scaleforBackground, this.scaleforBackground);
        canvas.draw(background_L3, Color.WHITE, backgroundOx, backgroundOy, diffX*0.7f, diffY*0.7f, 0,  this.scaleforBackground, this.scaleforBackground);
        canvas.draw(background_L4, Color.WHITE, backgroundOx, backgroundOy, diffX*0.4f, diffY*0.4f, 0,  this.scaleforBackground, this.scaleforBackground);
        canvas.draw(background_L5, Color.WHITE, backgroundOx, backgroundOy, diffX*0.1f, diffY*0.1f, 0,  this.scaleforBackground, this.scaleforBackground);

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
            if (obj.getClass().equals(Billboard.class) || obj.getClass().equals(Exit.class)
                    || obj.getClass().equals(TutorialArea.class)) {
                obj.draw(canvas);
                if (obj.getClass().equals(TutorialArea.class)) {
                    TutorialArea tutorialArea = (TutorialArea) obj;
                    if (tutorialArea.isDisplay()) tutorialArea.displayTutorial(canvas, this);

                }
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
        uiElements.draw(canvas, player.getSpirit(), player.getHearts());
        boolean alreadyDisplay = false;
        for (Billboard billboard : billboards) {
            if (billboard.isDisplay() && !alreadyDisplay) {
                billboard.displayDialog(canvas);
                alreadyDisplay = true;
            }
        }
        canvas.end();

        canvas.getCameraController().setGameplayCamera(canvas,cam_x,cam_y, cameraWidth, cameraHeight);

        if (shouldShakeCamera && cameraShakeOn) {
            origCameraPosition = canvas.getCamera().position.cpy();
            switch (cameraShakeType) {
                case 1:
                    canvas.getCameraController().shakeCamera(1);
                    break;
                case 0:
                    canvas.getCameraController().shakeCamera(0);
                    break;
            }

            shouldShakeCamera = false;
            cameraShaked = true;
        }

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
        for (Spike spike : spikes) {
            addObject(spike);
        }

        for (Billboard billboard : billboards) {
            addObject(billboard);
        }

        for (Enemy enemy : enemies) {
            addObject(enemy);
        }

        for (TutorialArea tutorialArea: tutorialAreas) {
            addObject(tutorialArea);
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
        ArrayList<Enemy> result = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
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
        tutorialAreas.clear();
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

    public void shakeControllerSmall() {
        try {
            controller.startVibration(100,0.3f);
        } catch (Exception e) {

        }
    }

    public void shakeControllerMedium() {
        try {
            controller.startVibration(100,0.5f);
        } catch (Exception e) {

        }
    }

    public void shakeControllerHeavy() {
        try {
            controller.startVibration(100,0.5f);
        } catch (Exception e) {

        }
    }

    public void setShouldShakeCamera(boolean value, int strength) {
        shouldShakeCamera = value;
        cameraShakeType = strength;
    }


}
